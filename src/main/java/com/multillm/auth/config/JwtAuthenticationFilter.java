package com.multillm.auth.config;

import com.multillm.auth.service.AccessTokenBlacklistService;
import com.multillm.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AccessTokenBlacklistService blacklistService;

    
    @Override
    protected void doFilterInternal(
            @NotNull  HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.trace("Processing request path: {}", path);
        final String authHeader = request.getHeader("Authorization");

        // 1. Check for JWT
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            log.trace("No Bearer token found in Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract the token
        final String jwt =  authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);
        final String jti = jwtService.extractTokenId(jwt);
        log.debug("JWT detected for user: {} jti: {}", userEmail, jti);

        //Reject if Blacklisted
        if(blacklistService.isBlacklist(jti)){
            log.warn("Rejected blacklisted token jti: {} for path: {}", jti, path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 3. Validate the token and user
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);


            if(jwtService.isTokenValid(jwt, userDetails)){
                log.trace("JWT is valid for user: {}", userEmail);

                // If token is valid, update the security context
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

            }
        }
        filterChain.doFilter(request, response);
    }
}
