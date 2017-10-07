<#list opt in options>
if ("${opt.name()}".equals(name) {
                this.${opt.fieldName()} = true;
                return;
            }
</#list>
            super.setOption(name);