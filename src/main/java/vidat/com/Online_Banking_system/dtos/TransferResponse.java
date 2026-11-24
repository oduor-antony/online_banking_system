package vidat.com.Online_Banking_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import vidat.com.Online_Banking_system.entities.Account;
import vidat.com.Online_Banking_system.entities.Status;
import vidat.com.Online_Banking_system.entities.Transfer;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TransferResponse {
    private Long transferId;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private BigDecimal fromBalanceAfter;
    private Status status;

    public static TransferResponse from(Transfer transfer, Account from) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getFromAccount().getId(),
                transfer.getToAccount().getId(),
                transfer.getAmount(),
                from.getBalance(),
                transfer.getStatus()
        );
    }
}
