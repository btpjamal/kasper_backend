package dev.jamal.kasper_backend.Controller;

import dev.jamal.kasper_backend.Security.ApiTokenPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ApiTestController {

    @GetMapping("/test")
    public Map<String, Object> test(
            Authentication authentication
    ) {

        ApiTokenPrincipal principal =
                (ApiTokenPrincipal)
                        authentication.getPrincipal();

        return Map.of(
                "message",
                "API Token autenticado com sucesso",

                "userId",
                principal.userId(),

                "tokenId",
                principal.tokenId(),

                "email",
                principal.email()
        );
    }
}