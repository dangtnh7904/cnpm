package com.nhom33.quanlychungcu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity

public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, @Lazy AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/payment/vnpay/callback",
                                "/api/payment/vnpay-return",
                                "/actuator/health"
                        ).permitAll()
                // API Quản lý phí - ADMIN, MANAGER, ACCOUNTANT
                .requestMatchers(
                        "/api/loai-phi/**", 
                        "/api/dot-thu/**", 
                        "/api/dinh-muc-thu/**",
                        "/api/hoa-don/**", 
                        "/api/report/**",
                        "/api/invoice/**",
                        "/api/payment/**",
                        "/api/dien-nuoc/**",
                        "/api/bang-gia/**"
                ).hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT")
                // API Quản lý tòa nhà, hộ gia đình, nhân khẩu - ADMIN, MANAGER
                .requestMatchers(
                        "/api/toa-nha/**",
                        "/api/ho-gia-dinh/**",
                        "/api/nhan-khau/**",
                        "/api/tam-tru/**",
                        "/api/tam-vang/**",
                        "/api/user-toa-nha/**"
                ).hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT", "RESIDENT")
                // API Thông báo - cho phép tất cả người dùng đã đăng nhập xem
                .requestMatchers(
                        "/api/notification/**"
                ).authenticated()
                // API Phản ánh - cho phép tất cả người dùng đã đăng nhập
                .requestMatchers(
                        "/api/phan-anh/**"
                ).authenticated()
                // Resident portal - cho phép RESIDENT truy cập
                .requestMatchers(
                        "/api/resident/**"
                ).hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT", "RESIDENT")
                // API Admin (Backup, User management) - chỉ ADMIN
                .requestMatchers(
                        "/api/admin/**",
                        "/api/backup/**",
                        "/api/user/**"
                ).hasRole("ADMIN")
                // Các route còn lại mặc định yêu cầu xác thực (PreAuthorize sẽ kiểm tra chi tiết)
                .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cho phép Frontend (React/Vue) gọi vào
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); 
        
        // Cho phép các method (bao gồm PATCH)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Cho phép các header quan trọng (đặc biệt là Authorization để gửi Token)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        
        // Cho phép gửi credentials (cookie, authorization header)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
