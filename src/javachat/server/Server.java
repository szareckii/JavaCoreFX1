package javachat.server;

import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();
        ServerSocket server = null;
        Socket socket;

        final int PORT = 8189;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент пытается подключиться ");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(String msg){
        for (ClientHandler c:clients) {
            c.sendMsg(msg);
        }
    }

// метод приватной отправки сообщения конкретному получателю

    public void privateMsg(ClientHandler clientHandler, String[] nickMsg){
        ClientHandler sendToClientHandler = findClintHandler(nickMsg[1]);

        if (sendToClientHandler != null) {
            sendToClientHandler.sendMsg("Приватное сообщение от " + clientHandler.nick + ": "+ nickMsg[2]);
        }
        else {
            clientHandler.sendMsg("Получатель не найден!");
        }

    }

// метод поиска клиента по его нику

    private ClientHandler findClintHandler(String nick) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).nick.equals(nick)) {
                return clients.get(i);
            }
        }
        return null;
    }

    public boolean subscribe(ClientHandler clientHandler, String nick){
        ClientHandler sendToClientHandler = findClintHandler(nick);

        if (sendToClientHandler == null) {
            clients.add(clientHandler);
            return true;
        }

        return false;
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }
}
