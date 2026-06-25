package com.cardio.sppvr.ui;

import com.cardio.sppvr.model.SystemUser;
import com.cardio.sppvr.service.UserService;
import com.cardio.sppvr.ui.components.MessageDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Панель администратора. Отображает таблицу всех пользователей,
 * позволяет добавлять новые учётные записи и разблокировать заблокированных.
 * Имеет боковое меню и верхнюю панель с информацией о текущем администраторе.
 */
public class AdminPanelForm extends JFrame {

    private final UserService  userService = new UserService();
    private final SystemUser   currentUser;
    private DefaultTableModel  tableModel;
    private JTable             userTable;

    public AdminPanelForm(SystemUser currentUser) {
        this.currentUser = currentUser;
        configureWindow();
        buildSidebar();
        buildMainArea();
        setVisible(true);
    }

    private void configureWindow() {
        setTitle("СППВР — Управление пользователями");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(13, 71, 161));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel menuTitle = new JLabel("Меню");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        menuTitle.setForeground(Color.WHITE);
        menuTitle.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        sidebar.add(menuTitle);

        for (String item : new String[]{"Пользователи", "Журнал аудита", "Настройки"}) {
            JLabel label = new JLabel(item);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setForeground(Color.WHITE);
            label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            sidebar.add(label);
        }
        sidebar.add(Box.createVerticalGlue());

        JLabel version = new JLabel("v1.0");
        version.setForeground(new Color(100, 150, 220));
        version.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        sidebar.add(version);

        add(sidebar, BorderLayout.WEST);
    }

    private void buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 247, 250));
        main.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        JLabel userInfo = new JLabel("Администратор: " + currentUser.getLogin());
        userInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topBar.add(userInfo, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Выйти");
        logoutBtn.addActionListener(e -> { new LoginForm(); dispose(); });
        topBar.add(logoutBtn, BorderLayout.EAST);
        main.add(topBar, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(245, 247, 250));
        JButton addBtn = new JButton("Добавить пользователя");
        styleButton(addBtn, new Color(21, 101, 192), Color.WHITE);
        addBtn.addActionListener(e -> showAddUserDialog());
        toolbar.add(addBtn);
        main.add(toolbar, BorderLayout.PAGE_START);

        String[] cols = {"ID", "Логин", "Роль", "Заблокирован", "Последний вход", "Создан"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        userTable = new JTable(tableModel);
        userTable.setRowHeight(28);
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        main.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setBackground(new Color(245, 247, 250));
        JButton unblockBtn = new JButton("Разблокировать");
        unblockBtn.addActionListener(e -> unblockSelected());
        actions.add(unblockBtn);
        main.add(actions, BorderLayout.SOUTH);

        add(main, BorderLayout.CENTER);
        refreshTable();
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            for (SystemUser u : userService.getAllUsers()) {
                tableModel.addRow(new Object[]{
                        u.getUserId(), u.getLogin(), u.getRole(),
                        u.isBlocked() ? "ДА" : "Нет",
                        u.getLastLogin() != null ? u.getLastLogin() : "—",
                        u.getCreatedAt()  != null ? u.getCreatedAt()  : "—"
                });
            }
        } catch (Exception e) {
            MessageDialog.showError(this, "Ошибка загрузки данных.");
        }
    }

    private void showAddUserDialog() {
        JTextField loginField  = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"user", "admin"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Логин:"));  panel.add(loginField);
        panel.add(new JLabel("Пароль:")); panel.add(passField);
        panel.add(new JLabel("Роль:"));   panel.add(roleCombo);

        int res = JOptionPane.showConfirmDialog(this, panel,
                "Добавить пользователя", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String login    = loginField.getText().trim();
        String password = new String(passField.getPassword());
        String role     = (String) roleCombo.getSelectedItem();

        if (login.isEmpty() || password.isEmpty()) {
            MessageDialog.showError(this, "Все поля обязательны.");
            return;
        }
        try {
            if (userService.createUser(login, password, role)) {
                MessageDialog.showSuccess(this, "Пользователь добавлен.");
            } else {
                MessageDialog.showError(this, "Пользователь с логином «" + login + "» уже существует.");
            }
        } catch (Exception e) {
            MessageDialog.showError(this, "Ошибка при создании пользователя.");
        }
        refreshTable();
    }

    private void unblockSelected() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            MessageDialog.showError(this, "Выберите пользователя из таблицы.");
            return;
        }
        try {
            int userId = (int) tableModel.getValueAt(row, 0);
            userService.unblockUser(userId);
            MessageDialog.showSuccess(this, "Пользователь разблокирован.");
            refreshTable();
        } catch (Exception e) {
            MessageDialog.showError(this, "Ошибка при разблокировке.");
        }
    }
}
