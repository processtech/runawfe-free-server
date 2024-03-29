package ru.runa.wf.web.tag;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.FieldValidator;
import ru.runa.wfe.validation.Validator;
import ru.runa.wfe.validation.ValidatorContext;
import ru.runa.wfe.validation.ValidatorManager;

public class XWorkJavascriptValidator {

    public static String getJavascript(User user, byte[] validationXmlBytes) {
        try {
            InputStream is = ClassLoaderUtil.getAsStreamNotNull("wfform-validate.ftl", XWorkJavascriptValidator.class);
            Configuration cfg = new Configuration();
            Template template = new Template("validate", new InputStreamReader(is), cfg);
            template.setEncoding(Charsets.UTF_8.name());

            Map<String, Object> model = new HashMap<String, Object>();
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("validate", Boolean.TRUE);
            parameters.put("performValidation", Boolean.TRUE);
            parameters.put("id", WFFormTag.FORM_NAME);

            Set<String> tagNames = new HashSet<String>();

            ValidatorContext validatorContext = new ValidatorContext();
            List<Validator> validators = ValidatorManager.getInstance().createValidators(null, null, validationXmlBytes, validatorContext,
                    new HashMap<String, Object>());
            for (Validator validator : validators) {
                if (validator instanceof FieldValidator) {
                    tagNames.add(((FieldValidator) validator).getFieldName());
                }
            }
            parameters.put("tagNames", tagNames);

            model.put("parameters", parameters);
            model.put("tag", new ValidatorCallback(validators));

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            template.process(model, new OutputStreamWriter(result, Charsets.UTF_8));

            return new String(result.toByteArray(), Charsets.UTF_8);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static class ValidatorCallback {
        private List<Validator> validators;

        public ValidatorCallback(List<Validator> validators) {
            this.validators = validators;
        }

        /**
         * Get all field validators. Used from web.
         */
        public List<Validator> getValidators(String name) {
            List<Validator> fieldValidators = new ArrayList<Validator>();
            for (Validator validator : validators) {
                if (validator instanceof FieldValidator) {
                    if (((FieldValidator) validator).getFieldName().equals(name)) {
                        fieldValidators.add(validator);
                    }
                }
            }
            return fieldValidators;
        }
    }
}
