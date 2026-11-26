package vidat.com.Online_Banking_system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vidat.com.Online_Banking_system.dtos.AccountCreationResponse;
import vidat.com.Online_Banking_system.dtos.CreateAccountRequest;
import vidat.com.Online_Banking_system.dtos.DepositeResponse;
import vidat.com.Online_Banking_system.entities.Account;
import vidat.com.Online_Banking_system.entities.Users;
import vidat.com.Online_Banking_system.exceptions.AccountNotFoundException;
import vidat.com.Online_Banking_system.repositories.AccountRepository;
import vidat.com.Online_Banking_system.repositories.UserRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator numberGenerator;

    @Transactional
    public AccountCreationResponse createAccount(String username, CreateAccountRequest request) {
        Users owner = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        Account account = Account.open(owner, generateNumber(), request.getCurrency());

        BigDecimal initial = request.getInitialDeposit();
        if (initial != null && initial.signum() > 0) {
            account.credit(initial);
        }
        return AccountCreationResponse.from(accountRepository.save(account));
    }

    private String generateNumber() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String number = numberGenerator.next();
            if (!accountRepository.existsByNumber(number)) {
                return number;
            }
        }
        throw new IllegalStateException("Could not generate a unique account number");
    }

    @Transactional(readOnly = true)
    public AccountCreationResponse getAccount(Long id, String username) {
        return AccountCreationResponse.from(findOwnedAccount(id, username));
    }

    @Transactional
    public DepositeResponse deposite(String username, Long id, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        Account account = findOwnedAccount(id, username);
        account.credit(amount);
        return DepositeResponse.from(account, amount);
    }

    // Same 404 for a missing account and someone else's, so ids can't be probed
    private Account findOwnedAccount(Long id, String username) {
        return accountRepository.findById(id)
                .filter(a -> a.getUsers().getUserName().equals(username))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }
}