package vidat.com.Online_Banking_system.entities;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

//@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Getter
@Entity

public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Users users;
    @Column(nullable = false,unique = true,length = 20)
    private String number;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false,length = 3)
    private String currency;

    @Version
    private long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static Account open(Users owner, String number,String currency){
        Account account=new Account();
        account.users=owner;
        account.number=number;
        account.currency=currency;
        return account;
    }

    public void debit(BigDecimal amount){
        requiredPositive(amount);
        if(balance.compareTo(amount)<0){
            throw new IllegalStateException("Insufficient funds");
        }
        balance=balance.subtract(amount);
    }

    public void credit(BigDecimal amount){
        requiredPositive(amount);
        balance=balance.add(amount);
    }
    private static void requiredPositive(BigDecimal amount){
        if(amount==null||amount.signum()<=0){
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

}
