package cn.mldn.aio.client;

import cn.mldn.commons.ServerInfo;
import cn.mldn.util.InputUtil;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

class ClientReadHandler implements CompletionHandler<Integer,ByteBuffer> {
    private AsynchronousSocketChannel clientChannel ;
    private CountDownLatch latch ;
    public ClientReadHandler(AsynchronousSocketChannel clientChannel,CountDownLatch latch) {
        this.clientChannel = clientChannel ;
        this.latch = latch ;
    }
    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        buffer.flip() ;
        String receiveMessage = new String(buffer.array(),0,buffer.remaining()) ;
        System.out.println(receiveMessage);
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            this.clientChannel.close();
        } catch (Exception e) {}
        this.latch.countDown();
    }
}
class ClientWriteHandler implements CompletionHandler<Integer,ByteBuffer> {
    private AsynchronousSocketChannel clientChannel ;
    private CountDownLatch latch ;
    public ClientWriteHandler(AsynchronousSocketChannel clientChannel,CountDownLatch latch) {
        this.clientChannel = clientChannel ;
        this.latch = latch ;
    }
    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        if(buffer.hasRemaining()) {
            this.clientChannel.write(buffer,buffer,this) ;
        } else {
            ByteBuffer readBuffer = ByteBuffer.allocate(50) ;
            this.clientChannel.read(readBuffer,readBuffer,new ClientReadHandler(this.clientChannel,this.latch)) ;
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            this.clientChannel.close();
        } catch (Exception e) {}
        this.latch.countDown();
    }
}
class AIOClientThread implements Runnable {
    private AsynchronousSocketChannel clientChannel ;
    private CountDownLatch latch ;
    public AIOClientThread() throws Exception {
        this.clientChannel = AsynchronousSocketChannel.open() ; // 打开客户端的Channel
        this.clientChannel.connect(new InetSocketAddress(ServerInfo.ECHO_SERVER_HOST,ServerInfo.PORT)) ;
        this.latch = new CountDownLatch(1) ;
    }
    @Override
    public void run() {
        try {
            this.latch.await();
            this.clientChannel.close();
        } catch (Exception e) {}
    }

    /**
     * 进行消息的发送处理
     * @param msg 输入的交互内容
     * @return 是否停止交互的标记
     */
    public boolean sendMessage(String msg) {
        ByteBuffer buffer = ByteBuffer.allocate(50) ;
        buffer.put(msg.getBytes()) ;
        buffer.flip() ;
        this.clientChannel.write(buffer,buffer,new ClientWriteHandler(this.clientChannel,this.latch)) ;
        if ("exit".equalsIgnoreCase(msg)) {
            return false ;
        }
        return true ;
    }
}


public class AIOEchoClient {
    public static void main(String[] args) throws Exception {
        AIOClientThread client = new AIOClientThread() ;
        new Thread(client).start();
        while(client.sendMessage(InputUtil.getString("请输入要发送的信息："))) {
            ;
        }
    }
}
