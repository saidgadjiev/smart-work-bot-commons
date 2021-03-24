package ru.gadjini.telegram.smart.bot.commons.domain;

import java.time.ZonedDateTime;

public class QueueItem {

    public static final String EXCEPTION = "exception";

    public static final String ID = "id";

    public static final String USER_ID = "user_id";

    public static final String REPLY_TO_MESSAGE_ID = "reply_to_message_id";

    public static final String CREATED_AT = "created_at";

    public static final String STARTED_AT = "started_at";

    public static final String COMPLETED_AT = "completed_at";

    public static final String LAST_RUN_AT = "last_run_at";

    public static final String STATUS = "status";

    public static final String QUEUE_POSITION = "queue_position";

    public static final String PROGRESS_MESSAGE_ID = "progress_message_id";

    public static final String SUPPRESS_USER_EXCEPTIONS = "suppress_user_exceptions";

    public static final String SERVER = "server";

    public static final String ATTEMPTS = "attempts";

    private int id;

    private int userId;

    private Integer replyToMessageId;

    private Integer progressMessageId;

    private ZonedDateTime createdAt;

    private ZonedDateTime startedAt;

    private ZonedDateTime lastRunAt;

    private ZonedDateTime completedAt;

    private Status status;

    private boolean suppressUserExceptions;

    private int queuePosition;

    private String exception;

    private int server;

    private int attempts;

    public final int getId() {
        return id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    public final int getUserId() {
        return userId;
    }

    public final void setUserId(int userId) {
        this.userId = userId;
    }

    public final Integer getReplyToMessageId() {
        return replyToMessageId;
    }

    public final void setReplyToMessageId(Integer replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public final ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public final void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public final ZonedDateTime getStartedAt() {
        return startedAt;
    }

    public final void setStartedAt(ZonedDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public final ZonedDateTime getLastRunAt() {
        return lastRunAt;
    }

    public final void setLastRunAt(ZonedDateTime lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public final ZonedDateTime getCompletedAt() {
        return completedAt;
    }

    public final void setCompletedAt(ZonedDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public final Status getStatus() {
        return status;
    }

    public final void setStatus(Status status) {
        this.status = status;
    }

    public final Integer getProgressMessageId() {
        return progressMessageId;
    }

    public final void setProgressMessageId(Integer progressMessageId) {
        this.progressMessageId = progressMessageId;
    }

    public final int getQueuePosition() {
        return queuePosition;
    }

    public final void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public final boolean isSuppressUserExceptions() {
        return suppressUserExceptions;
    }

    public final void setSuppressUserExceptions(boolean suppressUserExceptions) {
        this.suppressUserExceptions = suppressUserExceptions;
    }

    public final String getException() {
        return exception;
    }

    public final void setException(String exception) {
        this.exception = exception;
    }

    public int getServer() {
        return server;
    }

    public void setServer(int server) {
        this.server = server;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public enum Status {

        WAITING(0),

        PROCESSING(1),

        EXCEPTION(2),

        COMPLETED(3),

        BLOCKED(4),

        UNKNOWN(-1);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status fromCode(int code) {
            for (Status status : values()) {
                if (status.code == code) {
                    return status;
                }
            }

            return UNKNOWN;
        }
    }
}
