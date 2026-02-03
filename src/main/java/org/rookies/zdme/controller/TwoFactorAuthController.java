package org.rookies.zdme.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/2fa")
public class TwoFactorAuthController {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    // 시연을 위해 고정된 Secret Key를 사용합니다.
    private static final String USER_SECRET_KEY = "MZXW6YTBOIIDCMZS";

    @GetMapping("/generate-secret")
    public ResponseEntity<?> generateSecret() {
        try {
            String secret = USER_SECRET_KEY;

            String otpAuthURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    "ZDM-Admin", "admin-user", secret, "ZDM");

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthURL, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            String qrCodeBase64 = Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());

            System.out.println("QR 생성에 사용된 Secret: " + secret);

            return ResponseEntity.ok(Map.of(
                    "qrCode", "data:image/png;base64," + qrCodeBase64,
                    "manualKey", secret
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating QR code");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");

        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Code is empty");
        }

        try {
            boolean isValid = gAuth.authorize(USER_SECRET_KEY, Integer.parseInt(code));

            // 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            // [중요] 프론트엔드 Axios에서 커스텀 헤더인 X-2FA-Status에 접근할 수 있도록 허용
            headers.add("Access-Control-Expose-Headers", "X-2FA-Status");

            if (isValid) {
                // [변경] 성공 시 헤더 값을 success로 설정
                headers.add("X-2FA-Status", "success");
                System.out.println("인증 성공: success 헤더 반환");
                return ResponseEntity.ok().headers(headers).body(true);
            } else {
                // [변경] 실패 시 헤더 값을 fail로 설정 (상태코드는 401 유지)
                headers.add("X-2FA-Status", "fail");
                System.out.println("인증 실패: fail 헤더 반환");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(false);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request");
        }
    }
}