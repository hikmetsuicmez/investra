package com.investra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final TemplateEngine templateEngine;

    @Override
    public String processTemplate(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();

            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            context.setVariable("currentYear", Year.now().getValue());

            return templateEngine.process("email/" + templateName, context);
        } catch (Exception e) {
            log.error("Email şablonu işlenirken hata oluştu ({}): {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Email şablonu işlenirken hata oluştu: " + e.getMessage(), e);
        }
    }
}
