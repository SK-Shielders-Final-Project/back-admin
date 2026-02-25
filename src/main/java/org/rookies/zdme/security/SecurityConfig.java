package org.rookies.zdme.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * ⚠️ HTTP Firewall 설정: 특정 특수문자(;, //, %2e) 허용
     * 보안상 위험할 수 있으나, 모의해킹/테스트 시나리오를 위해 명시적으로 허용 설정
     */
    @Bean
    public HttpFirewall allowSemicolonHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowUrlEncodedPeriod(true);
        return firewall;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.httpFirewall(allowSemicolonHttpFirewall());
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * AuthenticationManager 설정
     * userDetailsService, 패스워드 인코더 연결
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    /**
     * HTTP 요청에 대한 보안 규칙(URL별 접근 제어, CSRF, 세션 등) 정의
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        // Spring Security 5.6(Boot 2.6)에는 Builder가 없으므로 직접 생성자를 사용해야 합니다.
        // CVE-2023-20860 대응을 위해 MvcRequestMatcher 인스턴스를 생성합니다.
        http
                .csrf(csrf -> csrf.disable())
                .authorizeRequests(auth -> auth
                        // 개별 URL에 대해 MvcRequestMatcher를 수동으로 생성하여 적용
                        .requestMatchers(
                                new MvcRequestMatcher(introspector, "/swagger-ui/**"),
                                new MvcRequestMatcher(introspector, "/v3/api-docs/**"),
                                new MvcRequestMatcher(introspector, "/swagger-ui.html")
                        ).permitAll()

                        // 로그인 API 경로 등은 인증 없이 누구나 접근 가능하게 설정 (Permit All)
                        .antMatchers(SecurityConstants.PUBLIC_URLS).permitAll()

                        // ⚠️ Actuator 및 Admin 경로 보호 (생성자 방식 적용)
                        .requestMatchers(new MvcRequestMatcher(introspector, "/actuator/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(new MvcRequestMatcher(introspector, "/api/admin/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .anyRequest().authenticated()
                )
                // 세션을 서버에 저장하지 않도록 STATELESS로 설정 (JWT 필수 설정)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // JWT 필터 위치 설정
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
