package vidat.com.Online_Banking_system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import vidat.com.Online_Banking_system.dtos.TransferRequest;
import vidat.com.Online_Banking_system.dtos.TransferResponse;
import vidat.com.Online_Banking_system.entities.Account;
import vidat.com.Online_Banking_system.exceptions.InsufficientFundsException;
import vidat.com.Online_Banking_system.services.TransferFacade;
import vidat.com.Online_Banking_system.services.TransferHistoryService;
import vidat.com.Online_Banking_system.services.TransferService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TransferFacadeTest {

    @Mock
    TransferService transferService;
    @Mock
    TransferHistoryService historyService;

    private TransferFacade facade;
    private final TransferRequest req = new TransferRequest(1L, 2L, new BigDecimal("10.00"));

    @BeforeEach
    void setUp() {
        facade = new TransferFacade(transferService, historyService) {
            @Override protected void backoff(int attempt) { /* no sleeping in tests */ }
        };
    }

    @Test
    void retriesOnConflictThenSucceeds() {
        TransferResponse ok = mock(TransferResponse.class);
        when(transferService.transfer(req, "alice"))
                .thenThrow(new ObjectOptimisticLockingFailureException(Account.class, 1L))
                .thenThrow(new ObjectOptimisticLockingFailureException(Account.class, 1L))
                .thenReturn(ok);

        assertSame(ok, facade.transfer(req, "alice"));
        verify(transferService, times(3)).transfer(req, "alice");
        verifyNoInteractions(historyService);
    }

    @Test
    void givesUpAfterMaxAttempts() {
        when(transferService.transfer(req, "alice"))
                .thenThrow(new ObjectOptimisticLockingFailureException(Account.class, 1L));

        assertThrows(ConcurrencyFailureException.class, () -> facade.transfer(req, "alice"));
        verify(transferService, times(TransferFacade.MAX_ATTEMPTS)).transfer(req, "alice");
        verify(historyService).recordFailure(eq(req), anyString());
    }

    @Test
    void doesNotRetryBusinessErrors() {
        when(transferService.transfer(req, "alice")).thenThrow(new InsufficientFundsException("Insufficient funds"));

        assertThrows(InsufficientFundsException.class, () -> facade.transfer(req, "alice"));
        verify(transferService, times(1)).transfer(req, "alice");
        verify(historyService).recordFailure(req, "Insufficient funds");
    }
}