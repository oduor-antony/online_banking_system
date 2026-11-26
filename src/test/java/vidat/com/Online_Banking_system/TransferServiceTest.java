package vidat.com.Online_Banking_system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import vidat.com.Online_Banking_system.dtos.TransferRequest;
import vidat.com.Online_Banking_system.entities.Account;
import vidat.com.Online_Banking_system.entities.Status;
import vidat.com.Online_Banking_system.entities.Transfer;
import vidat.com.Online_Banking_system.entities.Users;
import vidat.com.Online_Banking_system.exceptions.AccountNotFoundException;
import vidat.com.Online_Banking_system.exceptions.InsufficientFundsException;
import vidat.com.Online_Banking_system.repositories.AccountRepository;
import vidat.com.Online_Banking_system.repositories.TransferRepository;
import vidat.com.Online_Banking_system.services.TransferService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final String USER = "alice";

    @Mock
    AccountRepository accountRepo;
    @Mock
    TransferRepository transferRepo;
    @InjectMocks
    TransferService service;

    private Account from;
    private Account to;

    @BeforeEach
    void setUp() {
        Users owner = new Users();
        owner.setUserName(USER);

        from = Account.open(owner, "ACC-1", "KES");
        from.credit(new BigDecimal("100.00"));
        to = Account.open(owner, "ACC-2", "KES");
        to.credit(new BigDecimal("20.00"));
    }

    private void stubBothAccounts() {
        lenient().when(accountRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(from));
        lenient().when(accountRepo.findByIdForUpdate(2L)).thenReturn(Optional.of(to));
    }

    // 5.1 overdraft
    @Test
    void rejectsDebitLargerThanBalance() {
        stubBothAccounts();

        assertThrows(InsufficientFundsException.class,
                () -> service.transfer(new TransferRequest(1L, 2L, new BigDecimal("150.00")), USER));

        verify(transferRepo, never()).save(any());
        assertEquals(0, new BigDecimal("100.00").compareTo(from.getBalance()));
        assertEquals(0, new BigDecimal("20.00").compareTo(to.getBalance()));
    }

    // 5.3 same account
    @Test
    void rejectsSameAccount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(new TransferRequest(1L, 1L, BigDecimal.TEN), USER));
        verifyNoInteractions(accountRepo);
    }

    // 5.3 non-positive amounts
    @Test
    void rejectsZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(new TransferRequest(1L, 2L, BigDecimal.ZERO), USER));
        verifyNoInteractions(accountRepo);
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(new TransferRequest(1L, 2L, new BigDecimal("-5")), USER));
        verifyNoInteractions(accountRepo);
    }

    // 5.3 unknown accounts
    @Test
    void rejectsUnknownSourceAccount() {
        lenient().when(accountRepo.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        lenient().when(accountRepo.findByIdForUpdate(2L)).thenReturn(Optional.of(to));

        assertThrows(AccountNotFoundException.class,
                () -> service.transfer(new TransferRequest(1L, 2L, BigDecimal.TEN), USER));
        verify(transferRepo, never()).save(any());
    }

    @Test
    void rejectsUnknownDestinationAccount() {
        lenient().when(accountRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(from));
        lenient().when(accountRepo.findByIdForUpdate(2L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> service.transfer(new TransferRequest(1L, 2L, BigDecimal.TEN), USER));
        verify(transferRepo, never()).save(any());
        assertEquals(0, new BigDecimal("100.00").compareTo(from.getBalance()));
    }

    // 7.5 ownership
    @Test
    void rejectsTransferFromAnotherUsersAccount() {
        stubBothAccounts();

        assertThrows(AccessDeniedException.class,
                () -> service.transfer(new TransferRequest(1L, 2L, BigDecimal.TEN), "mallory"));

        verify(transferRepo, never()).save(any());
        assertEquals(0, new BigDecimal("100.00").compareTo(from.getBalance()));
        assertEquals(0, new BigDecimal("20.00").compareTo(to.getBalance()));
    }

    // happy path
    @Test
    void movesMoneyOnValidTransfer() {
        stubBothAccounts();
        when(transferRepo.save(any(Transfer.class))).thenAnswer(inv -> inv.getArgument(0));

        service.transfer(new TransferRequest(1L, 2L, new BigDecimal("30.00")), USER);

        assertEquals(0, new BigDecimal("70.00").compareTo(from.getBalance()));
        assertEquals(0, new BigDecimal("50.00").compareTo(to.getBalance()));

        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepo).save(captor.capture());
        assertEquals(Status.COMPLETED, captor.getValue().getStatus());
        assertEquals(0, new BigDecimal("30.00").compareTo(captor.getValue().getAmount()));
    }
}