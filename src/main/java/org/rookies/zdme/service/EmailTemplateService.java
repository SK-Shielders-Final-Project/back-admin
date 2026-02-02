package org.rookies.zdme.service;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.email.EmailTemplateResponseDto;
import org.rookies.zdme.dto.email.EmailTemplateUpdateRequestDto;
import org.rookies.zdme.dto.email.TemplatePreviewRequestDto;
import org.rookies.zdme.exception.NotFoundException;
import org.rookies.zdme.model.entity.EmailTemplate;
import org.rookies.zdme.repository.EmailTemplateRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final TemplateEngine templateEngine;

    public EmailTemplateService(EmailTemplateRepository emailTemplateRepository,
                                @Qualifier("stringTemplateEngine") TemplateEngine templateEngine) {
        this.emailTemplateRepository = emailTemplateRepository;
        this.templateEngine = templateEngine;
    }

    @Transactional(readOnly = true)
    public EmailTemplateResponseDto getTemplate(String name) {
        EmailTemplate template = emailTemplateRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Email template not found: " + name));
        return EmailTemplateResponseDto.builder().template(template).build();
    }

    @Transactional
    public void updateTemplate(String name, EmailTemplateUpdateRequestDto requestDto) {
        EmailTemplate template = emailTemplateRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Email template not found: " + name));
        template.updateTemplate(requestDto.getSubject(), requestDto.getContent());
        emailTemplateRepository.save(template);
    }
    
    @Transactional(readOnly = true)
    public String previewTemplate(TemplatePreviewRequestDto requestDto) {
        // SSTI 취약점 구현부
        // 사용자가 제공한 템플릿 문자열을 정제하지 않고 그대로 사용
        Context context = new Context();
        context.setVariable("username", "testuser");
        context.setVariable("resetLink", "http://localhost:8080/reset-password?token=PREVIEW_TOKEN");

        return templateEngine.process(requestDto.getContent(), context);
    }

    @Transactional(readOnly = true)
    public EmailTemplate findByName(String name) {
        return emailTemplateRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Email template not found: " + name));
    }
}
