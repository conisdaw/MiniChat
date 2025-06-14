package gui;

import core.Config;
import core.FontUtil;
import core.SetUserContent;
import core.UserLogical;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class LoginSystem extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private UserLogical UserLogical;
    private Font customFont;

    public LoginSystem() {
        super("登录");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 加载自定义字体
        customFont = FontUtil.loadCustomFont(14f);

        UserLogical = new UserLogical();

        // 创建卡片布局
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // 创建登录和注册面板
        loginPanel = new LoginPanel();
        registerPanel = new RegisterPanel();

        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(registerPanel, "REGISTER");

        add(cardPanel, BorderLayout.CENTER);

        // 显示登录面板
        cardLayout.show(cardPanel, "LOGIN");
    }

    class LoginPanel extends JPanel {
        private JTextField accountField;
        private JPasswordField passwordField;
        private JTextField portField;

        public LoginPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 10, 8, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // 标题
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            JLabel titleLabel = new JLabel("登录", JLabel.CENTER);
            titleLabel.setFont(customFont.deriveFont(Font.BOLD, 18f));
            add(titleLabel, gbc);

            // 账号
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            JLabel accountLabel = new JLabel("账号:");
            accountLabel.setFont(customFont);
            add(accountLabel, gbc);

            gbc.gridx = 1;
            accountField = new JTextField(20);
            accountField.setFont(customFont);
            add(accountField, gbc);

            // 密码
            gbc.gridx = 0;
            gbc.gridy = 2;
            JLabel passwordLabel = new JLabel("密码:");
            passwordLabel.setFont(customFont);
            add(passwordLabel, gbc);

            gbc.gridx = 1;
            passwordField = new JPasswordField(20);
            passwordField.setFont(customFont);
            add(passwordField, gbc);

            // 端口配置
            gbc.gridx = 0;
            gbc.gridy = 3;
            JLabel portLabel = new JLabel("端口:");
            portLabel.setFont(customFont);
            add(portLabel, gbc);

            gbc.gridx = 1;
            portField = new JTextField(20);
            portField.setFont(customFont);
            portField.setText(String.valueOf(Config.PORT));

            // 设置端口输入限制 (1000-9999)
            PlainDocument doc = (PlainDocument) portField.getDocument();
            doc.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                        throws BadLocationException {
                    String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
                    if (isValidPortInput(newText)) {
                        super.insertString(fb, offset, string, attr);
                    }
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                        throws BadLocationException {
                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                    if (isValidPortInput(newText)) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }

                private boolean isValidPortInput(String text) {
                    // 只允许输入4位数字
                    if (text.length() > 4) return false;
                    // 只允许数字输入
                    if (!text.matches("\\d*")) return false;
                    // 如果已经有4位，检查范围
                    if (text.length() == 4) {
                        try {
                            int port = Integer.parseInt(text);
                            return port >= 1000 && port <= 9999;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                    return true;
                }
            });

            // 添加焦点监听器，保存端口配置
            portField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    String portText = portField.getText().trim();
                    if (portText.length() != 4) {
                        JOptionPane.showMessageDialog(LoginSystem.this,
                                "端口号必须是4位数字", "输入错误", JOptionPane.WARNING_MESSAGE);
                        portField.setText(String.valueOf(Config.PORT));
                        return;
                    }

                    try {
                        int port = Integer.parseInt(portText);
                        if (port >= 1000 && port <= 9999) {
                            Config.PORT = port;
                        } else {
                            JOptionPane.showMessageDialog(LoginSystem.this,
                                    "端口必须在1000-9999范围内", "输入错误", JOptionPane.WARNING_MESSAGE);
                            portField.setText(String.valueOf(Config.PORT));
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(LoginSystem.this,
                                "请输入有效的端口号", "输入错误", JOptionPane.ERROR_MESSAGE);
                        portField.setText(String.valueOf(Config.PORT));
                    }
                }
            });

            add(portField, gbc);

            // 登录按钮
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            JButton loginButton = new JButton("登录");
            loginButton.setBackground(new Color(70, 130, 180));
            loginButton.setForeground(Color.WHITE);
            loginButton.setFont(customFont.deriveFont(Font.BOLD, 14f));
            loginButton.setPreferredSize(new Dimension(120, 35));
            loginButton.addActionListener(e -> {
                String account = accountField.getText().trim();
                String password = new String(passwordField.getPassword());

                if (account.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginSystem.this,
                            "账号和密码不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 确保端口配置已保存
                String portText = portField.getText().trim();
                if (portText.length() != 4) {
                    JOptionPane.showMessageDialog(LoginSystem.this,
                            "端口号必须是4位数字", "输入错误", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    int port = Integer.parseInt(portText);
                    if (port >= 1000 && port <= 9999) {
                        Config.PORT = SetUserContent.setPort(port);
                    } else {
                        JOptionPane.showMessageDialog(LoginSystem.this,
                                "端口必须在1000-9999范围内", "输入错误", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(LoginSystem.this,
                            "请输入有效的端口号", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (UserLogical.login(account, password)) {
                    JOptionPane.showMessageDialog(LoginSystem.this,
                            "登录成功！启动端口: " + Config.PORT, "成功", JOptionPane.INFORMATION_MESSAGE);
                    // 这里可以打开主界面
                } else {
                    JOptionPane.showMessageDialog(LoginSystem.this,
                            "账号或密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
                }
            });
            add(loginButton, gbc);

            // 注册链接
            gbc.gridy = 5;
            JButton registerButton = new JButton("没有账号？立即注册");
            registerButton.setBorderPainted(false);
            registerButton.setContentAreaFilled(false);
            registerButton.setForeground(new Color(30, 144, 255));
            registerButton.setFont(customFont.deriveFont(Font.PLAIN, 12f));
            registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            registerButton.addActionListener(e -> cardLayout.show(cardPanel, "REGISTER"));
            add(registerButton, gbc);
        }
    }

    class RegisterPanel extends JPanel {
        private JTextField accountField;
        private JPasswordField passwordField;
        private JPasswordField confirmPasswordField;
        private JTextField nicknameField;

        public RegisterPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 10, 8, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // 标题
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            JLabel titleLabel = new JLabel("注册", JLabel.CENTER);
            titleLabel.setFont(customFont.deriveFont(Font.BOLD, 18f));
            add(titleLabel, gbc);

            // 账号
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            JLabel accountLabel = new JLabel("账号:");
            accountLabel.setFont(customFont);
            add(accountLabel, gbc);

            gbc.gridx = 1;
            accountField = new JTextField(20);
            accountField.setFont(customFont);
            add(accountField, gbc);

            // 密码
            gbc.gridx = 0;
            gbc.gridy = 2;
            JLabel passwordLabel = new JLabel("密码:");
            passwordLabel.setFont(customFont);
            add(passwordLabel, gbc);

            gbc.gridx = 1;
            passwordField = new JPasswordField(20);
            passwordField.setFont(customFont);
            add(passwordField, gbc);

            // 确认密码
            gbc.gridx = 0;
            gbc.gridy = 3;
            JLabel confirmLabel = new JLabel("确认密码:");
            confirmLabel.setFont(customFont);
            add(confirmLabel, gbc);

            gbc.gridx = 1;
            confirmPasswordField = new JPasswordField(20);
            confirmPasswordField.setFont(customFont);
            add(confirmPasswordField, gbc);

            // 昵称
            gbc.gridx = 0;
            gbc.gridy = 4;
            JLabel nicknameLabel = new JLabel("昵称:");
            nicknameLabel.setFont(customFont);
            add(nicknameLabel, gbc);

            gbc.gridx = 1;
            nicknameField = new JTextField(20);
            nicknameField.setFont(customFont);
            add(nicknameField, gbc);

            // 注册按钮
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            JButton registerButton = new JButton("注册");
            registerButton.setBackground(new Color(60, 179, 113));
            registerButton.setForeground(Color.WHITE);
            registerButton.setFont(customFont.deriveFont(Font.BOLD, 14f));
            registerButton.setPreferredSize(new Dimension(120, 35));
            registerButton.addActionListener(e -> {
                String account = accountField.getText().trim();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                String nickname = nicknameField.getText().trim();

                if (account.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginSystem.this,
                            "所有字段不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(LoginSystem.this,
                            "两次输入的密码不一致", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String userId = UserLogical.register(account, password, nickname);
                if (userId != null) {
                    JOptionPane.showMessageDialog(LoginSystem.this,
                            "注册成功！请登录", "注册成功", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(cardPanel, "LOGIN");
                    // 清空注册表单
                    accountField.setText("");
                    passwordField.setText("");
                    confirmPasswordField.setText("");
                    nicknameField.setText("");
                } else {
                    JOptionPane.showMessageDialog(LoginSystem.this,
                            "注册失败，账号可能已被使用", "注册失败", JOptionPane.ERROR_MESSAGE);
                }
            });
            add(registerButton, gbc);

            // 返回登录链接
            gbc.gridy = 6;
            JButton backButton = new JButton("返回登录");
            backButton.setBorderPainted(false);
            backButton.setContentAreaFilled(false);
            backButton.setForeground(new Color(30, 144, 255));
            backButton.setFont(customFont.deriveFont(Font.PLAIN, 12f));
            backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            backButton.addActionListener(e -> cardLayout.show(cardPanel, "LOGIN"));
            add(backButton, gbc);
        }
    }
}