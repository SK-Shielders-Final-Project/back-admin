package org.rookies.zdme.controller.api.admin;

import org.rookies.zdme.dto.LoginRequest;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.security.JwtUtil;
import org.rookies.zdme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
public class AdminAPIController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/auth/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) throws Exception {
        // 1. 아이디/비밀번호 검증
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        // 2. 관리자 권한 체크
        userService.checkAdminRole(authenticationRequest.getUsername());

        final UserDetails userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());
        User user = (User) userDetails;

        final Long userId = user.getUserId();
        final boolean is2faEnabled = user.is2faEnabled();

        // [우회 포인트] 1차 로그인 성공 시 토큰을 생성하여 프론트에 미리 전달
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        userService.saveRefreshToken(userDetails.getUsername(), refreshToken);

        // 응답 맵 구성
        Map<String, Object> response = new HashMap<>();
        response.put("tempAccessToken", accessToken);
        response.put("tempRefreshToken", refreshToken);
        response.put("userId", userId);
        response.put("is2faEnabled", is2faEnabled);
        response.put("requires2FA", true);
        response.put("success", false); // 프론트에서 우회 시 true로 조작할 필드

        return ResponseEntity.ok()
                // 프론트엔드가 읽을 수 있도록 헤더 노출
                .header("Access-Control-Expose-Headers", "X-2FA-Status")
                .header("X-2FA-Status", "required")
                .body(response);
    }

    private void authenticate(String username, String password) throws Exception {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}