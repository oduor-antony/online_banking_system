package vidat.com.Online_Banking_system.exceptions;

public class AccountNotFoundException extends RuntimeException{
     AccountNotFoundException(String message){
         super(message);
     }
}
