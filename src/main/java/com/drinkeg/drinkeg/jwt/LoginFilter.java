package com.drinkeg.drinkeg.jwt;

import com.drinkeg.drinkeg.dto.securityDTO.oauth2DTO.LoginResponse;
import com.drinkeg.drinkeg.dto.securityDTO.jwtDTO.PrincipalDetail;
import com.drinkeg.drinkeg.service.loginService.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        // 클라이언트 요청에서 username, password 추출
        /*String username = obtainUsername(request);
        String password = obtainPassword(request);*/

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> requestBody;

        try {
            requestBody = objectMapper.readValue(request.getInputStream(), Map.class);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to parse authentication request body", e);
        }

        String username = requestBody.get("username");
        String password = requestBody.get("password");

        // 스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        // token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }

    // 로그인 성공시 실행하는 메서드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {

        PrincipalDetail principalDetail = (PrincipalDetail) authentication.getPrincipal();

        String username = principalDetail.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String accessToken = jwtUtil.createJwt("access",username, role, 600000L);
        String refreshToken = jwtUtil.createJwt("refresh",username,role,86400000L);

        System.out.println("---------------LoginFilter------------------");

        System.out.println("accessToken  ===  " + accessToken);
        System.out.println("refreshToken == " + refreshToken);

        // 토큰을 쿠키에 저장하여 응답 (access 의 경우 추후 프론트와 협의하여 헤더에 넣어서 반환할 예정)
        response.addCookie(tokenService.createCookie("accessToken", accessToken));
        response.addCookie(tokenService.createCookie("refreshToken", refreshToken));
        response.setStatus(HttpStatus.OK.value());

        // refresh 토큰 저장
        tokenService.addRefreshToken(username, refreshToken, 86400000L);


        response.sendRedirect("http://localhost:8080/maindy");

        /*LoginResponse loginResponse = LoginResponse.builder()
                .username(username)
                .role(role)
                .accessToken(token)
                .build();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), loginResponse);

        System.out.println("token  ===  " +token);*/
        // response.sendRedirect("http://localhost:8080/main");
    }

    // 로그인 실패시 실행하는 메서드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {

        response.setStatus(401);
    }

}