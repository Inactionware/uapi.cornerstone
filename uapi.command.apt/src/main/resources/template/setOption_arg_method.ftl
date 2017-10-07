<#list opt in options>
if ("${opt.name()}".equals(name) {
                this.${opt.fieldName()} = value;
                return;
            }
</#list>
            super.setOption(name, value);