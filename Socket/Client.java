import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static String fileEncoding = "UTF-8";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String host = "localhost";
        int port = 8080;

        // 支持命令行参数
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println(">> 端口号格式错误，使用默认端口8080");
            }
        }

        // 如果没有提供命令行参数，提示用户输入
        if (args.length == 0) {
            System.out.print(">> 请输入服务器IP地址（默认localhost）: ");
            String inputHost = sc.nextLine().trim();
            if (!inputHost.isEmpty()) {
                host = inputHost;
            }

            System.out.print(">> 请输入服务器端口号（默认8080）: ");
            String inputPort = sc.nextLine().trim();
            if (!inputPort.isEmpty()) {
                try {
                    port = Integer.parseInt(inputPort);
                } catch (NumberFormatException e) {
                    System.out.println(">> 端口号格式错误，使用默认端口8080");
                }
            }
        }

        try {
            System.out.println(">> 正在连接到 " + host + ":" + port + "...");
            Socket socket = new Socket(host, port); // 连接到指定服务器 [cite: 322]
            System.out.println(">> Connected to server!");

            // 启动接收线程，专门收服务器发来的消息 [cite: 367]
            new ServerHandler(socket).start();

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String msg = sc.nextLine();
                if (msg.equals("exit")) break; // 退出客户端 [cite: 26, 373]
                dos.writeUTF(msg); // 发送消息给服务器，writeUTF支持长文本（最多65535字节UTF-8）[cite: 423]
                dos.flush();
            }
            socket.close();
        } catch (IOException e) {
            System.out.println(">> 连接错误: 无法连接到服务器!");
            System.out.println(">> 请检查IP地址和端口号是否正确，并确保服务器已启动。");
        }
    }
}
