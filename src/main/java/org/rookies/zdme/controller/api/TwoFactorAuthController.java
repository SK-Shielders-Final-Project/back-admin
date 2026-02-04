package org.rookies.zdme.controller.api;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.RequiredArgsConstructor;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.UserRepository;
import org.rookies.zdme.security.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/2fa")
public class TwoFactorAuthController {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // 요구사항: 고정된 공통 Secret Key 사용
    private static final String USER_SECRET_KEY = "MZXW6YTBOIIDCMZS";

    @PostMapping("/generate-secret") // 프론트 요청에 맞춰 PostMapping 유지
    public ResponseEntity<?> generateSecret() {
        try {
            String otpAuthURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    "ZDM-Admin", "admin-user", USER_SECRET_KEY, "ZDM");

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthURL, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            String qrCodeBase64 = Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());

            return ResponseEntity.ok(Map.of(
                    "qrCodeDataUri", "data:image/png;base64," + qrCodeBase64
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating QR code");
        }
    }

    // 최초 등록 및 검증 (IS_2FA_ENABLED를 1로 변경)
    @PostMapping("/verify-initial")
    public ResponseEntity<?> verifyInitial(@RequestBody Map<String, String> payload) {
        return processVerification(payload, true);
    }

    // 이후 일반 로그인 검증
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        return processVerification(payload, false);
    }

    private ResponseEntity<?> processVerification(Map<String, String> payload, boolean isInitial) {
        String code = payload.get("code");
        String userIdStr = payload.get("userId");

        if (code == null || userIdStr == null) return ResponseEntity.badRequest().body("Missing parameters");

        try {
            boolean isValid = gAuth.authorize(USER_SECRET_KEY, Integer.parseInt(code));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Access-Control-Expose-Headers", "X-2FA-Status");

            if (isValid) {
                User user = userRepository.findById(Long.parseLong(userIdStr)).orElseThrow();

                // 최초 등록인 경우 DB 업데이트
                if (isInitial) {
                    user.set2faEnabled(true);
                    userRepository.save(user);
                }

                // 성공 시 실제 토큰 발급
                String accessToken = jwtUtil.generateToken(user);
                String refreshToken = jwtUtil.generateRefreshToken(user);

                headers.add("X-2FA-Status", "success");
                return ResponseEntity.ok().headers(headers).body(Map.of(
                        "success", true,
                        "accessToken", accessToken,
                        "refreshToken", refreshToken,
                        "userId", user.getUserId()
                ));
            } else {
                headers.add("X-2FA-Status", "fail");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(Map.of("success", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request");
        }
    }
}