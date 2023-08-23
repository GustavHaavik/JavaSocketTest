import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private final String username;

    private Client(Socket socket, String username) {
        this.socket = socket;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            this.username = username;
        } catch (IOException e) {
            closeClient();
            throw new RuntimeException(e);
        }
    }

    private void closeClient() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage() {
        try {
            writer.write(username + ": " + "\n");
            writer.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String message = scanner.nextLine();
                writer.write(username + ": " + message + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            closeClient();
            e.printStackTrace();
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    String message = reader.readLine();
                    System.out.println(message);
                }
            } catch (IOException e) {
                closeClient();
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        Socket socket = new Socket("localhost", 9000);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }
}
