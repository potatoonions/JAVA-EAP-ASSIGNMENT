package com.crs.template;

import java.util.Map;

public class TemplateEngine {
    private TemplateEngine() {}
 
    public static String resolve(String template, Map<String, String> tokens) {
        if (template == null)
            throw new IllegalArgumentException("Template string must not be null.");
        if (tokens == null || tokens.isEmpty())
            return template;
 
        String result = template;
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            String placeholder = entry.getKey();
            String value       = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
 
    public static String resolve(String template, String placeholder, String value) {
        if (template == null)
            throw new IllegalArgumentException("Template string must not be null.");
        return template.replace(
                placeholder,
                value != null ? value : "");
    }
}
