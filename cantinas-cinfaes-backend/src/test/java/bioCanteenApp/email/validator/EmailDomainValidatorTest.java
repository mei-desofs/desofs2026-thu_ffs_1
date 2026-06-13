package bioCanteenApp.email.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailDomainValidatorTest {

    private EmailDomainValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmailDomainValidator("gmail.com,outlook.com,isep.ipp.pt,cinfaes.pt");
    }

    @Test
    void validate_AllowedDomain_ShouldPass() {
        assertDoesNotThrow(() -> validator.validate("user@gmail.com"));
        assertDoesNotThrow(() -> validator.validate("user@outlook.com"));
        assertDoesNotThrow(() -> validator.validate("user@isep.ipp.pt"));
        assertDoesNotThrow(() -> validator.validate("user@cinfaes.pt"));
    }

    @Test
    void validate_DomainCaseInsensitive_ShouldPass() {
        assertDoesNotThrow(() -> validator.validate("user@GMAIL.COM"));
        assertDoesNotThrow(() -> validator.validate("user@Gmail.Com"));
    }

    @Test
    void validate_DisallowedDomain_ShouldThrow() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("user@yahoo.com")
        );
        assertTrue(ex.getMessage().contains("yahoo.com"));
        assertTrue(ex.getMessage().contains("not accepted"));
    }

    @Test
    void validate_NullEmail_ShouldThrow() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(null)
        );
        assertEquals("Invalid email address.", ex.getMessage());
    }

    @Test
    void validate_EmailWithNoAtSign_ShouldThrow() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("notanemail")
        );
        assertEquals("Invalid email address.", ex.getMessage());
    }

    @Test
    void validate_DomainWithLeadingSpace_ShouldPass() {
        EmailDomainValidator validatorWithSpaces =
                new EmailDomainValidator(" gmail.com , outlook.com ");
        assertDoesNotThrow(() -> validatorWithSpaces.validate("user@gmail.com"));
    }
}
