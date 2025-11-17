package war;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONObject;

import java.net.InetSocketAddress;

public class WarEventSocketServer extends WebSocketServer {

    public WarEventSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    /**
     * Sends a JSON event to all connected clients.
     * @param event The JSON object to broadcast.
     */
    public void broadcastEvent(JSONObject event) {
        broadcast(event.toString());
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started successfully on port: " + getPort());
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // We can implement receiving messages from the client here later (e.g., for pause/play)
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
}