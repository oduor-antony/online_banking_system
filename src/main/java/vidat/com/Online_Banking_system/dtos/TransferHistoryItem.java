package vidat.com.Online_Banking_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vidat.com.Online_Banking_system.entities.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferHistoryItem {
    private Long id;
    private Long fromAccountId;
    private Long toAccountId;
    private String direction;   // DEBIT if this account sent it, CREDIT if it received it
    private BigDecimal amount;
    private Status status;
    private String failureReason;
    private LocalDateTime createdAt;
}