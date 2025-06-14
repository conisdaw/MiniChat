package gui;

import core.ChatCore;
import core.Config;
import core.FontUtil;
import data.ChatMessage;
import data.FriendsSQL;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;

public class ChatInterface extends JFrame {
    private JTabbedPane tabbedPane;
    private JList<String> friendsList;
    private JList<String> groupsList;
    private JList<String> blockedList;
    private DefaultListModel<String> friendsModel;
    private DefaultListModel<String> groupsModel;
    private DefaultListModel<String> blockedModel;
    private JTextPane chatHistoryArea;
    private JTextArea messageInputArea;
    private JLabel currentChatLabel;
    private JLabel networkInfoLabel;
    private Font customFont;
    private Color backgroundColor = new Color(240, 245, 255);
    private Color panelColor = new Color(250, 250, 255);
    private Color buttonColor = new Color(70, 130, 180);
    private Color buttonHoverColor = new Color(100, 149, 237);
    private String currentChatId;
    private boolean currentIsGroup;
    private Map<String, String> friendIdMap = new HashMap<>();
    private Map<String, String> groupIdMap = new HashMap<>();
    private Map<String, String> currentGroupMemberMap;
    private Map<String, String> blockedIdMap = new HashMap<>();


    public ChatInterface() {
        super("聊天应用");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 加载自定义字体
        customFont = FontUtil.loadCustomFont(14f);

        initUI();
    }

    private void initUI() {
        // 主布局
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(250);
        mainSplitPane.setDividerSize(3);
        mainSplitPane.setBorder(null);

        // 左侧面板
        JPanel leftPanel = createLeftPanel();
        mainSplitPane.setLeftComponent(leftPanel);

        // 右侧面板
        JPanel rightPanel = createRightPanel();
        mainSplitPane.setRightComponent(rightPanel);

        add(mainSplitPane);
    }

    private void loadFriendList() {
        friendsModel.clear();
        friendIdMap.clear();
        List<Map<String, String>> friends = ChatCore.getFriendList();
        for (Map<String, String> friend : friends) {
            String name = friend.get("name"); // 正确键名
            String friendId = friend.get("friend_id");
            friendsModel.addElement(name);
            friendIdMap.put(name, friendId);
        }
    }

    private void loadGroupList() {
        groupsModel.clear();
        groupIdMap.clear();
        List<Map<String, String>> groups = ChatCore.getGroupList();
        for (Map<String, String> group : groups) {
            String name = group.values().iterator().next();
            String groupId = group.keySet().iterator().next();
            groupsModel.addElement(name);
            groupIdMap.put(name, groupId);
        }
    }

    private void loadBlockedList() {
        blockedModel.clear();
        blockedIdMap.clear(); // 清空旧映射
        List<Map<String, String>> blockedFriends = ChatCore.getBlockedFriendList();
        for (Map<String, String> blocked : blockedFriends) {
            String name = blocked.get("name");
            String friendId = blocked.get("friend_id");
            blockedModel.addElement(name + " (已拉黑)");
            blockedIdMap.put(name, friendId);
        }
    }

