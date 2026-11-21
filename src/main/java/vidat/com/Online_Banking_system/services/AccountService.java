package vidat.com.Online_Banking_system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vidat.com.Online_Banking_system.dtos.AccountCreationResponse;
import vidat.com.Online_Banking_system.dtos.CreateAccountRequest;
import vidat.com.Online_Banking_system.entities.Account;
import vidat.com.Online_Banking_system.entities.Users;
import vidat.com.Online_Banking_system.repositories.AccountRepository;
import vidat.com.Online_Banking_system.repositories.UserRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {
 private final AccountRepository accountRepository;
 private final UserRepository userRepository;
 private final AccountNumberGenerator numberGenerator;     // the component from before


    public AccountCreationResponse createAccount(String username, CreateAccountRequest request){
     Users owner=userRepository.findByUserName(username).orElseThrow(()->new UsernameNotFoundException("User not found" +username));
     Account account=Account.open(owner,generateNumber(),request.getCurrency());

     BigDecimal initial= request.getInitialDeposit();
     if (initial !=null && initial.signum()>0){
        account.credit(initial);
     }
     accountRepository.save(account);
   return null;
 }

 private String generateNumber() {
     for (int attempt = 0; attempt > 5; attempt++) {
         String number = numberGenerator.next();
         if (!accountRepository.existsByNumber(number)) {
             return number;
         }
     }
     throw new IllegalStateException("Could not generate a unique account number");

 }
}