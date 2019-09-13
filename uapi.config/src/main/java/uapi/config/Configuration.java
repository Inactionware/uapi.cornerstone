/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config;

import uapi.GeneralException;
import uapi.InvalidArgumentException;
import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.IServiceReference;
import uapi.service.QualifiedServiceId;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * The uapi.config.Configuration hold at least one config value and store as a tree structure
 */
public class Configuration {

    public static final String ROOT_KEY                 = "/";
    private static final String PATH_SEPARATOR_PATTERN  = "\\.";
    private static final String PATH_SEPARATOR          = ".";

    public static Configuration createRoot() {
        return new Configuration();
    }

    private final Configuration _parent;
    private final String _key;
    private Object _value;
    private final Map<QualifiedServiceId, WeakReference<IServiceReference>> _configurableSvcs;
    private final Map<String, Configuration> _children;

    public Configuration(final Configuration parent, final String key) {
        this(parent, key, null, null);
    }

    Configuration(
            final Configuration parent,
            final String key,
            final Object value,
            final IServiceReference serviceReference
    ) throws InvalidArgumentException {
        ArgumentChecker.notNull(parent, "parent");
        ArgumentChecker.notEmpty(key, "key");
        ArgumentChecker.notContains(key, ROOT_KEY, "key");
        this._parent = parent;
        this._key = key;
        this._value = value;
        this._configurableSvcs = new HashMap<>();
        if (serviceReference != null) {
            this._configurableSvcs.put(serviceReference.getQualifiedId(), new WeakReference<>(serviceReference));
        }
        this._children = new HashMap<>();
    }

    /**
     * Only for creating root configuration
     */
    private Configuration() {
        this._parent = null;
        this._key = ROOT_KEY;
        // For root node, not configurable service can be bind on it.
        this._configurableSvcs = null;
        this._children = new HashMap<>();
    }

    public String getKey() {
        return this._key;
    }

    public boolean isRoot() {
        return this._parent == null && this._key.equals(ROOT_KEY);
    }

    public Object getValue() {
        if (this._value != null) {
            return this._value;
        }
        if (this._children.size() > 0) {
            return this._children;
        }
        return null;
    }

    public Class<?> getValueType() {
        if (this._value != null) {
            return this._value.getClass();
        }
        return null;
    }

    public Object getValue(final String path) {
        ArgumentChecker.notEmpty(path, "path");
        var steps = path.split(PATH_SEPARATOR_PATTERN);
        var config = this;
        for (String step : steps) {
            if (config == null) {
                return null;
            }
            config = config.getChild(step);
        }
        return config.getValue();
    }

    @SuppressWarnings("unchecked")
    public void setValue(final Object value) {
        if (value instanceof Map) {
            setValue((Map<String, Object>) value);
        } else {
            this._value = value;
        }

        Looper.on(this._configurableSvcs.values())
                .filter(ref -> ref.get() != null)
                .map(WeakReference::get)
                .next(svcRef -> ((IConfigurable) svcRef.getService()).config(getFullPath(), value))
                .foreach(IServiceReference::notifySatisfied);
        cleanNullReference();
    }

    public void setValue(final Map<String, Object> configMap) {
        ArgumentChecker.notNull(configMap, "configMap");

        Looper.on(configMap.entrySet()).foreach(entry -> {
            Configuration config = getOrCreateChild(entry.getKey());
            config.setValue(entry.getValue());
        });
    }

    public void setValue(final String path, final Object value) {
        ArgumentChecker.notEmpty(path, "path");
        ArgumentChecker.notNull(value, "value");

        Configuration config = getOrCreateChild(path);
        config.setValue(value);
    }

