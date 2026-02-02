package org.rookies.zdme.dto.email;

import lombok.Builder;
import lombok.Getter;
import org.rookies.zdme.model.entity.EmailTemplate;

import java.time.LocalDateTime;

@Getter
public class EmailTemplateResponseDto {
    private final Long id;
    private final String name;
    private final String subject;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public EmailTemplateResponseDto(EmailTemplate template) {
        this.id = template.getId();
        this.name = template.getName();
        this.subject = template.getSubject();
        this.content = template.getContent();
        this.createdAt = template.getCreatedAt();
        this.updatedAt = template.getUpdatedAt();
    }
}