    // 我的信息选项卡
    private JPanel createMyInfoTab() {
        JPanel myInfoTab = new JPanel(new BorderLayout());
        myInfoTab.setBackground(panelColor);

        // 主面板 - 使用垂直布局
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(panelColor);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel horizontalPanel = new JPanel(new BorderLayout(5, 0)); // 水平间距5px
        horizontalPanel.setBackground(panelColor);

        // 头像面板
        JPanel avatarPanel = new JPanel();
        avatarPanel.setBackground(panelColor);
        avatarPanel.setBorder(null);

        // 加载头像
        ImageIcon originalIcon = null;
        try {
            originalIcon = new ImageIcon(getClass().getResource(Config.IMAGE_PATH + "/avatar.jpg"));
        } catch (Exception e) {
            // 如果头像加载失败，使用默认图标
            try {
                originalIcon = new ImageIcon(getClass().getResource(Config.IMAGE_PATH + "/default_avatar.png"));
            } catch (Exception ex) {
                // 如果默认头像也没有，创建一个空图标
                originalIcon = new ImageIcon();
            }
        }

        // 增大头像大小到100x100
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel avatarLabel = new JLabel(new ImageIcon(scaledImage));
        avatarLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 220), 2));
        avatarPanel.add(avatarLabel);

        // 信息面板 - 紧挨着头像
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(panelColor);
        infoPanel.setBorder(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 0, 10); // 减少内部间距为0
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nicknameLabel = new JLabel("我的昵称:");
        nicknameLabel.setFont(customFont.deriveFont(Font.BOLD, 16f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(nicknameLabel, gbc);

        JLabel currentNicknameLabel = new JLabel(Config.USER_NAME);
        currentNicknameLabel.setFont(customFont.deriveFont(Font.PLAIN, 16f));
        gbc.gridx = 1;
        gbc.gridy = 0;
        infoPanel.add(currentNicknameLabel, gbc);

        // 将头像和信息面板添加到水平面板
        horizontalPanel.add(avatarPanel, BorderLayout.WEST);
        horizontalPanel.add(infoPanel, BorderLayout.CENTER);

        JButton changeNicknameButton = new JButton("修改昵称");
        styleButton(changeNicknameButton, true);
        changeNicknameButton.addActionListener(e -> {
            String newNickname = JOptionPane.showInputDialog(
                    this,
                    "输入新昵称:",
                    "修改昵称",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (newNickname != null && !newNickname.trim().isEmpty()) {
                ChatCore.updataNickname(newNickname.trim());
                currentNicknameLabel.setText(newNickname.trim());
                Config.USER_NAME = newNickname.trim();
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        infoPanel.add(changeNicknameButton, gbc);

        // 添加所有组件到主面板（直接添加，不添加额外面板）
        mainPanel.add(avatarPanel);
        mainPanel.add(infoPanel);

        myInfoTab.add(mainPanel, BorderLayout.CENTER);
        return myInfoTab;
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(panelColor);
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(customFont.deriveFont(Font.BOLD, 14f));
        tabbedPane.setBackground(panelColor);

        // 好友选项卡
        JPanel friendsTab = createFriendsTab();
        tabbedPane.addTab("好友", friendsTab);

        // 群聊选项卡
        JPanel groupsTab = createGroupsTab();
        tabbedPane.addTab("群聊", groupsTab);

        // 被拉黑选项卡
        JPanel blockedTab = createBlockedTab();
        tabbedPane.addTab("被拉黑", blockedTab);

        // 我的信息选项卡
        JPanel myInfoTab = createMyInfoTab();
        tabbedPane.addTab("我的信息", myInfoTab);

        leftPanel.add(tabbedPane, BorderLayout.CENTER);

        // 底部面板（网络信息和按钮）
        JPanel bottomPanel = createBottomPanel();
        leftPanel.add(bottomPanel, BorderLayout.SOUTH);

        return leftPanel;
    }

    private JPanel createBlockedTab() {
        JPanel blockedTab = new JPanel(new BorderLayout());
        blockedTab.setBackground(panelColor);

        JPanel blockedHeader = new JPanel(new BorderLayout());
        blockedHeader.setBackground(panelColor);
        blockedHeader.setBorder(new EmptyBorder(5, 0, 10, 0));

        JLabel blockedLabel = new JLabel("被拉黑好友");
        blockedLabel.setFont(customFont.deriveFont(Font.BOLD, 16f));
        blockedLabel.setForeground(new Color(50, 50, 50));
        blockedHeader.add(blockedLabel, BorderLayout.WEST);
        blockedTab.add(blockedHeader, BorderLayout.NORTH);

        // 被拉黑好友列表
        blockedModel = new DefaultListModel<>();
        loadBlockedList();
        blockedList = new JList<>(blockedModel);
        blockedList.setFont(customFont);
        blockedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        blockedList.setBackground(panelColor);
        blockedList.setCellRenderer(new ContactListCellRenderer());

        // 添加右键菜单（被拉黑好友设置）
        JPopupMenu blockedPopupMenu = new JPopupMenu();
        JMenuItem unblockItem = new JMenuItem("解除拉黑");
        unblockItem.addActionListener(e -> {
            String selected = blockedList.getSelectedValue();
            if (selected != null) {
                String friendName = selected.replace(" (已拉黑)", "");
                String friendId = blockedIdMap.get(friendName); // 从blockedIdMap获取ID

                if (friendId != null) {
                    ChatCore.blockedFriend(friendId);
                    blockedModel.removeElement(selected);
                    friendsModel.addElement(friendName);
                    friendIdMap.put(friendName, friendId); // 加回好友映射
                }
            }
        });
        blockedPopupMenu.add(unblockItem);
        blockedList.setComponentPopupMenu(blockedPopupMenu);

        JScrollPane blockedScrollPane = new JScrollPane(blockedList);
        blockedScrollPane.setBorder(null);
        blockedTab.add(blockedScrollPane, BorderLayout.CENTER);

        return blockedTab;
    }


    private JPanel createFriendsTab() {
        JPanel friendsTab = new JPanel(new BorderLayout());
        friendsTab.setBackground(panelColor);

        // 好友列表标题
        JPanel friendsHeader = new JPanel(new BorderLayout());
        friendsHeader.setBackground(panelColor);
        friendsHeader.setBorder(new EmptyBorder(5, 0, 10, 0));

        JLabel friendsLabel = new JLabel("好友列表");
        friendsLabel.setFont(customFont.deriveFont(Font.BOLD, 16f));
        friendsLabel.setForeground(new Color(50, 50, 50));
        friendsHeader.add(friendsLabel, BorderLayout.WEST);
        friendsTab.add(friendsHeader, BorderLayout.NORTH);

        // 好友列表
        friendsModel = new DefaultListModel<>();
        loadFriendList();
        friendsList = new JList<>(friendsModel);
        friendsList.setFont(customFont);
        friendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendsList.setBackground(panelColor);
        friendsList.setCellRenderer(new ContactListCellRenderer());

        // 添加右键菜单（好友设置按钮）
        JPopupMenu friendPopupMenu = new JPopupMenu();

        // 添加备注选项
        JMenuItem remarkItem = new JMenuItem("修改备注");
        remarkItem.addActionListener(e -> {
            String selected = friendsList.getSelectedValue();
            if (selected != null) {
                String friendId = friendIdMap.get(selected);
                if (friendId != null) {
                    String newRemark = JOptionPane.showInputDialog(
                            this,
                            "输入新备注:",
                            "修改备注",
                            JOptionPane.PLAIN_MESSAGE
                    );

                    if (newRemark != null && !newRemark.trim().isEmpty()) {
                        // 调用核心方法更新备注
                        ChatCore.UpdataFriendsRemark(newRemark.trim(), friendId);

                        // 更新UI
                        int index = friendsList.getSelectedIndex();
                        friendsModel.setElementAt(newRemark.trim(), index);
                    }
                }
            }
        });

        JMenuItem blockItem = new JMenuItem("拉黑");
        blockItem.addActionListener(e -> {
            String selected = friendsList.getSelectedValue();
            if (selected != null) {
                String friendId = friendIdMap.get(selected);
                if (friendId != null) {
                    ChatCore.blockedFriend(friendId);
                    friendsModel.removeElement(selected);
                    blockedModel.addElement(selected + " (已拉黑)");
                    blockedIdMap.put(selected, friendId);
                    friendIdMap.remove(selected);
                }
            }
        });

        JMenuItem deleteItem = new JMenuItem("删除");
        deleteItem.addActionListener(e -> {
            String selected = friendsList.getSelectedValue();
            if (selected != null) {
                String friendId = friendIdMap.get(selected);
                if (friendId != null) {
                    // 调用核心方法删除好友
                    ChatCore.deleteFriend(friendId);

                    friendsModel.removeElement(selected);
                    friendIdMap.remove(selected);
                }
            }
        });

        friendPopupMenu.add(remarkItem);
        friendPopupMenu.add(blockItem);
        friendPopupMenu.add(deleteItem);

        friendsList.setComponentPopupMenu(friendPopupMenu);

        friendsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedFriend = friendsList.getSelectedValue();
                if (selectedFriend != null) {
                    currentChatLabel.setText("与 " + selectedFriend + " 聊天中");
                    loadChatHistory(selectedFriend, false);
                }
            }
        });

        JScrollPane friendsScrollPane = new JScrollPane(friendsList);
        friendsScrollPane.setBorder(null);
        friendsTab.add(friendsScrollPane, BorderLayout.CENTER);

        return friendsTab;
    }

    private JPanel createGroupsTab() {
        JPanel groupsTab = new JPanel(new BorderLayout());
        groupsTab.setBackground(panelColor);

        // 群聊列表标题
        JLabel groupsLabel = new JLabel("群聊列表");
        groupsLabel.setFont(customFont.deriveFont(Font.BOLD, 16f));
        groupsLabel.setForeground(new Color(50, 50, 50));
        groupsLabel.setBorder(new EmptyBorder(5, 0, 10, 0));
        groupsTab.add(groupsLabel, BorderLayout.NORTH);

        // 群聊列表
        groupsModel = new DefaultListModel<>();
        loadGroupList();
        groupsList = new JList<>(groupsModel);
        groupsList.setFont(customFont);
        groupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupsList.setBackground(panelColor);
        groupsList.setCellRenderer(new ContactListCellRenderer());

        // 添加群聊右键菜单
        JPopupMenu groupPopupMenu = new JPopupMenu();

        // 添加成员
        JMenuItem addMemberItem = new JMenuItem("添加成员");
        addMemberItem.addActionListener(e -> {
            String selectedGroup = groupsList.getSelectedValue();
            if (selectedGroup != null) {
                String groupId = groupIdMap.get(selectedGroup);
                if (groupId != null) {
                    showAddMemberDialog(groupId);
                }
            }
        });


        // 修改群名称
        JMenuItem renameGroupItem = new JMenuItem("修改群名称");
        renameGroupItem.addActionListener(e -> {
            String selectedGroup = groupsList.getSelectedValue();
            if (selectedGroup != null) {
                String newName = JOptionPane.showInputDialog(
                        this,
                        "输入新群名称:",
                        "修改群名称",
                        JOptionPane.PLAIN_MESSAGE
                );

                if (newName != null && !newName.trim().isEmpty()) {
                    String groupId = groupIdMap.get(selectedGroup);
                    if (groupId != null) {
                        // 调用核心方法更新群名称
                        ChatCore.updateGroupName(groupId, newName.trim());

                        // 更新UI
                        int index = groupsList.getSelectedIndex();
                        groupsModel.setElementAt(newName.trim(), index);
                    }
                }
            }
        });

        // 修改我的群昵称
        JMenuItem changeMyNickItem = new JMenuItem("修改我的群昵称");
        changeMyNickItem.addActionListener(e -> {
            String selectedGroup = groupsList.getSelectedValue();
            if (selectedGroup != null) {
                String newNick = JOptionPane.showInputDialog(
                        this,
                        "输入新群昵称:",
                        "修改群昵称",
                        JOptionPane.PLAIN_MESSAGE
                );

                if (newNick != null && !newNick.trim().isEmpty()) {
                    String groupId = groupIdMap.get(selectedGroup);
                    if (groupId != null) {
                        // 调用核心方法更新群昵称
                        ChatCore.updateGroupNickname(groupId, Config.USER_ID, newNick.trim());
                    }
                }
            }
        });

        // 解散群
        JMenuItem dismissGroupItem = new JMenuItem("解散群");
        dismissGroupItem.addActionListener(e -> {
            String selectedGroup = groupsList.getSelectedValue();
            if (selectedGroup != null) {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "确定要解散群 '" + selectedGroup + "' 吗?",
                        "解散群",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    String groupId = groupIdMap.get(selectedGroup);
                    if (groupId != null) {
                        // 调用核心方法解散群
                        if (ChatCore.deleteGroups(groupId)) {
                            groupsModel.removeElement(selectedGroup);
                            groupIdMap.remove(selectedGroup);
                        }
                    }
                }
            }
        });

        groupPopupMenu.add(addMemberItem);
        groupPopupMenu.add(renameGroupItem);
        groupPopupMenu.add(changeMyNickItem);
        groupPopupMenu.addSeparator();
        groupPopupMenu.add(dismissGroupItem);

        groupsList.setComponentPopupMenu(groupPopupMenu);

        groupsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedGroup = groupsList.getSelectedValue();
                if (selectedGroup != null) {
                    currentChatLabel.setText("群聊: " + selectedGroup);
                    loadChatHistory(selectedGroup, true);
                }
            }
        });

        JScrollPane groupsScrollPane = new JScrollPane(groupsList);
        groupsScrollPane.setBorder(null);
        groupsTab.add(groupsScrollPane, BorderLayout.CENTER);

        return groupsTab;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(panelColor);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                new EmptyBorder(10, 5, 5, 5)
        ));

        // 网络信息
        JPanel networkPanel = new JPanel(new BorderLayout());
        networkPanel.setBackground(panelColor);

        networkInfoLabel = new JLabel("网络: " + Config.IP + ":" + Config.PORT);
        networkInfoLabel.setFont(customFont);
        networkInfoLabel.setForeground(new Color(80, 80, 80));

        JButton editNetworkButton = new JButton("修改");
        styleButton(editNetworkButton, true);
        editNetworkButton.setPreferredSize(new Dimension(70, 25));
        editNetworkButton.addActionListener(e -> showNetworkDialog());

        networkPanel.add(networkInfoLabel, BorderLayout.CENTER);
        networkPanel.add(editNetworkButton, BorderLayout.EAST);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(panelColor);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 5, 0));

        JButton addFriendButton = new JButton("添加好友");
        styleButton(addFriendButton, false);
        addFriendButton.addActionListener(e -> showAddFriendDialog());

        JButton createGroupButton = new JButton("创建群聊");
        styleButton(createGroupButton, false);
        createGroupButton.addActionListener(e -> showCreateGroupDialog());

        buttonPanel.add(addFriendButton);
        buttonPanel.add(createGroupButton);

        bottomPanel.add(networkPanel);
        bottomPanel.add(buttonPanel);

        return bottomPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(backgroundColor);
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 当前聊天标题
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(backgroundColor);
        chatHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        currentChatLabel = new JLabel("请选择聊天对象");
        currentChatLabel.setFont(customFont.deriveFont(Font.BOLD, 16f));
        currentChatLabel.setForeground(new Color(50, 50, 50));

        chatHeader.add(currentChatLabel, BorderLayout.CENTER);
        rightPanel.add(chatHeader, BorderLayout.NORTH);

        // 聊天历史区域
        chatHistoryArea = new JTextPane();
        chatHistoryArea.setContentType("text/html");
        chatHistoryArea.setEditorKit(new HTMLEditorKit());
        chatHistoryArea.setDocument(new HTMLDocument());
        chatHistoryArea.setEditable(false);
        //chatHistoryArea.setCursor(null); // 移除光标
        chatHistoryArea.setFont(customFont);
        chatHistoryArea.setBackground(Color.WHITE);
        chatHistoryArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 220)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // 添加超链接监听器
        chatHistoryArea.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                handleHyperlinkClick(e.getDescription());
            }
        });

