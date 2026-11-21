package vidat.com.Online_Banking_system.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vidat.com.Online_Banking_system.entities.Account;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor

public class AccountCreationResponse {
    private final Long id;
    private final String number;
    private final String currency;
    private final BigDecimal balance;
    private final Instant createdAt;

    public static AccountCreationResponse from(Account a) {
        return new AccountCreationResponse(
                a.getId(),
                a.getNumber(),
                a.getCurrency(),
                a.getBalance(),
                a.getCreatedAt());
    }
}
