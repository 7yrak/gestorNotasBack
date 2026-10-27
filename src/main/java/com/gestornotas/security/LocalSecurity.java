package com.gestornotas.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class LocalSecurity {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain localChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(access -> access
                .requestMatchers("/auth/csrf", "/auth/info", "/auth/login").permitAll()
                .requestMatchers("/api/users", "/api/users/**").denyAll()
                .anyRequest().authenticated());
        http.csrf(csrf -> csrf.csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));
        http.formLogin(login -> login
                .loginProcessingUrl("/auth/login")
                .successHandler((request, response, auth) -> response.setStatus(204))
                .failureHandler((request, response, error) -> response.setStatus(401)));
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.requestCache(cache -> cache.requestCache(new NullRequestCache()));
        http.logout(logout -> logout.logoutUrl("/auth/logout")
                .logoutSuccessHandler((request, response, auth) -> response.setStatus(204)));
        http.exceptionHandling(errors -> errors.authenticationEntryPoint(
                (request, response, error) -> response.setStatus(401)));
        http.addFilterBefore(new IdentityFilter(), AuthorizationFilter.class);
        return http.build();
    }

    static final class IdentityFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain chain)
                throws java.io.IOException, jakarta.servlet.ServletException {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof LocalAccount.Principal owner) {
                request.setAttribute("notes.user-id", owner.userId());
            } else if (auth != null && auth.isAuthenticated()
                    && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                response.setStatus(401);
                return;
            }
            chain.doFilter(request, response);
        }
    }
}
