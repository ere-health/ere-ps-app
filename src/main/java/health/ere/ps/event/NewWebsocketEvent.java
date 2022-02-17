package health.ere.ps.event;

import javax.websocket.Session;

public class NewWebsocketEvent {
    private Session session;

    public NewWebsocketEvent(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return this.session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    
}
