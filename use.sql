-- 用户自身信息表 (单条记录)
CREATE TABLE IF NOT EXISTS UserInfo (
    user_id TEXT PRIMARY KEY,      -- 用户唯一标识
    last_ip TEXT NOT NULL,         -- 上次登录IP
    nickname TEXT NOT NULL DEFAULT 'User',   -- 昵称
    last_port INTEGER NOT NULL,   -- 上次登录端口
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 好友列表
CREATE TABLE IF NOT EXISTS Friends (
     friend_id TEXT PRIMARY KEY,    -- 好友用户ID
     ip_address TEXT NOT NULL,     -- 好友最近IP
     port INTEGER NOT NULL,        -- 好友最近端口
     last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     nickname TEXT,                -- 好友昵称
     remark TEXT,                  -- 备注名
     avatar_path TEXT,             -- 本地头像存储路径
     is_blocked BOOLEAN DEFAULT 0  -- 是否拉黑 (0=正常, 1=拉黑)
);

-- 单人聊天记录
CREATE TABLE IF NOT EXISTS SingleChatHistory (
     chat_id INTEGER PRIMARY KEY AUTOINCREMENT,
     peer_id TEXT NOT NULL,        -- 对方用户ID
     message_type TEXT CHECK(message_type IN ('text', 'image', 'file', 'system')) DEFAULT 'text',
     message TEXT NOT NULL,        -- 消息内容
     is_sent BOOLEAN NOT NULL,     -- 0=接收/1=发送
     timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     ip_address TEXT,              -- 消息来源IP(可选)
     port INTEGER                  -- 消息来源端口(可选)
);

-- 多人聊天记录
CREATE TABLE IF NOT EXISTS GroupMessages (
     message_id INTEGER PRIMARY KEY AUTOINCREMENT,
     group_id TEXT NOT NULL,           -- 群组ID
     sender_id TEXT NOT NULL,          -- 发送者ID
     message_type TEXT CHECK(message_type IN ('text', 'image', 'file', 'system')) DEFAULT 'text',
     content TEXT NOT NULL,            -- 消息内容或文件路径
     timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     is_sent BOOLEAN NOT NULL,     -- 0=接收/1=发送
     ip_address TEXT,                  -- 发送者IP
     port INTEGER,                     -- 发送者端口

    FOREIGN KEY (group_id) REFERENCES Groups(group_id) ON DELETE CASCADE
    );

--  群组信息表
CREATE TABLE IF NOT EXISTS Groups (
      group_id TEXT PRIMARY KEY,        -- 群组唯一ID
      group_name TEXT NOT NULL,         -- 群组名称
      owner_id TEXT NOT NULL,           -- 群主用户ID
      avatar_path TEXT,                 -- 群头像本地路径
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
      last_updated TIMESTAMP,           -- 最后更新时间
      is_active BOOLEAN DEFAULT 1,      -- 群是否活跃
      description TEXT,                 -- 群描述
      invitation_code TEXT UNIQUE,      -- 入群邀请码
      max_members INTEGER DEFAULT 100   -- 最大成员数
);

-- 群组成员表
CREATE TABLE IF NOT EXISTS GroupMembers (
    group_id TEXT NOT NULL,           -- 群组ID
    member_id TEXT NOT NULL,          -- 成员用户ID
    role TEXT CHECK(role IN ('owner', 'admin', 'member')) DEFAULT 'member', -- 成员角色
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 加入时间
    last_seen TIMESTAMP,              -- 最后查看时间
    nickname_in_group TEXT,           -- 群内昵称
    is_muted BOOLEAN DEFAULT 0,       -- 是否被禁言
    is_blocked BOOLEAN DEFAULT 0,     -- 是否被踢出/拉黑

    PRIMARY KEY (group_id, member_id),
    FOREIGN KEY (group_id) REFERENCES Groups(group_id) ON DELETE CASCADE
    );

-- 群组设置表
CREATE TABLE IF NOT EXISTS GroupSettings (
     group_id TEXT PRIMARY KEY,        -- 群组ID
     allow_invites BOOLEAN DEFAULT 1,  -- 是否允许成员邀请
     approval_required BOOLEAN DEFAULT 0, -- 入群是否需要批准
     history_visible BOOLEAN DEFAULT 1, -- 新成员是否可见历史消息
     max_admins INTEGER DEFAULT 3,     -- 最大管理员数量
     pinned_message_id INTEGER,        -- 置顶消息ID

     FOREIGN KEY (group_id) REFERENCES Groups(group_id) ON DELETE CASCADE
    );

-- private static void createIndexes(Statement stmt) throws SQLException {
--         stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_friends_update ON Friends(last_updated)");
--         stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_singlechat_peer ON SingleChatHistory(peer_id)");
--         stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_group_members ON GroupMembers(group_id, member_id)");
--         stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_group_messages ON GroupMessages(group_id, timestamp)");
--         stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_group_notifications ON GroupNotifications(user_id, is_read)");