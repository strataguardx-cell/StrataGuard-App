package com.strataguard.server.config

import com.strataguard.server.security.FirebaseTokenFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(private val firebaseTokenFilter: FirebaseTokenFilter) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Strata search is public (no auth needed to search)
                    .requestMatchers("/api/v1/strata/search").permitAll()
                    .requestMatchers("/api/v1/strata/**").permitAll()
                    // Actuator health check
                    .requestMatchers("/actuator/health").permitAll()
                    // Evidence and incidents require authentication
                    .requestMatchers("/api/v1/evidence/**").authenticated()
                    .requestMatchers("/api/v1/incidents/**").authenticated()
                    // Everything else requires authentication
                    .anyRequest().authenticated()
            }
            .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
