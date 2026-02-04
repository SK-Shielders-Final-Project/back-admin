package org.rookies.zdme.controller.api;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.*;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.security.JwtUtil;
import org.rookies.zdme.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserAPIController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/auth/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) {
        try {
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

            // 유저 정보 조회
            final User user = (User) userService.loadUserByUsername(authenticationRequest.getUsername());

            // 토큰 발급
            final String accessToken = jwtUtil.generateToken(user);
            final String refreshToken = jwtUtil.generateRefreshToken(user);
            userService.saveRefreshToken(user.getUsername(), refreshToken);

            // DB에서 실제 설정 여부(true/false)를 가져옴
            boolean isEnrolled = user.is2faEnabled();
            Long userId = user.getUserId();

            // DTO에 담아서 응답 (isEnrolled를 is2faEnabled 위치에 전달)
            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, userId, isEnrolled, true));

        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "계정 비활성화"));
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "로그인 정보 불일치"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "로그인 처리 중 오류"));
        }
    }

    // 나머지 메서드들은 기존과 동일 (생략하지 않고 전문 유지 권장)
    @PostMapping("/auth/signup")
    public ResponseEntity<?> vulnerableSignup(@RequestBody Map<String, Object> requestData) {
        try {
            User newUser = userService.vulnerableSignup(requestData);
            return ResponseEntity.ok(SignupResponse.fromEntity(newUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/info/{user_id}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable("user_id") Long userId) {
        try {
            Map<String, Object> userInfo = userService.getUserInfo(userId);
            return new ResponseEntity<>(userInfo, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "회원 정보 조회 중 오류"));
        }
    }

    private void authenticate(String username, String password) throws DisabledException, BadCredentialsException {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}