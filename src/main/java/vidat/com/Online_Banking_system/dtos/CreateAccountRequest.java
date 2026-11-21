package vidat.com.Online_Banking_system.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import vidat.com.Online_Banking_system.entities.Account;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class CreateAccountRequest {
    @NotBlank(message = "currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter code such as KES or USD")
    private String currency;

    @DecimalMin(value = "0.00", message = "initial deposit cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "amount allows at most 2 decimal places")
    private BigDecimal initialDeposit;

}
