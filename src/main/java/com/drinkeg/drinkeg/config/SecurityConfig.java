package com.drinkeg.drinkeg.config;

import com.drinkeg.drinkeg.jwt.CustomLogoutFilter;
import com.drinkeg.drinkeg.jwt.JWTFilter;
import com.drinkeg.drinkeg.jwt.JWTUtil;
import com.drinkeg.drinkeg.jwt.LoginFilter;
import com.drinkeg.drinkeg.oauth2.CustomSuccessHandler;
import com.drinkeg.drinkeg.repository.RefreshRepository;
import com.drinkeg.drinkeg.service.loginService.CustomOAuth2UserService;
import com.drinkeg.drinkeg.service.loginService.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;
    private final TokenService tokenService;
    private final RefreshRepository refreshRepository;

    // AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            web.ignoring()
                    .requestMatchers("/join"); // 필터를 타면 안되는 경로
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //csrf disable
        http
                .csrf(AbstractHttpConfigurer::disable);

        //From 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //HTTP Basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        //JWT 필터 추가
        http
                .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, tokenService), UsernamePasswordAuthenticationFilter.class);

        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);

        //oauth2
        http
                .oauth2Login((oauth2)->oauth2.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)

                );

        //경로별 인가 작업
        http
                .authorizeHttpRequests((authorize) -> authorize
                        //.requestMatchers("/my").authenticated()
                        .requestMatchers("/", "/join", "/login").permitAll()

                        // tasting note 인가
                        .requestMatchers("tasting-note/**").hasRole("USER")

                        // wine note 인가
                        .requestMatchers(HttpMethod.GET, "wine-note/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "wine-note/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PATCH, "wine-note/**").hasRole("USER")

                        // wine class 인가
                        .requestMatchers(HttpMethod.POST, "wine-class/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "wine-class/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "wine-class/**").hasRole("ADMIN")

                        .requestMatchers("/", "/join", "/login", "/reissue").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html/**", "/v3/api-docs/**", "/swagger-ui/index.html#/**").permitAll()
                        .anyRequest().authenticated());


        //세션 설정 : STATELESS
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}