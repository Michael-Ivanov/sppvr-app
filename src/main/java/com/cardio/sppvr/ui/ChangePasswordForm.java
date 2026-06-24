package com.cardio.sppvr.ui;

import com.cardio.sppvr.service.AuthService;
import com.cardio.sppvr.ui.components.MessageDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Форма принудительной смены пароля при первом входе.
 * Содержит три поля: текущий пароль, новый пароль, подтверждение.
 * Все поля обязательны. Проверяется совпадение нового пароля с подтверждением
 * и корректность текущего пароля.
 */
public class ChangePasswordForm extends JFrame {

    private final JPasswordField currentField  = new JPasswordField(15);
    private final JPasswordField newField      = new JPasswordField(15);
    private final JPasswordField confirmField  = new JPasswordField(15);
    private final JButton        changeButton  = new JButton("Изменить пароль");

    private final int           userId;
    private final AuthService   authService;
    private final Runnable      onSuccess;

    public ChangePasswordForm(int userId, AuthService authService, Runnable onSuccess) {
        this.userId      = userId;
        this.authService = authService;
        this.onSuccess   = onSuccess;
        configureWindow();
        buildLayout();
        setVisible(true);
    }

    private void configureWindow() {
        setTitle("СППВР — Смена пароля");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(420, 320));
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void buildLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;

        JLabel title = new JLabel("Смена пароля", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(21, 101, 192));
        panel.add(title, gbc);

        addField(panel, gbc, "Текущий пароль:", currentField);
        addField(panel, gbc, "Новый пароль:",     newField);
        addField(panel, gbc, "Подтверждение:",    confirmField);

        gbc.gridy++; gbc.insets = new Insets(15, 0, 0, 0);
        changeButton.setBackground(new Color(21, 101, 192));
        changeButton.setForeground(Color.WHITE);
        changeButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        changeButton.setPreferredSize(new Dimension(0, 36));
        changeButton.addActionListener(e -> performChange());
        panel.add(changeButton, gbc);

        add(panel);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, JPasswordField field) {
        gbc.gridy++; gbc.gridwidth = 1; gbc.insets = new Insets(10, 0, 3, 0);
        panel.add(new JLabel(label), gbc);
        gbc.gridy++; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(field, gbc);
    }

    private void performChange() {
        String cur     = new String(currentField.getPassword());
        String newPass = new String(newField.getPassword());
        String conf    = new String(confirmField.getPassword());

        if (cur.isEmpty() || newPass.isEmpty() || conf.isEmpty()) {
            MessageDialog.showError(this, "Все поля обязательны для заполнения.");
            return;
        }

        try {
            boolean ok = authService.changePassword(userId, cur, newPass, conf);
            if (ok) {
                MessageDialog.showSuccess(this, "Пароль успешно изменён.");
                dispose();
                onSuccess.run();
            } else {
                MessageDialog.showError(this,
                        "Ошибка при смене пароля. Проверьте текущий пароль и совпадение нового с подтверждением.");
            }
        } catch (Exception ex) {
            MessageDialog.showError(this, "Ошибка соединения с сервером.");
        }
    }
}
