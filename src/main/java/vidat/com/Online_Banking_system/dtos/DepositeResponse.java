package vidat.com.Online_Banking_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vidat.com.Online_Banking_system.entities.Account;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DepositeResponse {
    private Long accountId;
    private String accountNumber;
    private String currency;
    private BigDecimal amountDeposited;
    private BigDecimal newBalance;

    public static DepositeResponse from(Account account, BigDecimal amountDeposited) {
        return new DepositeResponse(
                account.getId(),
                account.getNumber(),
                account.getCurrency(),
                amountDeposited,
                account.getBalance()
        );
    }
}
