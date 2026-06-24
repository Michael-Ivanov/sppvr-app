package com.cardio.sppvr;

import com.cardio.sppvr.ui.LoginForm;
import javax.swing.*;

/**
 * Точка входа в приложение СППВР.
 * Устанавливает системный Look & Feel и запускает форму авторизации.
 */
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Не удалось установить Look & Feel: " + e.getMessage());
            }
            new LoginForm();
        });
    }
}
