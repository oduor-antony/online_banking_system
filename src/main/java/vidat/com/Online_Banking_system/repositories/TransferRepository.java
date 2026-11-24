package vidat.com.Online_Banking_system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vidat.com.Online_Banking_system.entities.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
}
