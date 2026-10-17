package com.irisi.authlab;
import com.irisi.authlab.exception.ApiException;
import com.irisi.authlab.service.PasswordPolicy;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;
public class PasswordPolicyTest {
 private PasswordPolicy policy;
 @BeforeEach void setup()throws Exception {
  Path file=Files.createTempFile("passwords", ".txt");
  Files.write(file,IntStream.range(0,3000).mapToObj(i->"badpass"+i).toList());
  policy=new PasswordPolicy();ReflectionTestUtils.setField(policy,"blocklistPath",file.toString());policy.load();
 }
 @Test void acceptsAnyCharacterComposition(){assertDoesNotThrow(()->policy.validate("l o n g p a s s w o r d 2 0 2 6"));}
 @Test void rejectsShortPasswords(){assertThrows(ApiException.class,()->policy.validate("1234567"));}
 @Test void rejectsCommonPasswordsIgnoringCase(){assertThrows(ApiException.class,()->policy.validate("BADPASS123"));}
 @Test void acceptsLongPasswords(){assertDoesNotThrow(()->policy.validate("E".repeat(64)));}
}
