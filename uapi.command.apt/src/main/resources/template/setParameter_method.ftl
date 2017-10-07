<#list param in parameters>
if ("${param.name()}".equals(name) {
                this.${param.fieldName()} = value;
                return;
            }
</#list>
            super.setParameter(name, value);