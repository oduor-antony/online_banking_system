package vidat.com.Online_Banking_system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vidat.com.Online_Banking_system.dtos.TransferRequest;
import vidat.com.Online_Banking_system.entities.Transfer;
import vidat.com.Online_Banking_system.repositories.AccountRepository;
import vidat.com.Online_Banking_system.repositories.TransferRepository;

@Service
@RequiredArgsConstructor
public class TransferHistoryService {
    private final TransferRepository transferRepo;
    private final AccountRepository accountRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(TransferRequest req, String reason) {
        if (req.getFromAccountId() == null || req.getToAccountId() == null
                || !accountRepo.existsById(req.getFromAccountId())
                || !accountRepo.existsById(req.getToAccountId())
                || req.getAmount() == null) {
            return; // can't satisfy the non-null foreign keys
        }
        String trimmed = reason != null && reason.length() > 255 ? reason.substring(0, 255) : reason;
        transferRepo.save(Transfer.failed(
                accountRepo.getReferenceById(req.getFromAccountId()),
                accountRepo.getReferenceById(req.getToAccountId()),
                req.getAmount(),
                trimmed));
    }
}