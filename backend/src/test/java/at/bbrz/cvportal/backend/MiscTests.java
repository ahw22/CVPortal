package at.bbrz.cvportal.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MiscTests {

    @Test
    void printHashes() {
        PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        System.out.println(encoder.encode("admin12345"));
        System.out.println(encoder.encode("muster12345"));
    }
}
