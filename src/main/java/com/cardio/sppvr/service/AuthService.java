package com.cardio.sppvr.service;

import com.cardio.sppvr.dao.UserDAO;
import com.cardio.sppvr.model.SystemUser;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Сервис аутентификации. Инкапсулирует бизнес-логику:
 *   — проверку логина и пароля (bcrypt);
 *   — блокировку после 3 неудачных попыток;
 *   — блокировку при неактивности более 30 дней;
 *   — смену пароля с валидацией совпадения полей.
 */
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int INACTIVE_DAYS_BLOCK = 30;
    private final UserDAO userDAO = new UserDAO();

    /**
     * Аутентифицирует пользователя. Возвращает объект AuthResult,
     * содержащий признак успеха, пользователя и код дополнительного действия
     * (например, требование сменить пароль при первом входе).
     */
    public AuthResult authenticate(String login, String password) throws SQLException {
        SystemUser user = userDAO.findByLogin(login);

        if (user == null) {
            return AuthResult.INVALID;
        }

        // Проверка блокировки по неактивности
        if (!user.isBlocked() && user.getLastLogin() != null) {
            long days = ChronoUnit.DAYS.between(user.getLastLogin(), LocalDateTime.now());
            if (days > INACTIVE_DAYS_BLOCK) {
                userDAO.blockUser(user.getUserId());
                user.setBlocked(true);
            }
        }

        if (user.isBlocked()) {
            return AuthResult.BLOCKED;
        }

        // Проверка пароля
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            int newAttempts = user.getFailedAttempts() + 1;
            userDAO.updateFailedAttempts(user.getUserId(), newAttempts);
            if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                userDAO.blockUser(user.getUserId());
                return AuthResult.BLOCKED;
            }
            return AuthResult.INVALID;
        }

        // Успешный вход
        userDAO.updateFailedAttempts(user.getUserId(), 0);
        userDAO.updateLastLogin(user.getUserId(), LocalDateTime.now());

        if (!user.isPasswordChanged()) {
            return new AuthResult(true, user, "CHANGE_PASSWORD");
        }
        return new AuthResult(true, user, null);
    }

    /**
     * Смена пароля. Проверяет корректность текущего пароля,
     * совпадение нового пароля с подтверждением и минимальную длину (8 символов).
     */
    public boolean changePassword(int userId, String currentPassword,
                                   String newPassword, String confirmPassword) throws SQLException {
        SystemUser user = userDAO.findAll().stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst().orElse(null);
        if (user == null) return false;

        if (!BCrypt.checkpw(currentPassword, user.getPasswordHash())) return false;
        if (!newPassword.equals(confirmPassword)) return false;
        if (newPassword.length() < 8) return false;

        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        userDAO.changePassword(userId, newHash);
        return true;
    }

    // ──────────────────────────────────────────────

    /** Результат аутентификации. */
    public static class AuthResult {
        public static final AuthResult INVALID  = new AuthResult(false, null, null);
        public static final AuthResult BLOCKED  = new AuthResult(false, null, null);

        private final boolean success;
        private final SystemUser user;
        private final String action;

        public AuthResult(boolean success, SystemUser user, String action) {
            this.success = success;
            this.user = user;
            this.action = action;
        }

        public boolean isSuccess()           { return success; }
        public SystemUser getUser()          { return user; }
        public String getAction()            { return action; }
    }

    // ──────────────────────────────────────────────

    /** Обёртка над org.mindrot.jbcrypt.BCrypt для изоляции зависимости. */
    static class BCrypt {
        static boolean checkpw(String plain, String hashed)  { return org.mindrot.jbcrypt.BCrypt.checkpw(plain, hashed); }
        static String  hashpw(String plain, String salt)     { return org.mindrot.jbcrypt.BCrypt.hashpw(plain, salt); }
        static String  gensalt()                             { return org.mindrot.jbcrypt.BCrypt.gensalt(); }
    }
}
