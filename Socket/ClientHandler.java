import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private int num;

    public ClientHandler(Socket socket, int num) {
        this.socket = socket;
        this.num = num;
    }

    public Socket getSocket() { return socket; }
    public int getNum() { return num; }

    @Override
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            // 发送欢迎信息 [cite: 86]
            Server.sendToClient(this, 0, "-t", "Welcome! Your ID is " + num, "UTF-8");

            String input;
            while ((input = dis.readUTF()) != null) { // 循环读取客户端消息 [cite: 88]
                System.out.println(">> Client " + num + " sent: " + input);
                // 这里可以根据 order 解析命令进行转发 [cite: 90, 107]
            }
        } catch (IOException e) {
            System.out.println(">> Client " + num + " disconnected.");
        } finally {
            Server.ClientList.remove(this); // 断开连接后移除 [cite: 59]
        }
    }
}