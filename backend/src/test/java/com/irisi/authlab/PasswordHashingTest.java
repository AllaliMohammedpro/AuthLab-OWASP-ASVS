package com.irisi.authlab;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
public class PasswordHashingTest {
 @Test void doesNotTruncateLongPasswords(){
  var encoder=Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  String original="A".repeat(100)+"X";
  String different="A".repeat(100)+"Y";
  String hashed=encoder.encode(original);
  assertTrue(encoder.matches(original,hashed));
  assertFalse(encoder.matches(different,hashed));
 }
}
