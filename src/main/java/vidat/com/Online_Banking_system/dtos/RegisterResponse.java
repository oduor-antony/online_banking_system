package vidat.com.Online_Banking_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vidat.com.Online_Banking_system.entities.Role;
import vidat.com.Online_Banking_system.entities.Users;

@Getter
@AllArgsConstructor
public class RegisterResponse {

    private final Long id;
    private final String username;
    private final Role role;
    private final String message;

    public static RegisterResponse from(Users user) {
        return new RegisterResponse(
                user.getId(),
                user.getUserName(),
                user.getRole(),
                "User registered successfully");
    }
}