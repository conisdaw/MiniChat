package data;

import core.Config;

import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class UserMute {
    private static final String DB_URL = "jdbc:sqlite:" + Config.DB_PATH;

    public static boolean muteUser(String userId, int minutes) {
        Instant muteEnd = Instant.now().plus(minutes, ChronoUnit.MINUTES);
        Timestamp muteEndTime = Timestamp.from(muteEnd);

        String sql = "INSERT OR REPLACE INTO UserMutes (user_id, mute_end_time) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setTimestamp(2, muteEndTime);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean unmuteUser(String userId) {
        String sql = "DELETE FROM UserMutes WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isUserMuted(String userId) {
        String sql = "SELECT mute_end_time FROM UserMutes WHERE user_id = ?";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp muteEnd = rs.getTimestamp("mute_end_time");
                    if (muteEnd == null || !muteEnd.after(now)) {
                        unmuteUser(userId);
                        return false;
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}