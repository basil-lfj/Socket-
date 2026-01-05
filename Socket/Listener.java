import java.io.*;
import java.util.*;

public class Listener implements Runnable {
    private List<ClientHandler> list;
    private String fileEncoding = "UTF-8";

    public Listener(List<ClientHandler> list) { this.list = list; }

    public void run() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            try {
                String cmd = sc.nextLine(); // 读取控制台输入 [cite: 189]
                if (cmd.trim().isEmpty()) continue;
                
                String[] parts = cmd.split(" ");
                
                if (parts[0].equals("-l")) { // 查看在线列表 [cite: 244, 300]
                    synchronized (list) {
                        for (ClientHandler h : list) System.out.println("Online: ID " + h.getNum());
                    }
                } else if (parts[0].equals("-c")) { // 私聊: -c <id> -t <msg> [cite: 201, 298]
                    try {
                        int targetId = Integer.parseInt(parts[1]);
                        synchronized (list) {
                            for (ClientHandler h : list) {
                                if (h.getNum() == targetId) Server.sendToClient(h, 0, parts[2], parts[3], fileEncoding);
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(">> 错误: ID必须是数字");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(">> 错误: 命令格式应为 -c <id> -t <msg>");
                    }
                } else if (parts[0].equals("exit")) { // 退出服务器 [cite: 17, 191]
                    System.exit(0);
                } else {
                    System.out.println(">> 未知命令: " + parts[0]);
                    System.out.println(">> 可用命令: -l (查看在线列表), -c <id> -t <msg> (私聊), exit (退出)");
                }
            } catch (Exception e) {
                System.out.println(">> 命令执行错误: " + e.getMessage());
            }
        }
    }
}