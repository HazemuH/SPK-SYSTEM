package com.spkmainan.config;

import com.spkmainan.user.Role;
import com.spkmainan.user.User;
import com.spkmainan.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a demo admin account on startup for local development so the mobile app
 * can log in immediately. Disabled under the {@code prod} profile.
 *
 * <p>Demo credentials: {@code admin / password123}
 */
@Component
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) {
            return;
        }
        User admin = new User(
                "admin",
                "admin@spkmainan.test",
                "Administrator",
                passwordEncoder.encode("password123"),
                Role.ADMIN);
        userRepository.save(admin);
        log.info("Seeded demo admin user (username='admin', password='password123')");
    }
}
