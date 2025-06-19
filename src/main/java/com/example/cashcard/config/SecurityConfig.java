package com.example.cashcard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> request
                .requestMatchers("/").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/cashcards/**")
                .hasRole("CARD-OWNER").anyRequest().authenticated()) // enable RBAC role base access control:.
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); //️ for H2 console
        ;
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder){
        User.UserBuilder users = User.builder();
        UserDetails sarah = users.username("sarah1")
                .password(passwordEncoder.encode("abc123"))
                .roles("CARD-OWNER") // this must be enabled in the SecurityFilterChain
                .build();
        UserDetails hankOwnsNoCards = users
                .username("hank-owns-no-cards")
                .password(passwordEncoder.encode("qrs456"))
                .roles("NON-OWNER")
                .build();
        UserDetails kumar = users
                .username("kumar2")
                .password(passwordEncoder.encode("xyz789"))
                .roles("CARD-OWNER")
                .build();
        return new InMemoryUserDetailsManager(sarah,hankOwnsNoCards,kumar);
    }
}