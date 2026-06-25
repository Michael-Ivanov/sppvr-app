package com.cardio.sppvr.ui;

import com.cardio.sppvr.dao.UserDAO;
import com.cardio.sppvr.model.SystemUser;
import com.cardio.sppvr.service.AuthService;
import com.cardio.sppvr.ui.components.MessageDialog;
import com.cardio.sppvr.ui.components.PuzzleCaptcha;

import javax.swing.*;
import java.awt.*;

/**
 * Форма авторизации — точка входа в систему.
 * Содержит поля «Логин» и «Пароль», кнопку «Войти».
 * После успешной аутентификации открывает соответствующий роли интерфейс
 * или форму смены пароля при первом входе.
 */
public class LoginForm extends JFrame {

    private final JTextField     loginField    = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton        loginButton   = new JButton("Войти");
    private final AuthService    authService   = new AuthService();
    private final UserDAO        userDAO       = new UserDAO();
    private final PuzzleCaptcha  puzzleCaptcha = new PuzzleCaptcha();

    public LoginForm() {
        configureWindow();
        buildLayout();
        configureTabOrder();
        setVisible(true);
    }

    private void configureWindow() {
        setTitle("СППВР Кардиологическая клиника — Авторизация");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(450, 620));
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void buildLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel logoLabel = new JLabel("СППВР", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setForeground(new Color(21, 101, 192));
        mainPanel.add(logoLabel, gbc);

        gbc.gridy++; gbc.insets = new Insets(0, 0, 20, 0);
        JLabel subtitle = new JLabel("Поддержка принятия врачебных решений", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(117, 117, 117));
        mainPanel.add(subtitle, gbc);

        addLabeledField(mainPanel, gbc, "Логин:", loginField);
        addLabeledField(mainPanel, gbc, "Пароль:", passwordField);

        gbc.gridy++; gbc.gridwidth = 2; gbc.insets = new Insets(8, 0, 3, 0);
        JLabel captchaHint = new JLabel("Соберите изображение (кликните фрагменты 1→2→3→4):", SwingConstants.CENTER);
        captchaHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        captchaHint.setForeground(new Color(117, 117, 117));
        mainPanel.add(captchaHint, gbc);

        gbc.gridy++; gbc.insets = new Insets(0, 0, 3, 0);
        mainPanel.add(puzzleCaptcha, gbc);

        gbc.gridy++; gbc.insets = new Insets(3, 0, 10, 0);
        JButton shuffleBtn = new JButton("Перемешать");
        shuffleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        shuffleBtn.addActionListener(e -> puzzleCaptcha.shuffle());
        mainPanel.add(shuffleBtn, gbc);

        gbc.gridy++; gbc.insets = new Insets(20, 0, 15, 0);
        styleButton(loginButton, new Color(21, 101, 192), Color.WHITE);
        loginButton.addActionListener(e -> performLogin());
        mainPanel.add(loginButton, gbc);

        gbc.gridy++; gbc.insets = new Insets(0, 0, 0, 0);
        JLabel help = new JLabel("Забыли пароль? Обратитесь к администратору", SwingConstants.CENTER);
        help.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        help.setForeground(new Color(117, 117, 117));
        mainPanel.add(help, gbc);

        add(mainPanel);
    }

    private void addLabeledField(JPanel panel, GridBagConstraints gbc, String label, JTextField field) {
        gbc.gridy++; gbc.insets = new Insets(10, 0, 3, 0); gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0); gbc.gridwidth = 2;
        panel.add(field, gbc);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(0, 36));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
    }

    private void configureTabOrder() {
        loginField.setNextFocusableComponent(passwordField);
        passwordField.setNextFocusableComponent(loginButton);
    }

    private void performLogin() {
        String login    = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            MessageDialog.showError(this, "Заполните все обязательные поля.");
            return;
        }

        if (!puzzleCaptcha.isSolved()) {
            incrementFailedAttempts(login);
            MessageDialog.showError(this,
                    "Капча не пройдена. Соберите изображение, кликнув фрагменты в правильном порядке (1→2→3→4).");
            puzzleCaptcha.shuffle();
            return;
        }

        try {
            AuthService.AuthResult result = authService.authenticate(login, password);

            if (result == AuthService.AuthResult.INVALID) {
                MessageDialog.showError(this,
                        "Вы ввели неверный логин или пароль. Пожалуйста проверьте ещё раз введенные данные.");
                puzzleCaptcha.shuffle();
            } else if (result == AuthService.AuthResult.BLOCKED) {
                MessageDialog.showError(this,
                        "Вы заблокированы. Обратитесь к администратору.");
            } else if ("CHANGE_PASSWORD".equals(result.getAction())) {
                MessageDialog.showSuccess(this, "Вы успешно авторизовались.");
                new ChangePasswordForm(result.getUser().getUserId(), authService, () -> {
                    openRoleInterface(result.getUser());
                });
                dispose();
            } else if (result.isSuccess()) {
                MessageDialog.showSuccess(this, "Вы успешно авторизовались.");
                openRoleInterface(result.getUser());
                dispose();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageDialog.showError(this, "Ошибка соединения с сервером.");
        }
    }

    private void openRoleInterface(SystemUser user) {
        if ("admin".equals(user.getRole())) {
            new AdminPanelForm(user);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Добро пожаловать, " + user.getLogin() + "!\nРабочий стол врача.",
                    "СППВР", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void incrementFailedAttempts(String login) {
        if (login.isEmpty()) return;
        try {
            SystemUser user = userDAO.findByLogin(login);
            if (user != null) {
                int newAttempts = user.getFailedAttempts() + 1;
                userDAO.updateFailedAttempts(user.getUserId(), newAttempts);
                if (newAttempts >= 3) {
                    userDAO.blockUser(user.getUserId());
                }
            }
        } catch (Exception ignored) {
        }
    }
}
