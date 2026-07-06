package com.upc.mind_health.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class G6_MH_SecurityConfig {

    private final G6_MH_JwtFilter jwtFilter; //
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 0. Preflight CORS: el navegador nunca envía el header Authorization
                        // en OPTIONS, así que debe permitirse sin autenticación
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 1. Endpoints Públicos (Acceso libre para Login, Registro y Swagger)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                "/api-docs/**", "/api/politicas/**").permitAll()

                        //2. Endpoints exclusivos para el rol PROFESIONAL
                        .requestMatchers("/api/terapia-ia/profesional/**").hasAuthority("ROLE_PROFESIONAL")

                        // 3. Endpoints exclusivos para el rol PACIENTE
                        .requestMatchers("/api/terapia-ia/paciente/**","/api/analisis-emocional/**",
                                "/api/emociones/**", "/api/privacidad/**", "/api/seguridad/**",
                                "/api/pagos-suscripciones/**").hasAuthority("ROLE_PACIENTE")

                        // 4. Endpoints generales de Usuario: Requieren estar autenticado
                        .requestMatchers("/api/usuario/**").authenticated()

                        .anyRequest().authenticated()
                );

        // Filtro personalizado JWT antes del filtro nativo de Spring
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}