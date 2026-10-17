package com.irisi.authlab.controller;
import org.springframework.security.core.AuthenticationException;
import com.irisi.authlab.dto.AuthDtos.*;
import com.irisi.authlab.entity.UserAccount;
import com.irisi.authlab.exception.ApiException;
import com.irisi.authlab.repository.UserAccountRepository;
import com.irisi.authlab.security.LoginRateLimiter;
import com.irisi.authlab.service.PasswordPolicy;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UserAccountRepository users;
 private final PasswordEncoder passwordEncoder;
 private final PasswordPolicy passwordPolicy;
 private final AuthenticationManager authenticationManager;
 private final SecurityContextRepository contextRepository;
 private final LoginRateLimiter limiter;
 public AuthController(UserAccountRepository users,PasswordEncoder passwordEncoder,PasswordPolicy passwordPolicy,
   AuthenticationManager authenticationManager,SecurityContextRepository contextRepository,LoginRateLimiter limiter){
  this.users=users;this.passwordEncoder=passwordEncoder;this.passwordPolicy=passwordPolicy;
  this.authenticationManager=authenticationManager;this.contextRepository=contextRepository;this.limiter=limiter;
 }
 private String canonicalEmail(String email){return email.trim().toLowerCase(Locale.ROOT);}
 @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken token){return Map.of("token",token.getToken(),"headerName",token.getHeaderName());}
 @PostMapping("/register") public ResponseEntity<Message> register(@Valid @RequestBody Register req){
  passwordPolicy.validate(req.password());
  String email=canonicalEmail(req.email());
  if(users.existsByEmail(email))throw new ApiException(HttpStatus.CONFLICT,"Impossible de créer ce compte avec cette adresse.");
  try{users.saveAndFlush(new UserAccount(email,req.displayName().trim(),passwordEncoder.encode(req.password())));}
  catch(org.springframework.dao.DataIntegrityViolationException e){throw new ApiException(HttpStatus.CONFLICT,"Impossible de créer ce compte avec cette adresse.");}
  return ResponseEntity.status(HttpStatus.CREATED).body(new Message("Compte créé. Vous pouvez maintenant vous connecter."));
 }
 @PostMapping("/login") public Profile login(@Valid @RequestBody Login req,HttpServletRequest request,HttpServletResponse response){
  String email=canonicalEmail(req.email());
  if(limiter.blocked(request,email))throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,"Trop de tentatives. Réessayez dans quelques minutes.");
  Authentication authentication;
  try{authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,req.password()));}
  catch(AuthenticationException ex){limiter.failed(request,email);throw new ApiException(HttpStatus.UNAUTHORIZED,"Identifiant ou mot de passe incorrect.");}
  limiter.succeeded(request,email);
  // Rotation explicite de l'identifiant de session après la connexion (anti-session-fixation).
  if(request.getSession(false)!=null)request.changeSessionId();
  SecurityContext context=SecurityContextHolder.createEmptyContext();
  context.setAuthentication(authentication); SecurityContextHolder.setContext(context);
  contextRepository.saveContext(context,request,response);
  return Profile.from(users.findByEmail(email).orElseThrow());
 }
 @GetMapping("/me") public Profile me(Authentication auth){return Profile.from(currentUser(auth));}
 private UserAccount currentUser(Authentication auth){
  if(auth==null)throw new ApiException(HttpStatus.UNAUTHORIZED,"Authentification nécessaire.");
  return users.findByEmail(auth.getName()).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"Compte introuvable."));
 }
 @PostMapping("/change-password") public Message changePassword(@Valid @RequestBody ChangePassword req,Authentication auth){
  UserAccount user=currentUser(auth);
  if(!passwordEncoder.matches(req.currentPassword(),user.getPasswordHash()))throw new ApiException(HttpStatus.BAD_REQUEST,"Mot de passe actuel incorrect.");
  passwordPolicy.validate(req.newPassword());
  if(passwordEncoder.matches(req.newPassword(),user.getPasswordHash()))throw new ApiException(HttpStatus.BAD_REQUEST,"Le nouveau mot de passe doit être différent de l'ancien.");
  user.setPasswordHash(passwordEncoder.encode(req.newPassword()));users.save(user);
  return new Message("Mot de passe modifié avec succès.");
 }
 @PostMapping("/logout") public Message logout(HttpServletRequest request,HttpServletResponse response){
  HttpSession session=request.getSession(false);
  if(session!=null)session.invalidate();
  SecurityContextHolder.clearContext();
  response.addHeader(HttpHeaders.SET_COOKIE,"JSESSIONID=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
  return new Message("Vous êtes déconnecté.");
 }
}
