package vidat.com.Online_Banking_system.services;


import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {
    private static final int BASE_LENGTH = 9;
    private final SecureRandom random=new SecureRandom();

    public String next(){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<BASE_LENGTH;i++){
            sb.append(random.nextInt(10));
        }
        sb.append(luhnCheckDigit(sb.toString()));
        return sb.toString();
    }

   private int luhnCheckDigit(String digits){
        int sum=0;
        boolean doubleIt=true;
        for(int i= digits.length()-1;i>=0;i--){
            int d=digits.charAt(i)-'0';
            if (doubleIt){
                d*=2;
                if(d>9)
                    d-=9;
            }
            sum+=d;
            doubleIt=! doubleIt;
        }
         return (10-sum%10 ) %10;
   }

   }

