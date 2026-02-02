package org.rookies.zdme.controller.admin;

import org.rookies.zdme.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.email.EmailTemplateResponseDto;
import org.rookies.zdme.dto.email.EmailTemplateUpdateRequestDto;
import org.rookies.zdme.dto.email.TemplatePreviewRequestDto;
import org.rookies.zdme.dto.email.TemplatePreviewResponseDto;
import org.rookies.zdme.service.EmailTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/email-templates")
@RequiredArgsConstructor
public class AdminEmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @GetMapping("/{name}")
    public ResponseEntity<ApiResult<EmailTemplateResponseDto>> getTemplate(@PathVariable String name) {
        EmailTemplateResponseDto template = emailTemplateService.getTemplate(name);
        return ResponseEntity.ok(ApiResult.ok(template));
    }

    @PutMapping("/{name}")
    public ResponseEntity<ApiResult<?>> updateTemplate(@PathVariable String name, @RequestBody EmailTemplateUpdateRequestDto requestDto) {
        emailTemplateService.updateTemplate(name, requestDto);
        return ResponseEntity.ok(ApiResult.ok(null));
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResult<TemplatePreviewResponseDto>> previewTemplate(@RequestBody TemplatePreviewRequestDto requestDto) {
        String renderedContent = emailTemplateService.previewTemplate(requestDto);
        return ResponseEntity.ok(ApiResult.ok(new TemplatePreviewResponseDto(renderedContent)));
    }
}
