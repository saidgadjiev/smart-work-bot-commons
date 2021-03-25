package ru.gadjini.telegram.smart.bot.commons.dao;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public abstract class QueueDao {

    public static final String MIN_LAST_RUN_AT = "last_run_at";

    public static final String MIN_LAST_RUN_AT_ID = "id";

    public static final String POLL_ORDER_BY = " ORDER BY qu.attempts, qu.next_run_at, qu.id ";

    public static final String DELETE_COMPLETED_INTERVAL = "interval '3 days'";

    private ServerProperties serverProperties;

    private static final String POLL_UPDATE_LIST = " status = 1, last_run_at = now(), attempts = attempts + 1, started_at = COALESCE(started_at, now()), server = %s ";

    public static String getUpdateList(int serverNumber) {
        return String.format(POLL_UPDATE_LIST, serverNumber);
    }

    @Autowired
    public void setServerProperties(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    public final void setExceptionStatus(int id, String exception) {
        getJdbcTemplate().update(
                "UPDATE " + getQueueName() + " SET status = ?, exception = ?, suppress_user_exceptions = TRUE, completed_at = now() WHERE id = ?",
                ps -> {
                    ps.setInt(1, QueueItem.Status.EXCEPTION.getCode());
                    ps.setString(2, exception);
                    ps.setInt(3, id);
                }
        );
    }

    public final void setCompleted(int id) {
        getJdbcTemplate().update(
                "UPDATE " + getQueueName() + " SET status = ?, completed_at = now() WHERE id = ?",
                ps -> {
                    ps.setInt(1, QueueItem.Status.COMPLETED.getCode());
                    ps.setInt(2, id);
                }
        );
    }

    public final void setWaitingAndDecrementAttempts(int id) {
        getJdbcTemplate().update("UPDATE " + getQueueName() + " SET status = 0, attempts = GREATEST(0, attempts - 1) WHERE id = ?",
                ps -> ps.setInt(1, id));
    }

    public final void setWaiting(int id) {
        getJdbcTemplate().update("UPDATE " + getQueueName() + " SET status = 0 WHERE id = ?",
                ps -> ps.setInt(1, id));
    }

    public final void setWaitingAndDecrementAttempts(int id, long seconds, String exception) {
        getJdbcTemplate().update("UPDATE " + getQueueName() + " SET status = 0, " +
                        "next_run_at = now() + interval '" + seconds + " seconds', exception = ?, attempts = GREATEST(0, attempts - 1) WHERE id = ?",
                ps -> {
                    ps.setString(1, exception);
                    ps.setInt(2, id);
                });
    }

    public final void setWaiting(int id, long seconds, String exception) {
        getJdbcTemplate().update("UPDATE " + getQueueName() + " SET status = 0, " +
                        "next_run_at = now() + interval '" + seconds + " seconds', exception = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, exception);
                    ps.setInt(2, id);
                });
    }

    public void resetProcessing() {
        getJdbcTemplate().update(
                "UPDATE " + getQueueName() + " SET status = 0 " +
                        "WHERE status = 1 " + getQueueDaoDelegate().getBaseAdditionalClause() + " AND server = " + serverProperties.getNumber()
        );
    }

    public final void deleteById(int id) {
        getJdbcTemplate().update(
                "DELETE FROM " + getQueueName() + " WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
    }

    public final String getException(int id) {
        return getJdbcTemplate().query(
                "SELECT exception FROM " + getQueueName() + " WHERE id = ?",
                ps -> {
                    ps.setInt(1, id);
                },
                rs -> rs.next() ? rs.getString(QueueItem.EXCEPTION) : null
        );
    }

    public final boolean exists(int id) {
        return BooleanUtils.toBoolean(getJdbcTemplate().query(
                "SELECT TRUE FROM " + getQueueName() + " WHERE id =?",
                ps -> {
                    ps.setInt(1, id);
                },
                ResultSet::next
        ));
    }

    public final void setProgressMessageId(int id, int progressMessageId) {
        getJdbcTemplate().update("UPDATE " + getQueueName() + " SET progress_message_id = ? WHERE id = ?",
                ps -> {
                    ps.setInt(1, progressMessageId);
                    ps.setInt(2, id);
                });
    }

    public final long countByStatusForToday(QueueItem.Status status) {
        return getJdbcTemplate().query(
                "SELECT COUNT(*) as cnt FROM " + getQueueName() + " WHERE status = ? AND created_at::date = current_date " + getQueueDaoDelegate().getBaseAdditionalClause(),
                ps -> ps.setInt(1, status.getCode()),
                rs -> rs.next() ? rs.getLong("cnt") : 0
        );
    }

    public final long countByStatusAllTime(QueueItem.Status status) {
        return getJdbcTemplate().query(
                "SELECT COUNT(*) as cnt FROM " + getQueueName() + " WHERE status = ? " + getQueueDaoDelegate().getBaseAdditionalClause(),
                ps -> ps.setInt(1, status.getCode()),
                rs -> rs.next() ? rs.getLong("cnt") : 0
        );
    }

    public final Long countActiveUsersForToday() {
        return getJdbcTemplate().query(
                "SELECT count(DISTINCT user_id) as cnt FROM " + getQueueName() + " WHERE created_at::date = current_date " + getQueueDaoDelegate().getBaseAdditionalClause(),
                rs -> rs.next() ? rs.getLong("cnt") : 0
        );
    }

    public final void deleteByIdAndStatuses(int id, Set<QueueItem.Status> statuses) {
        getJdbcTemplate().update(
                "DELETE FROM " + getQueueName() + " WHERE id = ? AND status IN(" + statuses.stream()
                        .map(s -> String.valueOf(s.getCode())).collect(Collectors.joining(",")) + ")",
                ps -> ps.setInt(1, id)
        );
    }

    public void setException(int id, String exception) {
        getJdbcTemplate().update(
                "UPDATE " + getQueueName() + " SET exception = ? WHERE id = ?",
                ps -> {
                    ps.setString(2, exception);
                    ps.setInt(3, id);
                }
        );
    }

    public ZonedDateTime getWaitingMaxNextRunAt() {
        return getJdbcTemplate().query(
                "SELECT MAX(next_run_at) as next_run_at FROM " + getQueueName() + " WHERE status = 0 " + getQueueDaoDelegate().getBaseAdditionalClause(),
                rs -> {
                    if (rs.next()) {
                        Timestamp nextRunAt = rs.getTimestamp("next_run_at");

                        if (nextRunAt != null) {
                            return ZonedDateTime.of(nextRunAt.toLocalDateTime(), ZoneOffset.UTC);
                        }
                    }

                    return null;
                }
        );
    }

    public Map<String, Object> getProcessingMinLastRunAt() {
        return getJdbcTemplate().query(
                "SELECT " + MIN_LAST_RUN_AT_ID + ", " + MIN_LAST_RUN_AT + " FROM " + getQueueName() + " WHERE status = 1 " + getQueueDaoDelegate().getBaseAdditionalClause() +
                        " ORDER BY last_run_at LIMIT 1",
                rs -> {
                    Map<String, Object> result = new HashMap<>();

                    if (rs.next()) {
                        Timestamp lastRunAt = rs.getTimestamp(MIN_LAST_RUN_AT);

                        if (lastRunAt != null) {
                            result.put(MIN_LAST_RUN_AT, ZonedDateTime.of(lastRunAt.toLocalDateTime(), ZoneOffset.UTC));
                        }

                        result.put(MIN_LAST_RUN_AT_ID, rs.getInt(MIN_LAST_RUN_AT_ID));
                    }

                    return result;
                }
        );
    }

    public ZonedDateTime getWaitingMinCreatedAt() {
        return getJdbcTemplate().query(
                "SELECT MIN(created_at) as created_at FROM " + getQueueName() + " WHERE status = 0 " + getQueueDaoDelegate().getBaseAdditionalClause(),
                rs -> {
                    if (rs.next()) {
                        Timestamp createdAt = rs.getTimestamp("created_at");

                        if (createdAt != null) {
                            return ZonedDateTime.of(createdAt.toLocalDateTime(), ZoneOffset.UTC);
                        }
                    }

                    return null;
                }
        );
    }

    public final Set<Integer> getOrphanItems(Set<Integer> itemsToCheck) {
        if (itemsToCheck.isEmpty()) {
            return Set.of();
        }
        String in = itemsToCheck.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<Integer> reallyProcessing = getJdbcTemplate().query(
                "SELECT id FROM " + getQueueName() + " WHERE id IN(" + in + ")",
                (rs, rowNum) -> rs.getInt(QueueItem.ID)
        );

        Set<Integer> result = new HashSet<>(itemsToCheck);

        result.removeAll(reallyProcessing);

        return result;
    }

    public final String getQueueName() {
        return getQueueDaoDelegate().getQueueName();
    }

    public abstract JdbcTemplate getJdbcTemplate();

    public abstract QueueDaoDelegate getQueueDaoDelegate();
}
