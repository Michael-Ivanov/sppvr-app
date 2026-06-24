package com.cardio.sppvr.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Утилитарный класс для отображения стандартных диалоговых окон
 * трёх типов: ошибка, информация и предупреждение.
 * Все окна имеют осмысленные заголовки и пиктограммы.
 */
public class MessageDialog {

    private MessageDialog() {}

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message,
                "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message,
                "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message,
                "Предупреждение", JOptionPane.WARNING_MESSAGE);
    }
}
