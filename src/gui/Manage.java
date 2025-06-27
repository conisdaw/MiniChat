package gui;

import core.Config;
import data.*;
import core.FontUtil;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.*;

public class Manage extends JFrame {
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JList<String> bannedWordsList;
    private DefaultListModel<String> bannedWordsListModel;
    private Timer refreshTimer;
    private Set<String> previousUsers = new HashSet<>();
    private Font customFont;
    private JTextField bannedWordTextField;

    public Manage() {
        super("管理员控制面板");
        setSize(800, 600);
        java.net.URL iconUrl = getClass().getResource(Config.FAVICON_PATH);
        setIconImage(Toolkit.getDefaultToolkit().getImage(iconUrl));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeAndSave();
            }
        });
        setLocationRelativeTo(null);

        // 加载自定义字体
        customFont = FontUtil.loadCustomFont(14f);

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // 用户管理面板
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(createUserPanel(), gbc);

        // 违禁词管理面板
        gbc.gridy = 1;
        gbc.weighty = 0.5;
        mainPanel.add(createBannedWordsPanel(), gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 初始化定时刷新
        initRefreshTimer();

        setVisible(true);
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                "用户管理",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                customFont.deriveFont(Font.BOLD, 16f),
                new Color(70, 130, 180)
        ));

        // 用户表
        String[] columnNames = {"用户ID", "昵称"};
        userTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        userTable.setFont(customFont);
        userTable.getTableHeader().setFont(customFont.deriveFont(Font.BOLD));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton toggleButton = createStyledButton("切换封禁状态", new Color(70, 130, 180));
        JButton refreshButton = createStyledButton("刷新用户列表", new Color(60, 179, 113));
        JButton muteButton = createStyledButton("设置禁言", new Color(205, 92, 92));
        JButton unmuteButton = createStyledButton("解除禁言", new Color(60, 179, 113));

        toggleButton.addActionListener(e -> toggleUserBlockStatus());
        refreshButton.addActionListener(e -> refreshUsers());
        muteButton.addActionListener(e -> muteUser());
        unmuteButton.addActionListener(e -> unmuteUser());

        buttonPanel.add(toggleButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(muteButton);
        buttonPanel.add(unmuteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // 初始加载用户
        refreshUsers();

        return panel;
    }

    private JPanel createBannedWordsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                "违禁词管理",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                customFont.deriveFont(Font.BOLD, 16f),
                new Color(70, 130, 180)
        ));

        // 违禁词列表
        bannedWordsListModel = new DefaultListModel<>();
        bannedWordsList = new JList<>(bannedWordsListModel);
        bannedWordsList.setFont(customFont);
        JScrollPane scrollPane = new JScrollPane(bannedWordsList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 控制面板
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 添加面板
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        // 使用成员变量存储文本框
        bannedWordTextField = new JTextField();
        bannedWordTextField.setFont(customFont);
        controlPanel.add(bannedWordTextField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        JButton addButton = createStyledButton("添加违禁词", new Color(70, 130, 180));
        addButton.addActionListener(e -> addBannedWord(bannedWordTextField.getText()));
        controlPanel.add(addButton, gbc);

        // 删除按钮
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JButton removeButton = createStyledButton("移除选中", new Color(205, 92, 92));
        removeButton.addActionListener(e -> removeSelectedBannedWord());
        controlPanel.add(removeButton, gbc);

        panel.add(controlPanel, BorderLayout.SOUTH);

        // 初始加载违禁词
        refreshBannedWords();

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 14f));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
        return button;
    }

    private void initRefreshTimer() {
        refreshTimer = new Timer(5000, e -> {
            refreshUsers();
            refreshBannedWords();
        });
        refreshTimer.start();
    }

    private void muteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            String userId = (String) userTableModel.getValueAt(selectedRow, 0);

            // 弹出禁言时长输入框
            String minutesStr = JOptionPane.showInputDialog(
                    this,
                    "请输入禁言时长（分钟）:",
                    "用户禁言设置",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (minutesStr != null && !minutesStr.trim().isEmpty()) {
                try {
                    int minutes = Integer.parseInt(minutesStr);
                    if (minutes <= 0) {
                        JOptionPane.showMessageDialog(this, "请输入有效的分钟数", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (UserMute.muteUser(userId, minutes)) {
                        JOptionPane.showMessageDialog(this, "用户已被禁言 " + minutes + " 分钟");
                    } else {
                        JOptionPane.showMessageDialog(this, "禁言操作失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "请输入有效的数字", "输入错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择用户", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void unmuteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            String userId = (String) userTableModel.getValueAt(selectedRow, 0);

            if (UserMute.unmuteUser(userId)) {
                JOptionPane.showMessageDialog(this, "用户禁言已解除");
            } else {
                JOptionPane.showMessageDialog(this, "解禁操作失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择用户", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshUsers() {
        List<Map<String, String>> users = UserSQL.getAllUsers();
        Set<String> currentUsers = new HashSet<>();

        for (Map<String, String> user : users) {
            String userId = user.get("user_id");
            currentUsers.add(userId);

            // 添加新用户
            if (!previousUsers.contains(userId)) {
                userTableModel.addRow(new Object[]{userId, user.get("nickname")});
            }
        }

        // 移除不再存在的用户
        for (int i = userTableModel.getRowCount() - 1; i >= 0; i--) {
            String userId = (String) userTableModel.getValueAt(i, 0);
            if (!currentUsers.contains(userId)) {
                userTableModel.removeRow(i);
            }
        }

        previousUsers = currentUsers;
    }

    private void toggleUserBlockStatus() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            String userId = (String) userTableModel.getValueAt(selectedRow, 0);
            if (UserSQL.toggleUserBlockStatus(userId)) {
                JOptionPane.showMessageDialog(this, "用户状态已更新");
            } else {
                JOptionPane.showMessageDialog(this, "操作失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择用户", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshBannedWords() {
        List<String> words = Disable.getDisableWords();
        bannedWordsListModel.clear();
        for (String word : words) {
            bannedWordsListModel.addElement(word);
        }
    }

    private void addBannedWord(String word) {
        String trimmedWord = word.trim();

        if (trimmedWord.isEmpty()) {
            JOptionPane.showMessageDialog(this, "违禁词不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (Disable.addDisableWord(trimmedWord)) {
            refreshBannedWords();
            bannedWordTextField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "该词已存在", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeSelectedBannedWord() {
        int idx = bannedWordsList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "请选择违禁词", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String word = bannedWordsListModel.getElementAt(idx).trim();
        boolean removed = Disable.removeDisableWord(word);
        if (!removed) {
            JOptionPane.showMessageDialog(this, "内存删除失败", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean dbOk = DisableSQL.removeWord(word);
        if (!dbOk) {
            Disable.addDisableWord(word);
            JOptionPane.showMessageDialog(this, "数据库删除失败", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        bannedWordsListModel.remove(idx);

        JOptionPane.showMessageDialog(this, "删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
    }


    private void closeAndSave() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }

        DisableSQL.saveAll(Disable.getDisableWords());

        Map<String, List<RequestNotSent.PendingRequest>> pendingRequests = RequestNotSent.getAllPendingRequests();
        RequestNotSentSQL.saveAll(pendingRequests);

        dispose();
        System.exit(0);
    }

}