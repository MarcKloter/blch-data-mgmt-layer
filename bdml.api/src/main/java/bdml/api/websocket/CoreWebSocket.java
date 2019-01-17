package bdml.api.websocket;

import bdml.core.CoreService;
import bdml.core.Core;
import bdml.core.domain.exceptions.AuthenticationException;
import bdml.core.domain.Account;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;

@WebSocket
public class CoreWebSocket {
    private Account account = null;
    private String handle = null;
    private Core core = CoreService.getInstance();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        ConnectRequest request = parseMessage(session, message);
        if(request == null)
            return;

        if (!isConnected()) {
            try {
                this.account = request.getAccount();
                this.handle = core.registerDataListener(this.account, new SessionDataListener(session));
                session.getRemote().sendString(this.handle);
            } catch(AuthenticationException e) {
                session.close(4002, "Invalid Account");
            }
        } else {
            session.close(4001, "Already Connected");
        }
    }


    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {
        if(isConnected()) {
            try {
                core.unregisterDataListener(this.account, this.handle);
            } catch (AuthenticationException e) {
                throw new IllegalStateException();
            }
        }
    }

    private boolean isConnected() {
        return this.handle != null;
    }

    private ConnectRequest parseMessage(Session session, String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(message, ConnectRequest.class);
        } catch(IOException e) {
            session.close(4000, "Invalid Message");
            return null;
        }
    }
}
