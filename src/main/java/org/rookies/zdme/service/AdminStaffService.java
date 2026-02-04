package org.rookies.zdme.service;

import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateRequest;
import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateResponse;
import org.rookies.zdme.dto.admin.StaffListResponse;
import org.rookies.zdme.exception.NotFoundException;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminStaffService {

    private final UserRepository userRepository;

    public AdminStaffService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ✅ 회원 목록 조회
     * 반환 형태(예시):
     * [
     *   { "user_id": 1, "email": "...", "admin_level": 0 },
     *   ...
     * ]
     *
     * ※ Controller가 List<StaffListResponse>로 반환하도록 맞추는 걸 추천
     */
    @Transactional(readOnly = true)
    public List<StaffListResponse> getStaffList() {
        List<User> users = userRepository.findAll();

        System.out.println("GET staffList");

        return users.stream()
                .map(u -> StaffListResponse.builder()
                        .user_id(u.getUserId())
                        .email(u.getEmail())
                        .admin_level(u.getAdminLevel())
                        .user_name(u.getUsername())
                        .name(u.getName())
                        .phone(u.getPhone())

                        .build())
                .collect(Collectors.toList());
    }

    /**
     * ✅ 회원 권한 수정
     */
    @Transactional
    public StaffAdminLevelUpdateResponse updateAdminLevel(StaffAdminLevelUpdateRequest req) {
        validateAdminLev(req.getAdmin_level());

        User target = userRepository.findById(req.getUser_id())
                .orElseThrow(() -> new NotFoundException("target user not found"));

        target.setAdminLevel(req.getAdmin_level());
        User saved = userRepository.save(target);

        return StaffAdminLevelUpdateResponse.builder()
                .user_id(saved.getUserId())
                .username(saved.getUsername())
                .admin_level(saved.getAdminLevel())
                .updated_at(saved.getUpdatedAt())
                .build();
    }
    
    /**
     * ✅ 2FA 최초 설정 완료 처리
     * 최초 OTP 검증 성공 시 호출되어, 해당 유저의 2FA 상태를 활성화하고 secret 키를 DB에 영구 저장합니다.
     * @param username 대상 사용자 아이디
     * @param secret 사용자의 2FA 비밀 키
     */
    @Transactional
    public void confirmAndEnable2FA(String username, String secret) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        user.enable2FA(secret);
        userRepository.save(user);
    }

    private void validateAdminLev(Integer lev) {
        // 1. null 체크를 먼저 해야 합니다.
        if (lev == null) {
            throw new IllegalArgumentException("admin_level is required");
        }
        // 2. 범위를 체크합니다. (0, 1, 2만 허용)
        if (lev < 0 || lev > 2) {
            throw new IllegalArgumentException("admin_level must be 0, 1, or 2");
        }
    }
}
