package bioCanteenApp.email.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EmailDomainValidator {

    private final List<String> allowedDomains;

    public EmailDomainValidator(@Value("${app.email.allowed-domains}") String allowedDomainsConfig) {
        this.allowedDomains = Arrays.asList(allowedDomainsConfig.split(","));
    }

    public void validate(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        if (allowedDomains.stream().noneMatch(d -> d.trim().equalsIgnoreCase(domain))) {
            throw new IllegalArgumentException(
                    "Email domain '" + domain + "' is not accepted. Allowed domains: " + String.join(", ", allowedDomains));
        }
    }
}
