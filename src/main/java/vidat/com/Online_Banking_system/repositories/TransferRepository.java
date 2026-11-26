package vidat.com.Online_Banking_system.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vidat.com.Online_Banking_system.entities.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    @Query("""
           select t from Transfer t
           where t.fromAccount.id = :accountId or t.toAccount.id = :accountId
           order by t.created_at desc
           """)
    Page<Transfer> findHistory(@Param("accountId") Long accountId, Pageable pageable);}
