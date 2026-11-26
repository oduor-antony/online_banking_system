package vidat.com.Online_Banking_system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vidat.com.Online_Banking_system.dtos.TransferHistoryItem;
import vidat.com.Online_Banking_system.dtos.TransferRequest;
import vidat.com.Online_Banking_system.dtos.TransferResponse;
import vidat.com.Online_Banking_system.entities.Account;
import vidat.com.Online_Banking_system.entities.Transfer;
import vidat.com.Online_Banking_system.exceptions.AccountNotFoundException;
import vidat.com.Online_Banking_system.exceptions.InsufficientFundsException;
import vidat.com.Online_Banking_system.repositories.AccountRepository;
import vidat.com.Online_Banking_system.repositories.TransferRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransferResponse transfer(TransferRequest request, String username) {
        BigDecimal amount = request.getAmount();
        Long fromId = request.getFromAccountId();
        Long toId = request.getToAccountId();

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // Lock in a fixed order (lowest id first) so opposite transfers can't deadlock
        Account first = lockAccount(Math.min(fromId, toId));
        Account second = lockAccount(Math.max(fromId, toId));
        Account from = fromId.equals(first.getId()) ? first : second;
        Account to = toId.equals(first.getId()) ? first : second;

        if (!from.getUsers().getUserName().equals(username)) {
            throw new AccessDeniedException("You do not own the source account");
        }
        if (!from.getCurrency().equals(to.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        from.debit(amount);
        to.credit(amount);
        Transfer saved = transferRepository.save(Transfer.completed(from, to, amount));
        return TransferResponse.from(saved, from);
    }

    @Transactional(readOnly = true)
    public Page<TransferHistoryItem> history(Long accountId, String userName, Pageable pageable) {
        accountRepository.findById(accountId)
                .filter(a -> a.getUsers().getUserName().equals(userName))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        // findHistory has its own ORDER BY, so ignore any client-supplied sort
        Pageable paging = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return transferRepository.findHistory(accountId, paging).map(t -> new TransferHistoryItem(
                t.getId(),
                t.getFromAccount().getId(),
                t.getToAccount().getId(),
                t.getFromAccount().getId().equals(accountId) ? "DEBIT" : "CREDIT",
                t.getAmount(),
                t.getStatus(),
                t.getFailure_reason(),
                t.getCreated_at()));
    }

    private Account lockAccount(Long id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountNotFoundException("Account " + id + " not found"));
    }
}