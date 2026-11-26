package vidat.com.Online_Banking_system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vidat.com.Online_Banking_system.dtos.TransferRequest;
import vidat.com.Online_Banking_system.dtos.TransferResponse;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TransferFacade {
    public static final int MAX_ATTEMPTS = 5;

    private final TransferService transferService;
    private final TransferHistoryService historyService;

    // Deliberately NOT @Transactional: each attempt must run in its own transaction
    public TransferResponse transfer(TransferRequest request, String username) {
        for (int attempt = 1; ; attempt++) {
            try {
                return transferService.transfer(request, username);
            } catch (ConcurrencyFailureException ex) {
                if (attempt == MAX_ATTEMPTS) {
                    recordFailure(request, "Too much contention, please retry");
                    throw ex;
                }
                backoff(attempt);
            } catch (AccessDeniedException ex) {
                throw ex; // don't write someone else's attempt into the owner's history
            } catch (RuntimeException ex) {
                recordFailure(request, ex.getMessage());
                throw ex;
            }
        }
    }

    private void recordFailure(TransferRequest request, String reason) {
        try {
            historyService.recordFailure(request, reason);
        } catch (RuntimeException ignored) {
            // a history problem must never hide the real error
        }
    }

    protected void backoff(int attempt) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(10L * attempt, 50L * attempt));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying", e);
        }
    }
}