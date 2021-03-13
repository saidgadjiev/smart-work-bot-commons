package ru.gadjini.telegram.smart.bot.commons.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserSettingsDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public UserSettingsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createDefaultSettings(String botName, int userId) {
        jdbcTemplate.update(
                "INSERT INTO user_settings(bot_name, user_id, smart_file) VALUES (?, ?, true)",
                ps -> {
                    ps.setString(1, botName);
                    ps.setInt(2, userId);
                }
        );
    }

    public void smartFileFeature(String botName, int userId, boolean enable) {
        jdbcTemplate.update(
                "UPDATE user_settings SET smart_file = ? where bot_name = ? and user_id = ?",
                ps -> {
                    ps.setBoolean(1, enable);
                    ps.setString(2, botName);
                    ps.setInt(3, userId);
                }
        );
    }

    public Boolean getSmartFileFeatureEnabledOrDefault(String botName, int userId) {
        return jdbcTemplate.query(
                "SELECT smart_file FROM user_settings WHERE bot_name = ? AND user_id = ?",
                ps -> {
                    ps.setString(1, botName);
                    ps.setInt(2, userId);
                },
                rs -> {
                    if (rs.next()) {
                        return rs.getBoolean("smart_file");
                    }

                    throw new RuntimeException("User settings not found for user " + userId);
                }
        );
    }
}
