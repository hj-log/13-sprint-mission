package com.sprint.mission.discodeit.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JwtLogoutHandler implements LogoutHandler {

    private static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";


    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {

        ResponseCookie expriedCookie =
                ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .sameSite("Lax")
                        .maxAge(Duration.ZERO)
                        .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expriedCookie.toString());

    }
}
