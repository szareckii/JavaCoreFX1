package javachat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    protected String nick;                                                  //!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private String login;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split(" ");

                            System.out.println(str);
                            if (token.length < 2) {
                                continue;
                            }

                            String newNick = server.getAuthService().getNicknameByLoginAndPassword(token[1], token[2]);

                            if (newNick != null) {
                                nick = newNick;
                                login = token[1];

                                if(server.subscribe(this, nick)) {
                                    sendMsg("/authok " + newNick);
                                    System.out.println("Клиент: " + nick + " подключился");
                                    break;
                                }
                                else {
                                    sendMsg("Такой логин уже используется! Введите другой. ");
                                }

                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            sendMsg("/end");
                            break;
                        }

                        //проверка условия отправки конкретному получателю
                        if (str.startsWith("/w ")) {
                            String[] token = str.split(" ", 3);

                            if (token.length < 3) {
                                continue;
                            }
                            server.privateMsg(this, token);
                        }

                        else {
                            server.broadcastMsg(nick + ": " + str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Клиент отключился");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
