package cn.mldn.bio.client;

import cn.mldn.commons.ServerInfo;
import cn.mldn.util.InputUtil;

import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

class EchoClientHandle implements AutoCloseable {
    private Socket client;

    public EchoClientHandle() throws Exception {
        this.client = new Socket(ServerInfo.ECHO_SERVER_HOST, ServerInfo.PORT);
        System.out.println("已经成功的连接到了服务器端，可以进行消息的发送处理。");
        this.accessServer();
    }
    private void accessServer() throws Exception {   // 数据交互处理
        Scanner scan = new Scanner(this.client.getInputStream()) ;  // 服务器端的输出为客户端的输入
        PrintStream out = new PrintStream(this.client.getOutputStream()) ; // 向服务器端发送内容
        scan.useDelimiter("\n") ;
        boolean flag = true ;
        while(flag) {
            String data = InputUtil.getString("请输入要发送的数据信息：") ;
            out.println(data); // 先把内容发送到服务器端上
            if ("exit".equalsIgnoreCase(data)) {
                flag = false ; // 结束循环s
            }
            if (scan.hasNext()) {
                System.out.println(scan.next());
            }
        }
    }

    @Override
    public void close() throws Exception {
        this.client.close();
    }
}

public class EchoClient {
    public static void main(String[] args) {
        try (EchoClientHandle echo = new EchoClientHandle()) {

        } catch(Exception e) {}
    }
}
