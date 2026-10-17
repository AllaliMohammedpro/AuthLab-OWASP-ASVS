package com.irisi.authlab.service;
import com.irisi.authlab.exception.ApiException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
/** v5.0.0-6.2.1 / 6.2.4 / 6.2.5 / 6.2.8. Le mot de passe n'est jamais transformé pour l'authentification. */
@Service
public class PasswordPolicy {
 @Value("${app.security.password-blocklist}") private String blocklistPath;
 private Set<String> blockedPasswords;
 @PostConstruct public void load(){
  Path file=Path.of(blocklistPath);
  try{
   if(!Files.isRegularFile(file))throw new IllegalStateException("Liste manquante: " +file.toAbsolutePath()+". Exécuter prepare-password-list.ps1 depuis la racine du projet.");
   blockedPasswords=Files.readAllLines(file,StandardCharsets.UTF_8).stream().filter(s->!s.isBlank()).map(s->s.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
   if(blockedPasswords.size()<3000)throw new IllegalStateException("La liste doit contenir au moins 3000 mots de passe communs admissibles (actuellement " +blockedPasswords.size()+").");
  }catch(IOException ex){throw new IllegalStateException("Impossible de lire la liste de mots de passe communs",ex);}
 }
 public void validate(String password){
  if(password==null)throw new ApiException(HttpStatus.BAD_REQUEST,"Mot de passe obligatoire.");
  int length=password.codePointCount(0,password.length());
  if(length<8 || length>128)throw new ApiException(HttpStatus.BAD_REQUEST,"Le mot de passe doit contenir de 8 à 128 caractères (15 ou plus sont conseillés).");
  if(blockedPasswords.contains(password.toLowerCase(Locale.ROOT)))throw new ApiException(HttpStatus.BAD_REQUEST,"Ce mot de passe est trop courant. Choisissez une phrase secrète plus difficile à deviner.");
 }
}
