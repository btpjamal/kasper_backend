package dev.jamal.kasper_backend.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public Map<String, String> test(
            Authentication authentication
    ) {

        return Map.of(
                "message", "Você está autenticado!",
                "user", authentication.getName()
        );
    }
}
