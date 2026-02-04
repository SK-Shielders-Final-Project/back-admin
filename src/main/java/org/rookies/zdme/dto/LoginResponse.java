package org.rookies.zdme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;

    @JsonProperty("tempAccessToken")
    private final String accessToken;

    @JsonProperty("tempRefreshToken")
    private final String refreshToken;

    @JsonProperty("userId")
    private final Long userId;

    // 변수명을 enrolled로 바꿔서 Jackson의 'is' 삭제 버그를 방지합니다.
    @JsonProperty("is2faEnabled")
    private final boolean enrolled;

    @JsonProperty("requires2FA")
    private final boolean requires2FA;
}