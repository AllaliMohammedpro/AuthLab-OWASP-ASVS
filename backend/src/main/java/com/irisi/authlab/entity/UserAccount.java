package com.irisi.authlab.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="user_accounts",uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class UserAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=254) private String email;
 @Column(nullable=false,length=80) private String displayName;
 @Column(nullable=false,length=255) private String passwordHash;
 @Column(nullable=false,updatable=false) private Instant createdAt;
 protected UserAccount() {}
 public UserAccount(String email,String displayName,String passwordHash){this.email=email;this.displayName=displayName;this.passwordHash=passwordHash;this.createdAt=Instant.now();}
 @PrePersist void onCreate(){if(createdAt==null)createdAt=Instant.now();}
 public Long getId(){return id;} public String getEmail(){return email;} public String getDisplayName(){return displayName;}
 public String getPasswordHash(){return passwordHash;} public Instant getCreatedAt(){return createdAt;}
 public void setPasswordHash(String passwordHash){this.passwordHash=passwordHash;}
}
