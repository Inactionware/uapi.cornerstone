<#list param in parameters>
if ("${param.name()}".equals(name) {
                this.${param.userCommandField()}.${param.setterName()}(value);
                return;
            }
</#list>
            super.setParameter(name, value);