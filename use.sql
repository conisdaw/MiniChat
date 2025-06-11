PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS UserInfo (
    user_id TEXT PRIMARY KEY,
    nickname TEXT NOT NULL DEFAULT 'User',
    last_ip TEXT NOT NULL,
    last_port INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Friends (
    friend_id TEXT PRIMARY KEY,
    ip_address TEXT NOT NULL,
    port INTEGER NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nickname TEXT,
    remark TEXT,
    avatar_path TEXT,
    is_blocked BOOLEAN DEFAULT 0
);

CREATE TABLE IF NOT EXISTS SingleChatHistory (
    message_id INTEGER PRIMARY KEY AUTOINCREMENT,
    peer_id TEXT NOT NULL,
    message_type TEXT CHECK(message_type IN ('text', 'image', 'file')) DEFAULT 'text',
    message TEXT NOT NULL,
    is_sent BOOLEAN NOT NULL,
    ip_address TEXT,
    port INTEGER,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Groups (
    group_id TEXT PRIMARY KEY,
    group_name TEXT NOT NULL,
    owner_id TEXT NOT NULL,
    avatar_path TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP,
    is_active BOOLEAN DEFAULT 1,
    description TEXT,
    invitation_code TEXT UNIQUE,
    max_members INTEGER DEFAULT 100
);

CREATE TABLE IF NOT EXISTS GroupMembers (
    group_id TEXT NOT NULL,
    member_id TEXT NOT NULL,
    role TEXT CHECK(role IN ('owner', 'admin', 'member')) DEFAULT 'member',
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP,
    nickname_in_group TEXT,
    ip_address TEXT NOT NULL,
    port INTEGER NOT NULL,
    PRIMARY KEY (group_id, member_id),
    FOREIGN KEY (group_id) REFERENCES Groups(group_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS GroupMessages (
    message_id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id TEXT NOT NULL,
    sender_id TEXT NOT NULL,
    message_type TEXT CHECK(message_type IN ('text', 'image', 'file')) DEFAULT 'text',
    content TEXT NOT NULL,
    is_sent BOOLEAN NOT NULL,
    ip_address TEXT,
    port INTEGER,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES Groups(group_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS GroupSettings (
    group_id TEXT PRIMARY KEY,
    allow_invites BOOLEAN DEFAULT 1,
    approval_required BOOLEAN DEFAULT 0,
    history_visible BOOLEAN DEFAULT 1,
    max_admins INTEGER DEFAULT 3,
    pinned_message_id INTEGER,
    FOREIGN KEY (group_id) REFERENCES Groups(group_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_friends_update ON Friends(last_updated);
CREATE INDEX IF NOT EXISTS idx_singlechat_peer ON SingleChatHistory(peer_id);
CREATE INDEX IF NOT EXISTS idx_group_members ON GroupMembers(group_id, member_id);
CREATE INDEX IF NOT EXISTS idx_group_messages ON GroupMessages(group_id, timestamp);