//        chatHistoryArea = new JTextArea();
//        chatHistoryArea.setFont(customFont);
//        chatHistoryArea.setEditable(false);
//        chatHistoryArea.setLineWrap(true);
//        chatHistoryArea.setWrapStyleWord(true);
//        chatHistoryArea.setBackground(Color.WHITE);
//        chatHistoryArea.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(new Color(200, 200, 220)),
//                new EmptyBorder(10, 10, 10, 10)
//        ));

        // 设置自动滚动
        DefaultCaret caret = (DefaultCaret) chatHistoryArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane historyScrollPane = new JScrollPane(chatHistoryArea);
        historyScrollPane.setBorder(null);
        rightPanel.add(historyScrollPane, BorderLayout.CENTER);

        // 输入区域面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(backgroundColor);
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // 消息输入框
        messageInputArea = new JTextArea(3, 20);
        messageInputArea.setFont(customFont);
        messageInputArea.setLineWrap(true);
        messageInputArea.setWrapStyleWord(true);
        messageInputArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 220)),
                new EmptyBorder(8, 8, 8, 8)
        ));

        // 处理回车发送和Shift+回车换行
        messageInputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown()) {
                        messageInputArea.append("\n");
                    } else {
                        e.consume(); // 防止添加换行符
                        sendMessage();
                    }
                }
            }
        });

        JScrollPane inputScrollPane = new JScrollPane(messageInputArea);
        inputScrollPane.setBorder(null);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setBackground(backgroundColor);

        JButton imageButton = new JButton("图片");
        styleButton(imageButton, true);
        imageButton.addActionListener(e -> selectImage());

        JButton fileButton = new JButton("文件");
        styleButton(fileButton, true);
        fileButton.addActionListener(e -> selectFile());

        JButton sendButton = new JButton("发送");
        styleButton(sendButton, false);
        sendButton.addActionListener(e -> sendMessage());

        buttonPanel.add(imageButton);
        buttonPanel.add(fileButton);
        buttonPanel.add(sendButton);

        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        return rightPanel;
    }

    // 处理超链接点击事件
    private void handleHyperlinkClick(String path) {
        File file = new File(Config.FILE_BASE_DIR + path);
        if (file.exists()) {
            try {
                // 图片文件显示缩略图
                if (path.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) {
                    showImageThumbnail(file);
                } else {
                    // 其他文件用系统默认方式打开
                    Desktop.getDesktop().open(file);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "打开文件失败: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "文件不存在: " + file.getAbsolutePath(),
                    "错误",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // 显示图片缩略图
    private void showImageThumbnail(File imageFile) {
        JDialog thumbnailDialog = new JDialog(this, "图片预览", false);
        thumbnailDialog.setSize(400, 400);
        thumbnailDialog.setLocationRelativeTo(this);

        try {
            // 加载图片并缩放
            BufferedImage originalImage = ImageIO.read(imageFile);
            int maxSize = 380;
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            if (width > maxSize || height > maxSize) {
                double scale = Math.min((double) maxSize / width, (double) maxSize / height);
                width = (int) (width * scale);
                height = (int) (height * scale);
            }

            Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));

            // 添加打开原图按钮
            JButton openOriginalButton = new JButton("打开原图");
            styleButton(openOriginalButton, true);
            openOriginalButton.addActionListener(e -> {
                try {
                    Desktop.getDesktop().open(imageFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.add(imageLabel, BorderLayout.CENTER);
            contentPanel.add(openOriginalButton, BorderLayout.SOUTH);

            thumbnailDialog.add(contentPanel);
            thumbnailDialog.setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "无法加载图片: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleButton(JButton button, boolean isSmall) {
        button.setFont(customFont.deriveFont(isSmall ? Font.PLAIN : Font.BOLD, isSmall ? 13f : 14f));
        button.setBackground(buttonColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (isSmall) {
            button.setPreferredSize(new Dimension(70, 28));
        } else {
            button.setPreferredSize(new Dimension(120, 35));
        }

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(buttonHoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(buttonColor);
            }
        });
    }

    private void showAddMemberDialog(String groupId) {
        JDialog dialog = new JDialog(this, "添加成员到群聊", true);
        // 对话框布局代码（与创建群聊类似）

        // 好友选择列表
        JList<String> selectionList = new JList<>(friendsModel);
        selectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JButton addButton = new JButton("添加");
        addButton.addActionListener(evt -> {
            List<String> selectedFriends = selectionList.getSelectedValuesList();
            for (String friendName : selectedFriends) {
                String friendId = friendIdMap.get(friendName);
                if (friendId != null) {
                    try {
                        Map<String, String> friendInfo = FriendsSQL.getFriendByID(Config.DB_PATH, friendId);
                        String ip = friendInfo.get("ip_address");
                        int port = Integer.parseInt(friendInfo.get("port"));
                        // 调用核心方法添加成员
                        ChatCore.updateGroupMembers(
                                groupId,
                                friendId,
                                friendName,
                                ip,
                                port
                        );
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            dialog.dispose();
        });

        // 显示对话框
        dialog.setVisible(true);
    }

    private String getUsernameById(String userId) {
        if (userId.equals(Config.USER_ID)) {
            return "我";
        }

        for (Map.Entry<String, String> entry : friendIdMap.entrySet()) {
            if (entry.getValue().equals(userId)) {
                return entry.getKey();
            }
        }

        if (currentIsGroup && currentGroupMemberMap != null) {
            return currentGroupMemberMap.getOrDefault(userId, userId);
        }

        return userId;
    }

    private void loadChatHistory(String target, boolean isGroup) {
        chatHistoryArea.setText("");
        currentChatId = getTargetId(target, isGroup);
        currentIsGroup = isGroup;

        if (isGroup) {
            currentGroupMemberMap = ChatCore.getGroupMemberName(currentChatId);
            if (currentGroupMemberMap == null) {
                currentGroupMemberMap = new HashMap<>();
            }
            if (!currentGroupMemberMap.containsKey(Config.USER_ID)) {
                currentGroupMemberMap.put(Config.USER_ID, "我");
            }
        }

        List<ChatMessage> history = ChatCore.getChatHistory(currentChatId, currentIsGroup);
        for (ChatMessage message : history) {
            appendMessageToHistory(message);
        }
    }

    // 根据名称获取目标ID
    private String getTargetId(String targetName, boolean isGroup) {
        if (isGroup) {
            return groupIdMap.get(targetName);
        } else {
            return friendIdMap.get(targetName);
        }
    }

//    private void loadChatHistory(String target, boolean isGroup) {
//        // 这里会调用ChatCore.getChatHistory()方法
//        // 这里模拟一些聊天记录
//        chatHistoryArea.setText("");
//
//        List<ChatMessage> history = new ArrayList<>();
//
//        // 添加一些模拟消息
//        history.add(createMessage("text", "你好，最近怎么样？", false, "张三", "2023-10-01 10:30:15"));
//        history.add(createMessage("text", "我很好，项目进展顺利！", true, "我", "2023-10-01 10:31:22"));
//        history.add(createMessage("text", "这是你要的设计文档", false, "张三", "2023-10-01 10:32:05"));
//        history.add(createMessage("file", "design_document.pdf", false, "张三", "2023-10-01 10:32:10"));
//        history.add(createMessage("text", "谢谢，我看看！", true, "我", "2023-10-01 10:33:45"));
//        history.add(createMessage("image", "screenshot.png", false, "张三", "2023-10-01 10:35:20"));
//        history.add(createMessage("text", "这是最新的界面截图", false, "张三", "2023-10-01 10:35:30"));
//
//        // 在文本区域显示聊天记录
//        for (ChatMessage message : history) {
//            appendMessageToHistory(message);
//        }
//    }

    private ChatMessage createMessage(String type, String content, boolean isSelf, String sender, String time) {
        ChatMessage msg = new ChatMessage();
        msg.setMessageType(type);
        msg.setContent(content);
        msg.setSenderId(isSelf ? "我" : sender);
        msg.setTimestamp(java.sql.Timestamp.valueOf(time));
        return msg;
    }

    private void appendMessageToHistory(ChatMessage message) {
        String timeStr = message.getTimestamp().toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        String sender;
        if (message.getSenderId().equals(Config.USER_ID)) {
            sender = "我";
        } else if (currentIsGroup) {
            if (currentGroupMemberMap != null && currentGroupMemberMap.containsKey(message.getSenderId())) {
                sender = currentGroupMemberMap.get(message.getSenderId());
            } else {
                sender = getUsernameById(message.getSenderId());
            }
        } else {
            sender = getUsernameById(message.getSenderId());
        }

        String content;
        String filePath = message.getContent();

        switch (message.getMessageType()) {
            case "image":
            case "file":
                // 文件路径不需要额外转义
                String fileName = new File(message.getContent()).getName();
                content = String.format(
                        "<a href='%s' style='color:blue;text-decoration:underline;'>[%s] %s</a>",
                        message.getContent(),
                        message.getMessageType().equals("image") ? "图片" : "文件",
                        fileName
                );
                break;
            default:
                content = escapeHtml(message.getContent())
                        .replace("\n", "<br/>");
        }

        // 构建HTML格式的消息
        String formattedMessage = String.format(
                "<div style='margin-bottom:10px;'>[%s] <b>%s</b>: %s</div>",
                timeStr, sender, content
        );

        try {
            HTMLEditorKit kit = (HTMLEditorKit) chatHistoryArea.getEditorKit();
            HTMLDocument doc = (HTMLDocument) chatHistoryArea.getDocument();
            kit.insertHTML(doc, doc.getLength(), formattedMessage, 0, 0, null);
            chatHistoryArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }

    // HTML转义工具方法
    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void sendMessage() {
        String message = messageInputArea.getText().trim();
        if (!message.isEmpty() && currentChatId != null && !currentChatId.isEmpty()) {
            ChatCore.setChatHistory(
                    currentIsGroup,
                    currentIsGroup ? currentChatId : null, // 群ID
                    currentIsGroup ? Config.USER_ID : currentChatId, // 成员ID或好友ID
                    message
            );
            ChatMessage newMessage = new ChatMessage();
            newMessage.setMessageType("text");
            newMessage.setContent(message);
            newMessage.setSenderId("我");
            newMessage.setTimestamp(java.sql.Timestamp.valueOf(LocalDateTime.now()));

            appendMessageToHistory(newMessage);
            messageInputArea.setText("");
        }
    }

//    private void sendMessage() {
//        String message = messageInputArea.getText().trim();
//        if (!message.isEmpty()) {
//            // 这里会调用ChatCore.setChatHistory()方法
//            ChatMessage newMessage = new ChatMessage();
//            newMessage.setMessageType("text");
//            newMessage.setContent(message);
//            newMessage.setSenderId("我");
//            newMessage.setTimestamp(java.sql.Timestamp.valueOf(LocalDateTime.now()));
//
//            appendMessageToHistory(newMessage);
//            messageInputArea.setText("");
//        }
//    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择图片");
        fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (currentChatId != null && !currentChatId.isEmpty()) {
                ChatCore.setImage(
                        currentIsGroup,
                        currentIsGroup ? currentChatId : null,
                        currentIsGroup ? Config.USER_ID : currentChatId,
                        selectedFile.getAbsolutePath()
                );
            }
            ChatMessage imageMessage = new ChatMessage();
            imageMessage.setMessageType("image");
            imageMessage.setContent(selectedFile.getName());
            imageMessage.setSenderId("我");
            imageMessage.setTimestamp(java.sql.Timestamp.valueOf(LocalDateTime.now()));
            
            appendMessageToHistory(imageMessage);
        }
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择文件");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (currentChatId != null && !currentChatId.isEmpty()) {
                ChatCore.setFile(
                        currentIsGroup,
                        currentIsGroup ? currentChatId : null,
                        currentIsGroup ? Config.USER_ID : currentChatId,
                        selectedFile.getAbsolutePath()
                );
            }
            ChatMessage fileMessage = new ChatMessage();
            fileMessage.setMessageType("file");
            fileMessage.setContent(selectedFile.getName());
            fileMessage.setSenderId("我");
            fileMessage.setTimestamp(java.sql.Timestamp.valueOf(LocalDateTime.now()));
            
            appendMessageToHistory(fileMessage);
        }
    }

    private void showNetworkDialog() {
        JDialog dialog = new JDialog(this, "修改网络设置", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(panelColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel ipLabel = new JLabel("IP地址:");
        ipLabel.setFont(customFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(ipLabel, gbc);

        JTextField ipField = new JTextField(Config.IP, 15);
        ipField.setFont(customFont);
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

        JTextField portField = new JTextField(String.valueOf(Config.PORT), 15);
        portField.setFont(customFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;

        // 添加端口输入限制 (1000-9999)
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

        panel.add(portField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(panelColor);

        JButton saveButton = new JButton("保存");
        styleButton(saveButton, true);
        saveButton.addActionListener(e -> {
            String ip = ipField.getText().trim();
            String portStr = portField.getText().trim();

            // 验证端口号
            if (portStr.length() != 4) {
                JOptionPane.showMessageDialog(dialog,
                        "端口号必须是4位数字", "输入错误", JOptionPane.WARNING_MESSAGE);
                portField.setText(String.valueOf(Config.PORT));
                return;
            }

            try {
                int port = Integer.parseInt(portStr);
                if (port >= 1000 && port <= 9999) {
                    String network = ip + ":" + portStr;

                    saveButton.setEnabled(false);
                    saveButton.setText("连接中...");

                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            ChatCore.resetNetwork(network);
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                                networkInfoLabel.setText("网络: " + network);
                                dialog.dispose();
                            } catch (Exception ex) {
                                saveButton.setEnabled(true);
                                saveButton.setText("保存");
                                JOptionPane.showMessageDialog(dialog,
                                        "网络连接失败: " + ex.getMessage(),
                                        "连接错误",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }.execute();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "端口必须在1000-9999范围内", "输入错误", JOptionPane.WARNING_MESSAGE);
                    portField.setText(String.valueOf(Config.PORT));
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "请输入有效的端口号", "输入错误", JOptionPane.ERROR_MESSAGE);
                portField.setText(String.valueOf(Config.PORT));
            }
        });

        JButton cancelButton = new JButton("取消");
        styleButton(cancelButton, true);
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showAddFriendDialog() {
        String network = JOptionPane.showInputDialog(
                this,
                "输入对方的网络地址（格式：IP:端口）",
                "添加好友",
                JOptionPane.PLAIN_MESSAGE
        );

        if (network != null && !network.trim().isEmpty()) {
            // 验证格式
            if (!network.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d+")) {
                JOptionPane.showMessageDialog(this,
                        "格式错误！正确格式：192.168.1.100:8080",
                        "输入错误",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 调用核心方法添加好友
            boolean success = ChatCore.addFriend(network.trim());
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "好友请求已发送",
                        "添加好友",
                        JOptionPane.INFORMATION_MESSAGE);
                loadFriendList();
            } else {
                JOptionPane.showMessageDialog(this,
                        "添加失败，请检查网络地址",
                        "添加失败",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCreateGroupDialog() {
        // 创建多选好友对话框
        JDialog dialog = new JDialog(this, "创建群聊", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(panelColor);

        // 添加多选提示
        JLabel hintLabel = new JLabel("按住 Ctrl 或 Shift 键多选");
        hintLabel.setFont(customFont.deriveFont(Font.ITALIC, 12f));
        hintLabel.setForeground(new Color(100, 100, 100));
        hintLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

        // 好友选择列表（多选模式）
        JList<String> selectionList = new JList<>(friendsModel);
        selectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // 启用多选
        selectionList.setFont(customFont);
        selectionList.setBackground(panelColor);
        selectionList.setCellRenderer(new ContactListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(selectionList);

        // 创建包含提示和列表的面板
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(hintLabel, BorderLayout.NORTH);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(listPanel, BorderLayout.CENTER);

        // 输入群聊名称
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        inputPanel.setBackground(panelColor);

        JLabel nameLabel = new JLabel("群聊名称:");
        nameLabel.setFont(customFont);
        JTextField groupNameField = new JTextField(20);
        groupNameField.setFont(customFont);

        inputPanel.add(nameLabel, BorderLayout.WEST);
        inputPanel.add(groupNameField, BorderLayout.CENTER);
        contentPanel.add(inputPanel, BorderLayout.NORTH);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(panelColor);

        JButton createButton = new JButton("创建");
        styleButton(createButton, true);
        createButton.addActionListener(e -> {
            String groupName = groupNameField.getText().trim();
            if (groupName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入群聊名称", "输入错误", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<String> selected = selectionList.getSelectedValuesList();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请至少选择一位好友", "输入错误", JOptionPane.WARNING_MESSAGE);
                return;
            }

            groupsModel.addElement(groupName);
            dialog.dispose();
        });

        JButton cancelButton = new JButton("取消");
        styleButton(cancelButton, true);
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // 自定义列表渲染器
    class ContactListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            // 处理 null 值
            String displayValue = (value == null) ? "" : value.toString();
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, displayValue, index, isSelected, cellHasFocus
            );

            label.setFont(customFont);
            label.setBorder(new EmptyBorder(8, 10, 8, 10));
            label.setIcon(new ImageIcon(getClass().getResource(Config.IMAGE_PATH + "/favicon.ico")));

            if (isSelected) {
                label.setBackground(new Color(220, 230, 255));
                label.setForeground(new Color(50, 50, 50));
            } else {
                label.setBackground(panelColor);
                label.setForeground(new Color(80, 80, 80));
            }

            // 为被拉黑的好友设置特殊样式
            if (list == blockedList && value != null) {
                String cleanValue = value.toString().replace(" (已拉黑)", "");
                label.setForeground(new Color(150, 150, 150));
            }

            // 设置客户端属性
            if (value != null) {
                if (list == friendsList) {
                    label.putClientProperty("id", getFriendId(value.toString()));
                } else if (list == groupsList) {
                    label.putClientProperty("id", getGroupId(value.toString()));
                } else if (list == blockedList) {
                    String friendName = value.toString().replace(" (已拉黑)", "");
                    label.putClientProperty("id", getFriendId(friendName));
                }
            }

            return label;
        }
    }


    private String getFriendId(String nickname) {
        // 根据昵称查找好友ID
        List<Map<String, String>> friends = ChatCore.getFriendList();
        for (Map<String, String> friend : friends) {
            if (nickname.equals(friend.get("nickname"))) {
                return friend.get("friend_id");
            }
        }
        return "";
    }

    private String getGroupId(String groupName) {
        // 根据群名查找群ID
        List<Map<String, String>> groups = ChatCore.getGroupList();
        for (Map<String, String> group : groups) {
            if (groupName.equals(group.get("group_name"))) {
                return group.get("group_id");
            }
        }
        return "";
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            ChatInterface chatInterface = new ChatInterface();
            chatInterface.setVisible(true);
        });
    }
}