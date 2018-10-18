package cn.mldn.bio.server;

import cn.mldn.commons.ServerInfo;

import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

class EchoServerHandle implements AutoCloseable {
    private ServerSocket serverSocket ;
    public EchoServerHandle() throws Exception {
        this.serverSocket = new ServerSocket(ServerInfo.PORT) ;   // 进行服务端的Socket启动
        System.out.println("ECHO服务器端已经启动了，该服务在" + ServerInfo.PORT + "端口上监听....");
        this.clientConnect();
    }
    private void clientConnect() throws Exception {
        boolean serverFlag = true ;
        while(serverFlag) {
            Socket client = this.serverSocket.accept(); // 等待客户端连接
            Thread clientThread = new Thread(()->{
                try {
                    Scanner scan = new Scanner(client.getInputStream());// 服务器端输入为客户端输出
                    PrintStream out = new PrintStream(client.getOutputStream()) ;//服务器端的输出为客户端输入
                    scan.useDelimiter("\n") ; // 设置分隔符
                    boolean clientFlag = true ;
                    while(clientFlag) {
                        if (scan.hasNext()) {    // 现在有内容
                            String inputData = scan.next(); // 获得输入数据
                            if ("exit".equalsIgnoreCase(inputData)) {   // 信息结束
                                clientFlag = false ; // 结束内部的循环
                                out.println("【ECHO】Bye Bye ... kiss"); // 一定需要提供有一个换行机制，否则Scanner不好读取
                            } else {
                                out.println("【ECHO】" + inputData); // 回应信息
                            }
                        }
                    }
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }) ;
            clientThread.start(); // 启动多线程
        }
    }

    @Override
    public void close() throws Exception {
        this.serverSocket.close();
    }
}

/**
 * 实现服务器端的编写开发，采用BIO（阻塞模式）实现开发的基础结构
 */
public class EchoServer {
    public static void main(String[] args) throws Exception {
        new EchoServerHandle() ;
    }
}
