package vidat.com.Online_Banking_system.exceptions;

public class InsufficientFundsException extends RuntimeException{
   public InsufficientFundsException(String message){
        super(message);
    }
}
