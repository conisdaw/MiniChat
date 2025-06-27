package data;

import core.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisableSQL {
    private static final String DB_URL = "jdbc:sqlite:" + Config.DB_PATH;
    private static final String CLEAR_TABLE_SQL = "DELETE FROM DisableWords";
    private static final String INSERT_SQL = "INSERT INTO DisableWords (word) VALUES (?)";
    private static final String LOAD_ALL_SQL = "SELECT word FROM DisableWords";
    private static final String REMOVE_SQL = "DELETE FROM DisableWords WHERE word = ?";

    /**
     * 移除指定的违禁词
     * @param word 需要移除的违禁词
     */
    public static boolean removeWord(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(REMOVE_SQL)) {
            conn.setAutoCommit(false);
            pstmt.setString(1, word);
            pstmt.addBatch();
            pstmt.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("移除违禁词失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 违禁词
     * @param words 违禁词列表
     */
    public static void saveAll(List<String> words) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);

            // 清空表
            stmt.executeUpdate(CLEAR_TABLE_SQL);

            // 批量插入新数据
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {
                for (String word : words) {
                    pstmt.setString(1, word);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // 提交事务
            conn.commit();
        } catch (SQLException e) {
            System.err.println("保存违禁词失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 加载所有违禁词
     * @return 违禁词列表
     */
    public static List<String> loadAll() {
        List<String> words = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(LOAD_ALL_SQL)) {

            while (rs.next()) {
                words.add(rs.getString("word"));
            }
        } catch (SQLException e) {
            System.err.println("加载违禁词失败: " + e.getMessage());
            e.printStackTrace();
        }
        return words;
    }
}