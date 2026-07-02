package com.spkmainan.auth;

import com.spkmainan.auth.dto.LoginRequest;
import com.spkmainan.auth.dto.LoginResponse;
import com.spkmainan.security.AppUserDetails;
import com.spkmainan.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Authentication and session endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with username and password, returning a JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out (stateless JWT: the client discards the token)")
    public ResponseEntity<Void> logout() {
        // With stateless JWTs there is no server session to invalidate. Provided
        // for client symmetry; add token revocation here if ever required.
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    @Operation(summary = "Get the currently authenticated user")
    public ResponseEntity<UserResponse> profile(@AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(authService.getProfile(principal.getUsername()));
    }
}
