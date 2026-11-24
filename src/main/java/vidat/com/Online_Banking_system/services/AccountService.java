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
import vidat.com.Online_Banking_system.repositories.AccountRepository;
import vidat.com.Online_Banking_system.repositories.UserRepository;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class AccountService {
 private final AccountRepository accountRepository;
 private final UserRepository userRepository;
 private final AccountNumberGenerator numberGenerator;     // the component from before

    @Transactional
    public AccountCreationResponse createAccount(String username, CreateAccountRequest request){
     Users owner=userRepository.findByUserName(username).orElseThrow(()->new UsernameNotFoundException("User not found: " +username));
     Account account=Account.open(owner,generateNumber(),request.getCurrency());

     BigDecimal initial= request.getInitialDeposit();
     if (initial !=null && initial.signum()>0){
        account.credit(initial);
     }
    Account saved= accountRepository.save(account);
   return AccountCreationResponse.from(saved);
 }

 private String generateNumber() {
     for (int attempt = 0; attempt <5; attempt++) {
         String number = numberGenerator.next();
         if (!accountRepository.existsByNumber(number)) {
             return number;
         }
     }
     throw new IllegalStateException("Could not generate a unique account number");

 }

    public AccountCreationResponse getAccount(Long id) {
        Account account=accountRepository.findById(id).orElseThrow();
        return AccountCreationResponse.from(account);
    }

    @Transactional
    public DepositeResponse deposite(String username, Long id, BigDecimal amount) throws AccessDeniedException {
        Account account= accountRepository.findById(id).orElseThrow();

        if(!account.getUsers().getUserName().equals(username)){
            throw new AccessDeniedException("You Do Not own this account");
        }
        if(amount==null && amount.signum()<=0){
            throw new IllegalArgumentException("Amount should not be zero or negative number");
        }

        account.credit(amount);
        return DepositeResponse.from(account,amount);
    }
}