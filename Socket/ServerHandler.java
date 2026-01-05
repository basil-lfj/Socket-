import java.io.*;
import java.net.*;

public class ServerHandler extends Thread {
    private Socket socket;
    public ServerHandler(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String line;
            while ((line = dis.readUTF()) != null) { // 阻塞式读取 [cite: 434]
                System.out.print(line); // 打印服务器发来的消息 [cite: 437]
                System.out.print(">> ");
            }
        } catch (IOException e) {
            System.out.println(">> Connection lost.");
        }
    }
}