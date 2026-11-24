package vidat.com.Online_Banking_system.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vidat.com.Online_Banking_system.dtos.AccountCreationResponse;
import vidat.com.Online_Banking_system.dtos.CreateAccountRequest;
import vidat.com.Online_Banking_system.dtos.DepositRequest;
import vidat.com.Online_Banking_system.dtos.DepositeResponse;
import vidat.com.Online_Banking_system.services.AccountService;

import javax.security.auth.login.AccountNotFoundException;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    @PostMapping("/creation")
    public ResponseEntity<AccountCreationResponse> accountCreation(
            Authentication authentication, @Valid @RequestBody CreateAccountRequest request){
        AccountCreationResponse response=accountService.createAccount(authentication.getName(),request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<AccountCreationResponse> getAccount(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAccount(id));
    }

    @PostMapping("{id}/deposite")
    public ResponseEntity<DepositeResponse> deposite(@PathVariable Long id, Authentication authentication, @RequestBody DepositRequest request) throws AccessDeniedException, AccountNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.deposite(authentication.getName(),id,request.getAmount()));
    }
}
