package vidat.com.Online_Banking_system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vidat.com.Online_Banking_system.entities.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByNumber(String number);


}
