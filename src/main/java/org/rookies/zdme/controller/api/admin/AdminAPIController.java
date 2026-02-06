package org.rookies.zdme.controller.api.admin;

import org.rookies.zdme.dto.LoginRequest;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.security.JwtUtil;
import org.rookies.zdme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) { // No longer throws generic Exception
        try {
            // 1. 아이디/비밀번호 검증
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

            final UserDetails userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());
            User user = (User) userDetails;

            // Admin Level Check: Only adminLevel 1 or 2 users can log in to the admin panel
            if (user.getAdminLevel() == null || user.getAdminLevel() == 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Admin access denied: Insufficient privileges.");
            }

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
                    .header("Access-Control-Expose-Headers", "X-2FA-Status")
                    .header("X-2FA-Status", "required")
                    .body(response);
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Handle authentication specific exceptions
            if (e instanceof org.springframework.security.authentication.BadCredentialsException) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            } else if (e instanceof org.springframework.security.authentication.DisabledException) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body("User is disabled");
            }
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void authenticate(String username, String password) throws AuthenticationException {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw e;
        } catch (BadCredentialsException e) {
            throw e;
        }
    }
}