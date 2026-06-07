package com.epam.gymcrmspringboot.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UsernamePasswordUtil Tests")
class UsernamePasswordUtilTest {

    private String chars;

    private int passwordLength;

    private UsernamePasswordUtil usernamePasswordUtil;

    @BeforeEach
    void setUp() {
        Map<String, Object> root = loadTestConfig();
        Map<String, Object> security = asMap(root.get("security"));
        Map<String, Object> password = asMap(security.get("password"));
        chars = String.valueOf(password.get("chars"));
        passwordLength = Integer.parseInt(String.valueOf(password.get("length")));
        usernamePasswordUtil = new UsernamePasswordUtil(chars, passwordLength);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalStateException("Invalid test configuration structure");
        }
        return (Map<String, Object>) map;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadTestConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application-test.yml")) {
            if (input == null) {
                throw new IllegalStateException("application-test.yml was not found on the test classpath");
            }
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(input);
            if (!(loaded instanceof Map<?, ?> map)) {
                throw new IllegalStateException("application-test.yml does not contain a valid YAML object");
            }
            return (Map<String, Object>) map;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load test configuration from application-test.yml", ex);
        }
    }

    @Test
    @DisplayName("Should generate unique username for new first-last name combination")
    void testGenerateUsernameForNewUser() {
        // Arrange
        Set<String> existingUsernames = new HashSet<>();
        String firstName = "John";
        String lastName = "Doe";

        // Act
        String username = usernamePasswordUtil.generateUsername(firstName, lastName, existingUsernames);

        // Assert
        assertEquals("John.Doe", username);
    }

    @Test
    @DisplayName("Should append suffix to username when base username already exists")
    void testGenerateUsernameWithExistingUsername() {
        // Arrange
        Set<String> existingUsernames = new HashSet<>();
        existingUsernames.add("John.Doe");
        String firstName = "John";
        String lastName = "Doe";

        // Act
        String username = usernamePasswordUtil.generateUsername(firstName, lastName, existingUsernames);

        // Assert
        assertEquals("John.Doe1", username);
    }

    @Test
    @DisplayName("Should increment suffix when multiple duplicate usernames exist")
    void testGenerateUsernameWithMultipleDuplicates() {
        // Arrange
        Set<String> existingUsernames = new HashSet<>();
        existingUsernames.add("John.Doe");
        existingUsernames.add("John.Doe1");
        existingUsernames.add("John.Doe2");
        String firstName = "John";
        String lastName = "Doe";

        // Act
        String username = usernamePasswordUtil.generateUsername(firstName, lastName, existingUsernames);

        // Assert
        assertEquals("John.Doe3", username);
    }

    @Test
    @DisplayName("Should generate password with correct length")
    void testGeneratePasswordLength() {
        // Act
        String password = usernamePasswordUtil.generatePassword();

        // Assert
        assertEquals(passwordLength, password.length());
    }

    @Test
    @DisplayName("Should generate password containing only valid characters")
    void testGeneratePasswordContainsValidCharacters() {
        // Act
        String password = usernamePasswordUtil.generatePassword();

        // Assert
        assertNotNull(password);
        for (char c : password.toCharArray()) {
            assertTrue(chars.contains(String.valueOf(c)),
                    "Password contains invalid character: " + c);
        }
    }

    @Test
    @DisplayName("Should generate different passwords on multiple calls")
    void testGeneratePasswordRandomness() {
        // Arrange
        List<String> generated = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            generated.add(usernamePasswordUtil.generatePassword());
        }

        // Assert
        Set<String> unique = new HashSet<>(generated);
        assertTrue(unique.size() > 1,
                "Password generation appears deterministic; expected multiple distinct values");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when chars is null")
    void testConstructorThrowsExceptionForNullChars() {
        // Assert
        assertThrows(IllegalStateException.class,
                () -> new UsernamePasswordUtil(null, 10));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when chars is blank")
    void testConstructorThrowsExceptionForBlankChars() {
        // Assert
        assertThrows(IllegalStateException.class,
                () -> new UsernamePasswordUtil("   ", 10));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when passwordLength is zero")
    void testConstructorThrowsExceptionForZeroPasswordLength() {
        // Assert
        assertThrows(IllegalStateException.class,
                () -> new UsernamePasswordUtil("ABC123", 0));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when passwordLength is negative")
    void testConstructorThrowsExceptionForNegativePasswordLength() {
        // Assert
        assertThrows(IllegalStateException.class,
                () -> new UsernamePasswordUtil("ABC123", -5));
    }
}

