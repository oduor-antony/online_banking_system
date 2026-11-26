package vidat.com.Online_Banking_system.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vidat.com.Online_Banking_system.dtos.TransferRequest;
import vidat.com.Online_Banking_system.dtos.TransferResponse;
import vidat.com.Online_Banking_system.services.TransferFacade;
import vidat.com.Online_Banking_system.services.TransferService;


@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;
    private final TransferFacade transferFacade;


    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferFacade.transfer(request, authentication.getName()));
    }
}
