package com.irisi.authlab.repository;
import com.irisi.authlab.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserAccountRepository extends JpaRepository<UserAccount,Long> {
 Optional<UserAccount> findByEmail(String email);
 boolean existsByEmail(String email);
}
