package data;

import core.Config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

//    public static void main(String[] args) {
//        initializeDatabase(Config.DB_PATH);
//    }

    public static void initializeDatabase(String dbPath) {
        File dbFile = new File(dbPath);
        boolean dbExists = dbFile.exists();

        if (!dbExists) {
            String url = "jdbc:sqlite:" + dbPath;

            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt = conn.createStatement()) {

                // 启用外键支持
                stmt.execute("PRAGMA foreign_keys = ON");

                // 创建用户表
                stmt.execute("CREATE TABLE IF NOT EXISTS UserInfo (" +
                        "user_id TEXT PRIMARY KEY," +
                        "nickname TEXT NOT NULL," +
                        "account TEXT NOT NULL," +
                        "password TEXT NOT NULL," +
                        "ip TEXT NOT NULL," +
                        "port INTEGER," +
                        "is_blocked BOOLEAN DEFAULT 0," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                // 创建群组表
                stmt.execute("CREATE TABLE IF NOT EXISTS Groups (" +
                        "group_id TEXT PRIMARY KEY," +
                        "group_name TEXT NOT NULL," +
                        "owner_id TEXT NOT NULL," +
                        "avatar_path TEXT," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "last_updated TIMESTAMP," +
                        "is_active BOOLEAN DEFAULT 1," +
                        "description TEXT," +
                        "invitation_code TEXT UNIQUE," +
                        "max_members INTEGER DEFAULT 100)");

                // 创建群组成员表
                stmt.execute("CREATE TABLE IF NOT EXISTS GroupMembers (" +
                        "group_id TEXT NOT NULL," +
                        "member_id TEXT NOT NULL," +
                        "role TEXT CHECK(role IN ('owner', 'admin', 'member')) DEFAULT 'member'," +
                        "join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "PRIMARY KEY (group_id, member_id)," +
                        "FOREIGN KEY (group_id) REFERENCES Groups(group_id) ON DELETE CASCADE)");

                // 待发送请求表
                stmt.execute("CREATE TABLE IF NOT EXISTS PendingRequests (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id TEXT NOT NULL," +
                        "request_url TEXT NOT NULL," +
                        "json_body TEXT NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                // 违禁词表
                stmt.execute("CREATE TABLE IF NOT EXISTS DisableWords (" +
                        "word TEXT PRIMARY KEY)");

                // 创建用户禁言表
                stmt.execute("CREATE TABLE IF NOT EXISTS UserMutes (" +
                        "user_id TEXT PRIMARY KEY," +
                        "mute_end_time TIMESTAMP NOT NULL," +
                        "FOREIGN KEY (user_id) REFERENCES UserInfo(user_id) ON DELETE CASCADE)");


                // 索引
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_account ON UserInfo(account)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_group_owner ON Groups(owner_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_group_active ON Groups(is_active)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_members_role ON GroupMembers(role)");

                System.out.println("Database initialized successfully");

            } catch (SQLException e) {
                System.err.println("Database initialization failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}