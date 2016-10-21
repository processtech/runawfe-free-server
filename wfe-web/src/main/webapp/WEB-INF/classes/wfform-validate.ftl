<#--
START SNIPPET: supported-validators
Only the following validators are supported:
* required validator
* requiredstring validator
* stringlength validator
* regex validator
* email validator
* url validator
* number validator
END SNIPPET: supported-validators
-->
<#if ((parameters.validate?default(false) == true) && (parameters.performValidation?default(false) == true))>
<script type="text/javascript">
    function validateForm_${parameters.id}() {
        form = document.getElementById("${parameters.id}");
        clearFieldErrorMessages(form);
        var errors = false;
    <#list parameters.tagNames as tagName>
        <#list tag.getValidators("${tagName}") as validator>
        // field name: ${validator.fieldName}
        // validator name: ${validator.config.type}
        if (form.elements['${validator.fieldName}']) {
            field = form.elements['${validator.fieldName}'];
            var error = "${validator.getMessage()}";
            <#if validator.config.type = "required">
            if (field.value == "") {
                addFieldErrorMessage(field, error);
                errors = true;
            }
            <#elseif validator.config.type = "requiredstring">
            if (field.value != null && (field.value == "" || field.value.replace(/^\s+|\s+$/g,"").length == 0)) {
                addFieldErrorMessage(field, error);
                errors = true;
            }
            <#elseif validator.config.type = "stringlength">
            if (field.value != null) {
                var value = field.value;
                <#if validator.trim>
                    //trim field value
                    while (value.substring(0,1) == ' ')
                        value = value.substring(1, value.length);
                    while (value.substring(value.length-1, value.length) == ' ')
                        value = value.substring(0, value.length-1);
                </#if>
                if(value.length > 0 && (
                        (${validator.minLength} > -1 && value.length < ${validator.minLength}) ||
                        (${validator.maxLength} > -1 && value.length > ${validator.maxLength})
                    )) {
                    addFieldErrorMessage(field, error);
                    errors = true;
                }
            } 
            <#elseif validator.config.type = "regex">
            if (field.value != null && !field.value.match("${validator.expression?js_string}")) {
                addFieldErrorMessage(field, error);
                errors = true;
            }
            <#elseif validator.config.type = "number">
            if ((field.value != null) && (field.value != "")) {
            	if (isNaN(parseInt(field.value))) {
            		addFieldErrorMessage(field, error);
	                errors = true;
            	}
                if (<#if validator.minComparatorValue?exists>parseInt(field.value) <
                     ${validator.minComparatorValue}<#else>false</#if> ||
                        <#if validator.maxComparatorValue?exists>parseInt(field.value) >
                           ${validator.maxComparatorValue}<#else>false</#if>) {
                    addFieldErrorMessage(field, error);
                    errors = true;
                }
            }
            </#if>
        }
        </#list>
    </#list>

        return !errors;
    }
    
	function clearFieldErrorMessages(form) {
		$("img[errorFor]").each(function() {
			$(this).remove();
		});
	}
	
	function addFieldErrorMessage(field, errorText) {
	    var errorImg = document.createElement("img");
	    errorImg.setAttribute("title", errorText);
	    errorImg.setAttribute("src", "/wfe/images/error.gif");
	    errorImg.setAttribute("errorFor", "yes");
	    field.parentNode.insertBefore(errorImg, field.nextSibling);
	}
</script>
</#if>
