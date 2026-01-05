import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static String fileEncoding = "UTF-8";

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8080); // 连接本地服务器 [cite: 322]
            System.out.println(">> Connected to server!");

            // 启动接收线程，专门收服务器发来的消息 [cite: 367]
            new ServerHandler(socket).start();

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            Scanner sc = new Scanner(System.in);

            while (true) {
                String msg = sc.nextLine();
                if (msg.equals("exit")) break; // 退出客户端 [cite: 26, 373]
                dos.writeUTF(msg); // 发送消息给服务器 [cite: 423]
                dos.flush();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}