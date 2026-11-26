package vidat.com.Online_Banking_system.entities;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccount;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccount;
    @Column(nullable = false,precision = 19,scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    private String failure_reason;
    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    private LocalDateTime created_at;

    public static Transfer completed(Account from, Account to, BigDecimal amount){
        Transfer t=new Transfer();
        t.fromAccount=from;
        t.toAccount=to;
        t.amount=amount;
        t.status=Status.COMPLETED;
        return t;
    }

    public static Transfer failed(Account from, Account to, BigDecimal amount, String reason) {
        Transfer t = new Transfer();
        t.fromAccount = from;
        t.toAccount = to;
        t.amount = amount;
        t.status = Status.FAILED;
        t.failure_reason = reason;
        return t;
    }
}
