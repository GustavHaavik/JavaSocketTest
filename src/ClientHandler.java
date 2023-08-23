import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread {
    public static List<ClientHandler> clientHandlers = new ArrayList<>();
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private final String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            username = reader.readLine();
            clientHandlers.add(this);

            broadcastMessage("SERVER: " + username + " joined the chat");

        } catch (IOException e) {
            closeClient();
            throw new RuntimeException(e);
        }
    }

    private void broadcastMessage(String s) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler == this) continue;

            try {
                clientHandler.writer.write(s + "\n");
                clientHandler.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeClient() {
        removeClientHandler();

        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + username + " left the chat");
    }

    @Override
    public void run() {
        String message;

        while (socket.isConnected()) {
            try {
                message = reader.readLine();
                broadcastMessage(message);
            } catch (IOException e) {
                closeClient();
                throw new RuntimeException(e);
            }
        }
    }
}
