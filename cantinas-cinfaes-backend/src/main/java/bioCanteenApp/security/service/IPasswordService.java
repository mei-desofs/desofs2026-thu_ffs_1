package bioCanteenApp.security.service;

import bioCanteenApp.users.domain.User;

public interface IPasswordService {
    void validatePasswordStrength(String password);

    void validateHistoricalPasswords(User userId, String newPassword);

    void changePassword(User user, String currentPassword, String newPassword);

    void applyNewPassword(User user, String newPassword);

    boolean isPasswordExpired(User user);

    void sendPasswordResetEmail(String email);

    String generateSupplierSetupToken(User user);
}
