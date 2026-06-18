package com.epam.gymcrmspringboot.util;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Set;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UsernamePasswordUtil {

    String chars;
    int passwordLength;
    SecureRandom random = new SecureRandom();

    public UsernamePasswordUtil(
            @Value("${security.password.chars}") String chars,
            @Value("${security.password.length}") int passwordLength) {
        if (chars == null || chars.isBlank()) {
            throw new IllegalStateException("security.password.chars must not be blank");
        }
        if (passwordLength <= 0) {
            throw new IllegalStateException("security.password.length must be greater than zero");
        }
        this.chars = chars;
        this.passwordLength = passwordLength;
    }

    /**
     * Generates a unique username as FirstName.LastName.
     * Appends a numeric suffix if the base username already exists.
     * E.g.: John.Smith → John.Smith1 → John.Smith2
     */
    public String generateUsername(String firstName, String lastName, Set<String> existingUsernames) {
        String base = firstName + "." + lastName;

        if (!existingUsernames.contains(base)) {
            return base;
        }
        int suffix = 1;
        while (existingUsernames.contains(base + suffix)) {
            suffix++;
        }
        return base + suffix;
    }

    /**
     * Generates a random alphanumeric password with configurable length.
     */
    public String generatePassword() {
        StringBuilder sb = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

