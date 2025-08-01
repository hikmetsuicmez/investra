package com.investra.service;

import java.util.Map;


public interface EmailTemplateService {

    String processTemplate(String templateName, Map<String, Object> variables);
}
