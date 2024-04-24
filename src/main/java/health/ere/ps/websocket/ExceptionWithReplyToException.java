package health.ere.ps.websocket;

import jakarta.websocket.Session;

public class ExceptionWithReplyToException extends Exception {

    protected Exception exception;
    protected Session replyTo;
    protected String messageId;

    public ExceptionWithReplyToException(Exception exception, Session replyTo, String messageId) {
        this.exception = exception;
        this.replyTo = replyTo;
        this.messageId = messageId;
    }

    public Exception getException() {
        return this.exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Session getReplyTo() {
        return this.replyTo;
    }

    public void setReplyTo(Session replyTo) {
        this.replyTo = replyTo;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
