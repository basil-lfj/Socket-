import java.io.*;
import java.util.*;

public class Listener implements Runnable {
    private List<ClientHandler> list;
    private String fileEncoding = "UTF-8";

    public Listener(List<ClientHandler> list) { this.list = list; }

    public void run() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String cmd = sc.nextLine(); // 读取控制台输入 [cite: 189]
            String[] parts = cmd.split(" ");
            
            if (parts[0].equals("-l")) { // 查看在线列表 [cite: 244, 300]
                for (ClientHandler h : list) System.out.println("Online: ID " + h.getNum());
            } else if (parts[0].equals("-c")) { // 私聊: -c <id> -t <msg> [cite: 201, 298]
                int targetId = Integer.parseInt(parts[1]);
                for (ClientHandler h : list) {
                    if (h.getNum() == targetId) Server.sendToClient(h, 0, parts[2], parts[3], fileEncoding);
                }
            } else if (parts[0].equals("exit")) { // 退出服务器 [cite: 17, 191]
                System.exit(0);
            }
        }
    }
}