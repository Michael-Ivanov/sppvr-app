package com.cardio.sppvr.service;

import static java.util.Objects.hash;

import com.cardio.sppvr.dao.UserDAO;
import com.cardio.sppvr.model.SystemUser;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Сервис управления пользователями (CRUD + разблокировка).
 * Используется панелью администратора.
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public List<SystemUser> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    /**
     * Создаёт нового пользователя. Проверяет уникальность логина.
     * @return true — создан, false — логин уже занят.
     */
    public boolean createUser(String login, String password, String role) throws SQLException {
        if (userDAO.existsByLogin(login)) {
            return false;
        }
        SystemUser user = new SystemUser();
        user.setLogin(login);
        user.setPasswordHash(String.valueOf(Objects.hash(password)));
        user.setRole(role);
        userDAO.create(user);
        return true;
    }

    public void updateUser(SystemUser user) throws SQLException {
        userDAO.update(user);
    }

    public void unblockUser(int userId) throws SQLException {
        userDAO.unblockUser(userId);
    }

    private static String hash(String password) {
//        return org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
        return "";
    }
}
