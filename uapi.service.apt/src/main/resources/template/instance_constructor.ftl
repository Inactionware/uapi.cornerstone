uapi.common.ArgumentChecker.required(attributes, "attributes");
        String missingAttr = uapi.rx.Looper.on(requiredAttributes()).filter(reqAttr -> ! attributes.containsKey(reqAttr)).first(null);
        if (missingAttr != null) {
            throw new uapi.GeneralException("Missing required attribute - {}", missingAttr);
        }
        this._attributes = attributes;

        uapi.rx.Looper.on(attributes.entrySet())
                .foreach(attrEntry -> {
                <#list attrs as attr>
    if (attrEntry.getKey().equals("${attr.name}")) {
                        if (! (attrEntry.getValue() instanceof ${attr.type})) {
                            throw new uapi.GeneralException(
                                    "The attribute value {} can't convert to {}",
                                    attrEntry.getValue(), "${attr.type}");
                        }
                        ${attr.field} = (${attr.type}) attrEntry.getValue();
                        return;
                    }
                </#list>
                });
