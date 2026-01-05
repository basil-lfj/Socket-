import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    // 存储所有在线客户端的处理线程 [cite: 42, 70]
    public static final List<ClientHandler> ClientList = Collections.synchronizedList(new ArrayList<>());
    private static int ClientCount = 0;

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8080); // 监听8080端口 [cite: 9]
        System.out.println(">> Server started on port 8080...");

        // 启动控制台监听线程，处理服务器输入的命令 [cite: 31]
        new Thread(new Listener(ClientList)).start();

        while (true) {
            try {
                Socket client = server.accept(); // 接收客户端连接 [cite: 50]
                ClientCount++;
                ClientHandler handler = new ClientHandler(client, ClientCount);
                ClientList.add(handler); // 加入在线列表 [cite: 53]
                handler.start(); // 启动线程处理该客户端 [cite: 55]
            } catch (IOException e) {
                break;
            }
        }
    }

    // 统一发送消息或文件的方法 [cite: 112, 124]
    public static void sendToClient(ClientHandler handler, int fromID, String order, String message, String encoding) {
        try {
            DataOutputStream dos = new DataOutputStream(handler.getSocket().getOutputStream());
            String prefix = (fromID == 0) ? "Server: " : "Client " + fromID + ": ";
            
            if (order.equals("-t")) { // 发送文本 [cite: 115, 136]
                dos.writeUTF(prefix + message + "\n");
            } else if (order.equals("-f")) { // 发送文件 [cite: 117, 155]
                dos.writeUTF(prefix + "(file) " + message + "\n");
                // 简化版文件传输逻辑 [cite: 163]
                dos.writeUTF(message); 
            }
            dos.flush();
        } catch (IOException e) {
            System.out.println(">> Send Error: " + e.getMessage());
        }
    }
}