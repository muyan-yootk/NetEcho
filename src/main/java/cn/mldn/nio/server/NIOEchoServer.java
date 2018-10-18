package cn.mldn.nio.server;

import cn.mldn.commons.ServerInfo;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于NIO实现数据的交互处理模式
 */
public class NIOEchoServer {
    public static void main(String[] args) throws Exception {
        new EchoServerHandle() ;
    }
}

/**
 * 实现一个专门用于客户端请求处理的线程对象
 */
class SocketClientChannelThread implements Runnable {
    private SocketChannel clientChannel ; // 客户端的信息
    private boolean flag = true ; // 循环处理的标记
    public SocketClientChannelThread(SocketChannel clientChannel) {
        this.clientChannel = clientChannel ;
        System.out.println("服务器端连接成功，可以与服务器端进行数据的交互操作...");
    }
    @Override
    public void run() { // 真正的通讯处理的核心需要通过run()方法来进行操作
        // NIO是基于Buffer缓冲操作实现的功能，需要将输入的内容保存在缓存之中
        ByteBuffer buffer = ByteBuffer.allocate(50) ; // 开辟一个50大小的缓存空间
        try {
            while(this.flag) {
                buffer.clear() ; // 清空缓存操作，可以进行该缓存空间的重复使用
                int readCount = this.clientChannel.read(buffer) ;  // 服务器端读取客户端发送来的内容
                // 将缓冲区之中保存的内容转位字节数组之后进行存储
                String readMessage = new String(buffer.array(),0,readCount).trim() ;
                System.out.println("【服务器端接收消息】" + readMessage); // 输出一下提示信息
                // 在进行整个的通讯过程里面，分隔符是一个绝对重要的概念，如果不能够很好的处理分隔符，那么无法进行有效通讯
                String writeMessage = "【ECHO】" + readMessage + "\n"; // 进行消息的回应处理
                if ("exit".equalsIgnoreCase(readMessage)) {
                    writeMessage = "【ECHO】Bye Byte ... kiss"  ; // 结束消息
                    this.flag = false ; // 要结束当前的循环操作
                }   // 现在的数据是在字符串之中，如果要回应内容，需要将内容保存在Buffer之中
                buffer.clear() ; // 将已经保存的内容（内容已经处理完毕）清除
                buffer.put(writeMessage.getBytes()) ; // 保存回应信息
                buffer.flip() ; // 重置缓冲区
                this.clientChannel.write(buffer) ;
            }
            this.clientChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class EchoServerHandle implements AutoCloseable {    // 定义服务器端的服务处理类
    private ExecutorService executorService ;
    private ServerSocketChannel serverSocketChannel ; // 服务器端的通讯通道
    private Selector selector ;
    private SocketChannel clientChannel ; // 客户端的信息
    public EchoServerHandle() throws Exception  {
        this.executorService = Executors.newFixedThreadPool(5) ; // 当前的执行线程只允许有5个
        this.serverSocketChannel = ServerSocketChannel.open() ; // 打开服务器端连接通道
        this.serverSocketChannel.configureBlocking(false) ; // 设置为非阻塞模式
        this.serverSocketChannel.bind(new InetSocketAddress(ServerInfo.PORT)) ;
        // NIO之中的Reactor模型重点在于所有的Channel需要向Selector之中进行注册
        this.selector = Selector.open() ; // 获取Selector实例
        this.serverSocketChannel.register(this.selector,SelectionKey.OP_ACCEPT) ;  // 服务器端需要进行接收
        System.out.println("服务器端程序启动，该程序在" + ServerInfo.PORT + "端口上进行监听...");
        this.clientHandle();
     }
     private void clientHandle() throws Exception {
        int keySelect = 0 ;     // 保存一个当前的状态
         while((keySelect = this.selector.select()) > 0) {  // 需要进行连接等待
             Set<SelectionKey> selectedKeys = this.selector.selectedKeys() ; // 获取全部连接通道信息
             Iterator<SelectionKey> selectionIter = selectedKeys.iterator() ;
             while(selectionIter.hasNext()) {
                 SelectionKey selectionKey = selectionIter.next() ; // 获取每一个通道
                 if(selectionKey.isAcceptable()) {  // 该通道为接收状态
                     this.clientChannel = this.serverSocketChannel.accept() ; // 等待连接
                     if (this.clientChannel != null) {  // 当前有连接
                         this.executorService.submit(new SocketClientChannelThread(this.clientChannel)) ;
                     }
                 }
                 selectionIter.remove(); // 移除掉此通道
             }
         }
     }

    @Override
    public void close() throws Exception {
        this.executorService.shutdown(); // 关闭线程池
        this.serverSocketChannel.close(); // 关闭服务器端
    }
}