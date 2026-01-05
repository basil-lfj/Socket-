import java.io.*;
import java.net.*;

public class ServerHandler extends Thread {
    private Socket socket;
    // 缓冲区大小：支持20KB+长文本消息
    private static final int BUFFER_SIZE = 32768; // 32KB缓冲区
    
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
        } catch (EOFException e) {
            System.out.println("\n>> Connection closed normally.");
        } catch (IOException e) {
            System.out.println("\n>> Connection lost: " + e.getMessage());
        }
    }
}