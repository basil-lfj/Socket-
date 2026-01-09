import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    // 存储所有在线客户端的处理线程 [cite: 42, 70]
    public static final List<ClientHandler> ClientList = Collections.synchronizedList(new ArrayList<>());
    private static int ClientCount = 0;

    // 验证端口号是否有效
    private static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        int port = 8080;

        // 支持命令行参数
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
                if (!isValidPort(port)) {
                    System.out.println(">> 错误：端口号必须在0-65535范围内");
                    System.exit(0);
                }
            } catch (NumberFormatException e) {
                System.out.println(">> 错误：端口号格式不正确，请输入有效的数字");
                System.exit(0);
            }
        }

        // 如果没有提供命令行参数，提示用户输入
        if (args.length == 0) {
            System.out.print(">> 请输入服务器端口号（默认8080）: ");
            String inputPort = sc.nextLine().trim();
            if (!inputPort.isEmpty()) {
                try {
                    port = Integer.parseInt(inputPort);
                    if (!isValidPort(port)) {
                        System.out.println(">> 错误：端口号必须在0-65535范围内");
                        System.exit(0);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(">> 错误：端口号格式不正确，请输入有效的数字");
                    System.exit(0);
                }
            }
        }

        try {
            ServerSocket server = new ServerSocket(port); // 监听指定端口 [cite: 9]
            System.out.println(">> Server started on port " + port + "...");

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
        } catch (IOException e) {
            System.out.println(">> 服务器启动失败: " + e.getMessage());
            System.out.println(">> 请检查端口号是否被占用，或尝试使用其他端口。");
        }
    }

    // 统一发送消息或文件的方法 [cite: 112, 124]
    public static void sendToClient(ClientHandler handler, int fromID, String order, String message, String encoding) {
        try {
            DataOutputStream dos = new DataOutputStream(handler.getSocket().getOutputStream());
            String prefix = (fromID == 0) ? "Server: " : "Client " + fromID + ": ";
            
            if (order.equals("-t")) { // 发送文本 [cite: 115, 136]
                // writeUTF支持最多65535字节的UTF-8编码数据，可支持20KB+长文本消息
                dos.writeUTF(prefix + message + "\n");
                dos.flush();
            } else if (order.equals("-f")) { // 发送文件 [cite: 117, 155]
                dos.writeUTF(prefix + "(file) " + message + "\n");
                // 简化版文件传输逻辑 [cite: 163]
                dos.writeUTF(message);
            }
        } catch (IOException e) {
            System.out.println(">> Send Error: " + e.getMessage());
        }
    }
}
