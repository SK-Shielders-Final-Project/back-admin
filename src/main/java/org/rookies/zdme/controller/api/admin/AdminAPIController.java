package org.rookies.zdme.controller.api.admin;

import org.rookies.zdme.dto.LoginRequest;
import org.rookies.zdme.dto.LoginResponse;
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
        final Long userId = ((User) userDetails).getUserId();

        // [핵심 수정] 1차 로그인 성공 시, 실제 토큰을 발행하지만 프론트엔드에는 2FA가 필요함을 알림
        // 시연을 위해 토큰은 생성하되, OTP 검증 전까지는 클라이언트가 '임시'로만 알게 함
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        userService.saveRefreshToken(userDetails.getUsername(), refreshToken);

        Map<String, Object> response = new HashMap<>();
        response.put("tempAccessToken", accessToken);
        response.put("tempRefreshToken", refreshToken);
        response.put("userId", userId);
        response.put("requires2FA", true);

        return ResponseEntity.ok(response);
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