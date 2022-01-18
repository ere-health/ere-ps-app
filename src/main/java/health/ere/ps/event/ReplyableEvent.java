package health.ere.ps.event;

import java.io.Serializable;

public interface ReplyableEvent {
    public String getType();
    public Serializable getPayload();
    public String getReplyToMessageId();
}
