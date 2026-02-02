package org.rookies.zdme.dto.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TemplatePreviewResponseDto {
    private final String renderedContent;
}
