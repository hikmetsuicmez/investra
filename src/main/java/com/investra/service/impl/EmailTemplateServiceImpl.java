package com.investra.service.impl;

import com.investra.service.EmailTemplateService;
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
        log.info("processTemplate() çağrıldı - templateName: {}", templateName);

        try {
            Context context = new Context();

            if (variables != null) {
                log.info("Şablona aktarılacak {} adet değişken bulundu.", variables.size());
                         variables.forEach((key, value) -> log.info("Değişken - {}: {}", key, value));
                variables.forEach(context::setVariable);
            }else {
            log.info("Şablon için değişken listesi boş ya da null.");
            }

            context.setVariable("currentYear", Year.now().getValue());

            String fullTemplateName = "email/" + templateName;
            log.info("Şablon işleniyor: {}", fullTemplateName);
            String result = templateEngine.process(fullTemplateName, context);

            log.info("Şablon başarıyla işlendi: {}", fullTemplateName);
            return result;

        } catch (Exception e) {
            log.error("Email şablonu işlenirken hata oluştu ({}): {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Email şablonu işlenirken hata oluştu: " + e.getMessage(), e);
        }
    }
}
