package vidat.com.Online_Banking_system.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vidat.com.Online_Banking_system.dtos.*;
import vidat.com.Online_Banking_system.services.AccountService;
import vidat.com.Online_Banking_system.services.TransferService;


@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final TransferService transferService;

    @PostMapping("/creation")
    public ResponseEntity<AccountCreationResponse> accountCreation(
            Authentication authentication, @Valid @RequestBody CreateAccountRequest request){
        AccountCreationResponse response=accountService.createAccount(authentication.getName(),request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<AccountCreationResponse> getAccount(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(accountService.getAccount(id, authentication.getName()));
    }

    @PostMapping("{id}/deposite")
    public ResponseEntity<DepositeResponse> deposite(@PathVariable Long id, Authentication authentication,
                                                     @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(accountService.deposite(authentication.getName(), id, request.getAmount()));
    }

    @GetMapping("{id}/transfers")
    public PagedModel<TransferHistoryItem> history(@PathVariable Long id,
                                                   Authentication authentication,
                                                   @PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(transferService.history(id, authentication.getName(), pageable));
    }
}
