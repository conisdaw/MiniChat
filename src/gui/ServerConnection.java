package gui;

import core.Config;
import core.FontUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerConnection extends JDialog {
    private JTextField ipField;
    private JTextField portField;
    private Font customFont;
    private boolean settingsSaved = false;

    public ServerConnection(JFrame parent) {
        super(parent, "服务器网络设置", true);
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        java.net.URL iconUrl = getClass().getResource(Config.FAVICON_PATH);
        setIconImage(Toolkit.getDefaultToolkit().getImage(iconUrl));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // 加载自定义字体
        customFont = FontUtil.loadCustomFont(14f);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(240, 245, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel ipLabel = new JLabel("服务器 IP:");
        ipLabel.setFont(customFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(ipLabel, gbc);

        ipField = new JTextField(15);
        ipField.setFont(customFont);
        ipField.setText(getLocalIP());
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        panel.add(ipField, gbc);

        JLabel portLabel = new JLabel("端口:");
        portLabel.setFont(customFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(portLabel, gbc);

        portField = new JTextField("", 15);
        portField.setFont(customFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;

        // 端口输入限制 (1000-9999)
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
                if (text.length() > 4) return false;
                if (!text.matches("\\d*")) return false;
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
        panel.add(portField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(240, 245, 255));

        JButton saveButton = new JButton("保存");
        styleButton(saveButton);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = ipField.getText().trim();
                String portStr = portField.getText().trim();

                if (portStr.length() != 4) {
                    JOptionPane.showMessageDialog(ServerConnection.this,
                            "端口号必须是4位数字", "输入错误", JOptionPane.WARNING_MESSAGE);
                    portField.setText(String.valueOf(Config.SERVER_PORT));
                    return;
                }

                try {
                    int port = Integer.parseInt(portStr);
                    if (port >= 1000 && port <= 9999) {
                        Config.SERVER_IP = ip;
                        Config.SERVER_PORT = port;
                        settingsSaved = true;
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(ServerConnection.this,
                                "端口必须在1000-9999范围内", "输入错误", JOptionPane.WARNING_MESSAGE);
                        portField.setText(String.valueOf(Config.SERVER_PORT));
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ServerConnection.this,
                            "请输入有效的端口号", "输入错误", JOptionPane.ERROR_MESSAGE);
                    portField.setText(String.valueOf(Config.SERVER_PORT));
                }
            }
        });

        JButton cancelButton = new JButton("取消");
        styleButton(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setFont(customFont.deriveFont(Font.BOLD, 14f));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(80, 30));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public boolean isSettingsSaved() {
        return settingsSaved;
    }

    private static String getLocalIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isLoopbackAddress()) continue;

                    // 优先返回 IPv4 地址
                    String ip = address.getHostAddress();
                    if (ip.indexOf(':') == -1) {
                        return ip;
                    }
                }
            }
            return null;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}