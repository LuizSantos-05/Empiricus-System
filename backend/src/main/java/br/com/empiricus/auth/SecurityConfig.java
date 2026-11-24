package br.com.empiricus.auth;

import br.com.empiricus.error.ApiSecurityExceptionHandler;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ApiSecurityExceptionHandler apiSecurityExceptionHandler) throws Exception {
        return http
            // A API usa JWT e nao depende de cookies de sessao, portanto CSRF nao se aplica aqui.
                .csrf(csrf -> csrf.disable())
            // Cada requisicao precisa apresentar seu proprio token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                // Somente cadastro e login sao publicos.
                    .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(apiSecurityExceptionHandler)
                        .accessDeniedHandler(apiSecurityExceptionHandler))
            // Valida o JWT antes das regras de autorizacao do Spring Security.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // BCrypt e apropriado para senhas porque e lento e resistente a ataques de forca bruta.
        return new BCryptPasswordEncoder();
    }
}