    /**
     * Bind a service reference to current configuration.
     * If the configuration has value then the configuration item will be configured to the service then return true.
     * If the configuration has no value and the configuration is not optional for the service then the method will
     * return false.
     *
     * @param   serviceRef
     *          The service reference hold configurable service
     * @return  True means the service is configured or the configuration is optional otherwise return false
     */
    public boolean bindConfigurable(final IServiceReference serviceRef) {
        ArgumentChecker.notNull(serviceRef, "serviceRef");

        var path = getFullPath();
        if (this._configurableSvcs.containsKey(serviceRef.getQualifiedId())) {
            if (this._value != null) {
                return true;
            }
            return ((IConfigurable) serviceRef.getService()).isOptionalConfig(path);
        }
        var cfg = ((IConfigurable) serviceRef.getService());
        this._configurableSvcs.put(serviceRef.getQualifiedId(), new WeakReference<>(serviceRef));
        if (this._value != null) {
            cfg.config(path, this._value);
            return true;
        } else if (this._children.size() > 0) {
            cfg.config(path, getChildrenValue(this._children));
            return true;
        } else {
            return cfg.isOptionalConfig(path);
        }
    }

    /**
     * Bind a service reference to current configuration.
     * If the configuration has value then the configuration item will be configured to the service then return true.
     * If the configuration has no value and the configuration is not optional for the service then the method will
     * return false.
     *
     * @param   path
     *          The full path of the configuration
     * @param   serviceRef
     *          The service reference hold configurable service
     * @return  True means the service is configured or the configuration is optional otherwise return false
     */
    public boolean bindConfigurable(final String path, final IServiceReference serviceRef) {
        ArgumentChecker.notEmpty(path, "path");
        ArgumentChecker.notNull(serviceRef, "serviceRef");

        var config = getOrCreateChild(path);
        return config.bindConfigurable(serviceRef);
    }

    public Configuration getChild(String key) {
        ArgumentChecker.notEmpty(key, "key");
        if (this._children == null) {
            throw new GeneralException("The configuration[{}] can't has child", this._key);
        }
        return this._children.get(key);
    }

    public Configuration setChild(String key, Object value) {
        ArgumentChecker.notEmpty(key, "key");
        ArgumentChecker.notNull(value, "value");
        if (this._children == null) {
            throw new GeneralException("The configuration[{}] can't attach child", this._key);
        }
        var child = new Configuration(this, key, value, null);
        this._children.put(key, child);
        return child;
    }

    public Configuration setChild(String key) {
        ArgumentChecker.notEmpty(key, "key");
        if (this._children == null) {
            throw new GeneralException("The configuration[{}] can't attach child", this._key);
        }
        var child = new Configuration(this, key);
        this._children.put(key, child);
        return child;
    }

    public String getFullPath() {
        var buffer = new StringBuilder();
        var isRoot = isRoot();
        var cfg = this;
        while (! isRoot) {
            buffer.insert(0, cfg._key).insert(0, PATH_SEPARATOR);
            cfg = cfg._parent;
            isRoot = cfg.isRoot();
        }
        if (buffer.length() > 0) {
            return buffer.deleteCharAt(0).toString();
        } else {
            return StringHelper.EMPTY;
        }
    }

    private Configuration getOrCreateChild(final String path) {
        ArgumentChecker.notEmpty(path, "path");
        var steps = path.split(PATH_SEPARATOR_PATTERN);
        var config = this;
        for (String step : steps) {
            Configuration child = config.getChild(step);
            if (child == null) {
                config = config.setChild(step);
            } else {
                config = child;
            }
        }
        return config;
    }

    private void cleanNullReference() {
//        var it = this._configurableSvcs.entrySet().iterator();
//        while (it.hasNext()) {
//            if (it.next().getValue().get() == null) {
//                it.remove();
//            }
//        }
        this._configurableSvcs.entrySet().removeIf(svcRefEntry -> svcRefEntry.getValue().get() == null);
    }

    /**
     * The value of configuration may be wrapped by Configuration object.
     * To inject configuration to service we have to unwrap it first.
     *
     * @param   value
     *          The configuration value
     * @return  Un-warped configuration value
     */
    private Object getChildrenValue(final Map<String, Configuration> value) {
        if (value == null) {
            return null;
        }

        var mapValue = new HashMap<String, Object>();
        Looper.on(value.entrySet())
                .foreach(entry -> {
                    if (entry.getValue()._children.size() > 0) {
                        mapValue.put(entry.getKey(), getChildrenValue(entry.getValue()._children));
                    } else {
                        mapValue.put(entry.getKey(), entry.getValue()._value);
                    }
                });
        return mapValue;
    }
}
