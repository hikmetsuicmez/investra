package com.investra.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EmailTemplateServiceImplTest {

    private EmailTemplateServiceImpl emailTemplateService;

    @Mock
    private TemplateEngine templateEngine;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        emailTemplateService = new EmailTemplateServiceImpl(templateEngine);
    }

    @Test
    public void testProcessTemplate() {
        String templateName = "sample-template";
        Map<String, Object> variables = Map.of("user", "Hakan");

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Test</html>");

        String result = emailTemplateService.processTemplate(templateName, variables);

        assertEquals("<html>Test</html>", result);
        verify(templateEngine).process(eq("email/" + templateName), any(Context.class));
    }
    @Test
    public void testProcessTemplate_WithNullVariables() {
        String templateName = "sample-template";

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Test with null variables</html>");

        String result = emailTemplateService.processTemplate(templateName, null);

        assertEquals("<html>Test with null variables</html>", result);
        verify(templateEngine).process(eq("email/" + templateName), any(Context.class));
    }
    @Test(expected = RuntimeException.class)
    public void testProcessTemplate_WhenTemplateEngineThrows() {
        String templateName = "error-template";

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Template engine error"));

        emailTemplateService.processTemplate(templateName, Map.of());
    }
}