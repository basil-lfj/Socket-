import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static String fileEncoding = "UTF-8";

    // 验证端口号是否有效
    private static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }

    // 验证IP地址格式是否有效
    private static boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        // 检查是否为localhost
        if (ip.equalsIgnoreCase("localhost")) {
            return true;
        }
        // 简单的IPv4格式验证
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    // 检查IP地址是否可达
    private static boolean isIPReachable(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(3000); // 3秒超时
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String host = "localhost";
        int port = 8080;

        // 支持命令行参数
        if (args.length >= 1) {
            host = args[0];
            if (!isValidIP(host)) {
                System.out.println(">> 错误：IP地址格式不正确 - " + host);
                System.out.println(">> 请输入有效的IP地址（如：127.0.0.1）或使用localhost");
                System.exit(0);
            }
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
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
            System.out.print(">> 请输入服务器IP地址（默认localhost）: ");
            String inputHost = sc.nextLine().trim();
            if (!inputHost.isEmpty()) {
                host = inputHost;
                if (!isValidIP(host)) {
                    System.out.println(">> 错误：IP地址格式不正确 - " + host);
                    System.out.println(">> 请输入有效的IP地址（如：127.0.0.1）或使用localhost");
                    System.exit(0);
                }
            }

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
            System.out.println(">> 正在连接到 " + host + ":" + port + "...");
            
            // 检查IP地址是否可达
            if (!host.equals("localhost")) {
                System.out.println(">> 正在检查主机可达性...");
                if (!isIPReachable(host)) {
                    System.out.println(">> 错误：无法连接到IP地址 - " + host);
                    System.exit(0);
                }
                System.out.println(">> 主机可达，正在建立连接...");
            }
            
            Socket socket = new Socket(host, port); // 连接到指定服务器 [cite: 322]
            System.out.println(">> Connected to server!");

            // 启动接收线程，专门收服务器发来的消息 [cite: 367]
            new ServerHandler(socket).start();

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            System.out.println(">> 提示：连续按两次回车键退出客户端");
            System.out.println(">> 提示：输入 /send 文件名 发送文件");
            int emptyLineCount = 0; // 记录连续空行的次数
            System.out.print(">> ");
            while (true) {
                String msg = sc.nextLine();
                if (msg.isEmpty()) {
                    emptyLineCount++;
                    if (emptyLineCount >= 2) {
                        System.out.println("\n>> 正在退出客户端...");
                        break; // 连续两次回车，退出客户端
                    }
                } else if (msg.startsWith("/send ")) {
                    emptyLineCount = 0; // 重置计数器
                    String filename = msg.substring(6).trim();
                    sendFile(dos, filename);
                } else {
                    emptyLineCount = 0; // 重置计数器
                    dos.writeUTF(msg); // 发送消息给服务器，writeUTF支持长文本（最多65535字节UTF-8）[cite: 423]
                    dos.flush();
                }
                System.out.print(">> ");
            }
            socket.close();
        } catch (UnknownHostException e) {
            System.out.println(">> 错误：无法解析主机地址 - " + host);
            System.out.println(">> 请检查IP地址是否正确，或使用localhost连接本地服务器");
        } catch (ConnectException e) {
            System.out.println(">> 错误：无法连接到服务器 " + host + ":" + port);
            System.out.println(">> 请检查：");
            System.out.println(">>   1. 服务器是否已启动");
            System.out.println(">>   2. IP地址是否正确");
            System.out.println(">>   3. 端口号是否正确");
        } catch (IOException e) {
            System.out.println(">> 连接错误: " + e.getMessage());
            System.out.println(">> 请检查网络连接和服务器状态");
        }
    }

    /**
     * 发送文件到服务器
     * @param dos 数据输出流
     * @param filename 文件名
     */
    private static void sendFile(DataOutputStream dos, String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println(">> 错误：文件不存在 - " + filename);
            return;
        }

        if (!file.isFile()) {
            System.out.println(">> 错误：不是有效的文件 - " + filename);
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            long fileSize = file.length();
            System.out.println(">> 正在发送文件: " + filename);
            System.out.println(">> 文件大小: " + fileSize + " 字节");

            // 发送文件头信息
            dos.writeUTF("[FILE] " + filename);
            dos.flush();

            // 读取文件内容并发送（作为UTF字符串）
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                String content = new String(buffer, 0, bytesRead, "UTF-8");
                dos.writeUTF(content);
                dos.flush();
            }

            // 发送文件结束标记
            dos.writeUTF("[FILE_END]");
            dos.flush();

            System.out.println(">> 文件发送完成: " + filename);
        } catch (IOException e) {
            System.out.println(">> 文件发送失败: " + e.getMessage());
        }
    }
}
