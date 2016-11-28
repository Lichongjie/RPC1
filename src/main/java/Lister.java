import com.sun.corba.se.spi.activation.Server;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import serverClass.serverClass1;
import tools.Invocation;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

/**
 * Created by innkp on 2016/11/14.
 */
public class Lister extends Thread {
    private ServerSocketChannel serverSocketChannel;
    private InetSocketAddress socket_addr;
    private tools.server server;
    private ExecutorService pool = Executors. newFixedThreadPool(5);


    public Lister(tools.server s) {
        this.server = s;
    }

    @Override
    public void run() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            Selector select = Selector.open();         //静态方法 实例化selector
            socket_addr = new InetSocketAddress(server.getHost(), server.getPort());
            serverSocketChannel.socket().bind(socket_addr);
            serverSocketChannel.configureBlocking(false);

            serverSocketChannel.register(select, OP_ACCEPT); //注册 OP_ACCEPT事件
            int i = 0;
            while (this.server.isRunning()) {
                i++;
                select.select();
                Set readkeys = select.selectedKeys();
                Iterator iterator = readkeys.iterator();
                while (iterator.hasNext()) {
                    final SelectionKey key = (SelectionKey) iterator.next();


                    if (key.isAcceptable()) {

                        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
                        if (socketChannel != null) {
                            System.out.println("接收到连接: " + socketChannel);
                            socketChannel.configureBlocking(false);
                            socketChannel.register(key.selector(), OP_READ, ByteBuffer.allocate(1024));
                        }
                    }
                    if (key.isReadable()) {
                        // 获得与客户端通信的信道
                        final SocketChannel clientChannel = (SocketChannel) key.channel();

                        Thread s =  new Thread() {
                            @Override
                            public void run() {
                                try {
                                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                                    buffer.clear();
                                    long bytesRead = clientChannel.read(buffer);
                                    if (bytesRead == -1) {
                                        clientChannel.close();
                                    } else {
                                        buffer.flip();
                                        byte[] byt = new byte[(int) bytesRead];
                                        int i = 0;
                                        while (buffer.hasRemaining()) {
                                            byte c = buffer.get();
                                            byt[i] = c;
                                            i++;
                                        }
                                        String s = new String(byt);
                                        String info = "";

                                        if (s.charAt(0) == '$') {
                                            // 将缓冲区准备为数据传出状态
                                            info = server.addUser(s);
                                            // System.out.println("----"+info+"-----");

                                            byte[] bytes = info.getBytes();
                                            ByteBuffer buffer2 = ByteBuffer.allocate(bytes.length);
                                            buffer2.clear();
                                            buffer2.put(bytes);
                                            buffer2.flip();
                                            while (buffer2.hasRemaining()) {
                                                clientChannel.write(buffer2);
                                            }
                                        } else {
                                            String ss = clientChannel.getRemoteAddress().toString();
                                            ss = ss.split(":")[0];
                                            ss = ss.substring(1, ss.length());
                                            if (server.judgeUser(ss) == false) {
                                                clientChannel.close();
                                            } else {
                                                serverClass1 a = new serverClass1();
                                                Invocation invo = Invocation.ToObject(byt);
                                                server.call(invo);
                                                byte[] bytes = invo.Tobyte();
                                                buffer.clear();
                                                buffer.put(bytes);
                                                buffer.flip();
                                                while (buffer.hasRemaining()) {
                                                    clientChannel.write(buffer);
                                                }
                                                // System.out.println("写成功");
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                }
                                // key.interestOps(OP_READ);
                            }
                        };
                        pool.execute(s);

                        clientChannel.register(key.selector(), key.OP_READ, ByteBuffer.allocate(1024));
                        key.interestOps(SelectionKey.OP_READ);

                        // key.cancel();

                    }
                  //  readkeys .remove(key);
                }

                readkeys.clear();
            }
            } catch(Exception e){
                e.printStackTrace();
            }

        }
    }

