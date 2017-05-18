try {
<#list methods as method>
            super.${method}();
</#list>
        } catch (Exception ex) {
            throw new uapi.GeneralException(ex);
        }