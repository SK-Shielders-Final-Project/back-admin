package org.rookies.zdme.model.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.hibernate.annotations.Type;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column
    private Long totalPoint;

    @Column
    private Integer adminLevel;

    @Column(name = "card_number", length = 50)
    private String cardNumber;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // IS_2FA_ENABLED 컬럼 매핑 (0/1을 false/true로 변환)
    @Column(name = "IS_2FA_ENABLED")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean is2faEnabled = false;

    @Column(name = "TWO_FACTOR_SECRET", length = 255)
    private String twoFactorSecret;

    // boolean 필드의 경우 Lombok Getter와 충돌을 방지하기 위해 명시적 메서드 추가
    public boolean is2faEnabled() {
        return this.is2faEnabled;
    }

    public void set2faEnabled(boolean is2faEnabled) {
        this.is2faEnabled = is2faEnabled;
    }

    public void enable2FA(String secret) {
        this.twoFactorSecret = secret;
        this.is2faEnabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateInfo(String name, String email, String phone, Integer adminLevel) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        if (adminLevel != null) {
            this.adminLevel = adminLevel;
        }
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.adminLevel != null) {
            if (this.adminLevel == 2) {
                return List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            } else if (this.adminLevel == 1) {
                return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() { return this.password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    @Column(length = 512)
    private String refreshToken;

    public void updatePoint(Long amount) { this.totalPoint += amount; }

    public void updateRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (totalPoint == null) totalPoint = 0L;
        if (adminLevel == null) adminLevel = 0;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public void setAdminLevel(Integer adminLevel) { this.adminLevel = adminLevel; }
}