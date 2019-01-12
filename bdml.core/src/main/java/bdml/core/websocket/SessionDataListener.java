package bdml.core.websocket;


import bdml.services.api.helper.DataListener;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class SessionDataListener implements DataListener {
    private Session session;

    public SessionDataListener(Session session) {
        this.session = session;
    }

    @Override
    public void update(String identifier) {
        if(session.isOpen()) {
            try {
                session.getRemote().sendString(identifier);
            } catch (IOException e) {
                session.close(1011, "Internal Error");
            }
        }
    }
}
