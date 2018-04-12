uapi.common.ArgumentChecker.required(attributes, "attributes");
        String missingAttr = uapi.rx.Looper.on(requiredAttributes()).filter(! attributes::contains).first(null);
        if (missingAttr != null) {
            throw new uapi.GeneralException("Missing required attribute - {}", missingAttr);
        }
        this._attributes = attributes;