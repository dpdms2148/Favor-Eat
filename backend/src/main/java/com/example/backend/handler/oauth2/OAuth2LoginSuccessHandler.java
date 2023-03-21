package com.example.backend.handler.oauth2;

import com.example.backend.api.domain.users.Role;
import com.example.backend.api.domain.users.entity.Users;
import com.example.backend.api.domain.users.oauth.CustomOAuth2User;
import com.example.backend.api.domain.users.repository.UsersRepository;
import com.example.backend.api.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UsersRepository usersRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");
        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

//            String accessToken = jwtService.createAccessToken(oAuth2User.getEmail(), oAuth2User.getNickName());
            String refreshToken = jwtService.createRefreshToken();
            String accessToken = jwtService.createAccessToken(oAuth2User.getEmail(), oAuth2User.getNickName());
            // 헤더에 담아서 두개 다 던진다.
//            jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
            jwtService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);


            jwtService.sendAccessAndRefreshToken(response, accessToken, null);
            log.info("여기 들어와?");
            log.info("oAuth2User.getEmail() : " + oAuth2User.getEmail());
            Users findUser = usersRepository.findByEmail(oAuth2User.getEmail())
                            .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 유저가 없습니다."));
            log.info("findUser.getEmail() : " + findUser.getEmail());
            log.info("findUser.getToken() : " + findUser.getToken());
            findUser.authorizeUser();// TODO : 취향분석 페이지 리다이렉트 받기
            // response.sendRedirect("http://localhost:3000/auth/kakao?token=" + accessToken);
            response.sendRedirect("http://i8d205.p.ssafy.io:3000/auth/kakao?token=" + accessToken);

        } catch (Exception e) {
            throw e;
        }
    }
}
