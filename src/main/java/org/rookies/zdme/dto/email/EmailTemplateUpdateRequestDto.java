package org.rookies.zdme.dto.email;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailTemplateUpdateRequestDto {
    private String subject;
    private String content;
}
