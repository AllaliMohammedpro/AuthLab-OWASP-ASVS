package com.irisi.authlab.config;
import com.irisi.authlab.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.*;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.config.http.SessionCreationPolicy;
import java.io.IOException;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Bean public PasswordEncoder passwordEncoder(){return Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();}
 @Bean public UserDetailsService userDetailsService(UserAccountRepository repo){
  return email->repo.findByEmail(email).map(u->User.withUsername(u.getEmail()).password(u.getPasswordHash()).roles("USER").build())
   .orElseThrow(()->new UsernameNotFoundException("Utilisateur inconnu"));
 }
 @Bean public AuthenticationManager authenticationManager(org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config) throws Exception {return config.getAuthenticationManager();}
 @Bean public SecurityContextRepository securityContextRepository(){return new HttpSessionSecurityContextRepository();}
 @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http,SecurityContextRepository repository) throws Exception {
  http.authorizeHttpRequests(auth->auth
    .requestMatchers(HttpMethod.GET,"/api/auth/csrf").permitAll()
    .requestMatchers(HttpMethod.POST,"/api/auth/register","/api/auth/login").permitAll()
    .requestMatchers("/api/**").authenticated()
    .anyRequest().denyAll())
   .csrf(csrf->csrf.csrfTokenRepository(new HttpSessionCsrfTokenRepository()))
   .securityContext(context->context.securityContextRepository(repository))
   .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
   .formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
   .logout(AbstractHttpConfigurer::disable)
   .exceptionHandling(errors->errors
     .authenticationEntryPoint((request,response,e)->write(response,401,"Authentification nécessaire."))
     .accessDeniedHandler((request,response,e)->write(response,403,"Accès refusé ou jeton CSRF invalide.")));
  return http.build();
 }
 private static void write(HttpServletResponse response,int code,String message)throws IOException {
  response.setStatus(code);response.setCharacterEncoding("UTF-8");response.setContentType("application/json");
  response.getWriter().write("{\"message\":\""+message+"\"}");
 }
}
