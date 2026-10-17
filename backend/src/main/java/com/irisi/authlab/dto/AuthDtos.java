package com.irisi.authlab.dto;
import com.irisi.authlab.entity.UserAccount;
import jakarta.validation.constraints.*;
import java.time.Instant;
public final class AuthDtos {
 private AuthDtos(){}
 public record Register(@NotBlank @Email @Size(max=254) String email,
                        @NotBlank @Size(max=80) String displayName,
                        @NotNull String password) {}
 public record Login(@NotBlank @Email @Size(max=254) String email,@NotNull String password) {}
 public record ChangePassword(@NotNull String currentPassword,@NotNull String newPassword) {}
 public record Profile(String email,String displayName,Instant createdAt){
  public static Profile from(UserAccount user){return new Profile(user.getEmail(),user.getDisplayName(),user.getCreatedAt());}
 }
 public record Message(String message) {}
}
