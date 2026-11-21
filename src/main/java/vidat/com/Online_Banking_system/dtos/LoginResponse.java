package vidat.com.Online_Banking_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class LoginResponse {

    private final String accessToken;
    private final String message;       // always "Bearer"
    public static LoginResponse of(String token, String message) {
        return new LoginResponse(token, message);
    }
}