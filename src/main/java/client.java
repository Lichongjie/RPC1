import tools.Invocation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_WRITE;

/**
 * Created by innkp on 2016/11/21.
 */
public class client {
    private String host;
    private int port;
    private SocketChannel sc;
    private Selector selector;
    public  Invocation invo;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public client(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public void init() throws Exception{
        sc = SocketChannel.open();
        sc.configureBlocking(false);
       selector = Selector.open();
        sc.register(selector, SelectionKey.OP_CONNECT);

        sc.connect(new InetSocketAddress(host, port));
        System.out.println("-----------连接服务器成功------------");

    }
    public void setPort(int port) {

        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void connect2(Invocation invo)throws Exception{


        byte[] bys = invo.Tobyte();

        while (selector.select() > 0) {
            Set readkeys = selector.selectedKeys();
            Iterator iterator = readkeys.iterator();
            SelectionKey selectionKey = (SelectionKey) iterator.next();
            if (selectionKey.isConnectable()) {

                selectionKey.interestOps(SelectionKey.OP_READ);

                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                if (socketChannel.isConnectionPending()) {
                    socketChannel.finishConnect();
                }
                socketChannel.register(selectionKey.selector(), OP_WRITE, ByteBuffer.allocate(1024));
            }
            if (selectionKey.isReadable()) {


                ByteBuffer buffer = ByteBuffer.allocate(1024);
                SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
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
                    Invocation invo2 = Invocation.ToObject(byt);
                    invo.setResult(invo2.getResult());

                    clientChannel.register(selector, SelectionKey.OP_READ);

                }
                selectionKey.interestOps(SelectionKey.OP_WRITE);
               clientChannel.register(selectionKey.selector(), SelectionKey.OP_WRITE, ByteBuffer.allocate(1024));
               // System.out.print("select   "+selector.select());
             //  readkeys.remove(selectionKey);
                return;
            }

            if (selectionKey.isWritable()){

                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                ByteBuffer buf = ByteBuffer.allocate(bys.length);
                buf.clear();
                buf.put(bys);
                buf.flip();
                while (buf.hasRemaining()) {
                    socketChannel.write(buf);
                }

                socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                readkeys.remove(selectionKey);
            }
        }
    }

    public void signUp(String Host){


        String selfHost ="";
        try{

            selfHost = InetAddress.getLocalHost().getHostAddress();
            System.out.println(selfHost+"向远程服务器注册");
            while(selector.select() > 0) {
                Set readkeys = selector.selectedKeys();
                Iterator iterator = readkeys.iterator();
                SelectionKey selectionKey = (SelectionKey) iterator.next();
                if (selectionKey.isConnectable()) {
                    selectionKey.interestOps(SelectionKey.OP_READ);

                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    if (socketChannel.isConnectionPending()) {
                        socketChannel.finishConnect();
                        byte[] bys = ("$" + selfHost).getBytes();
                        ByteBuffer buf = ByteBuffer.allocate(bys.length);
                        buf.clear();
                        buf.put(bys);
                        buf.flip();
                        while (buf.hasRemaining()) {
                            socketChannel.write(buf);
                        }
                       // socketChannel.close();
                    }
                    socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                    if (selectionKey.isReadable()) {
                        SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                        buffer.clear();
                        long bytesRead = clientChannel.read(buffer);
                        if (bytesRead == -1) {
                            clientChannel.close();
                        } else {
                            buffer.flip();
                            byte[] byt = new byte[(int)bytesRead];
                            int i = 0;
                            while (buffer.hasRemaining()) {
                                byte c = buffer.get();
                                byt[i] = c;
                                i++;
                            }
                            String s = new String(byt);
                            System.out.println(selfHost+s);
                            return;
                        }
                        selectionKey.interestOps(SelectionKey.OP_READ);
                        readkeys.remove(selectionKey);
                    }

        }
            sc.close();
            selector.close();
        }catch(Exception e){
            e.printStackTrace();
        }


    }

    public static <T> T getProxy(final Class<T> c,String host,int port,final client client){
        final String host1 = host;
        InvocationHandler handler = new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Invocation in =new Invocation();
                in.setInterfaces(c);
                //invo.setMethod(new method(method.getName(),method.getParameterTypes()));
                in.setParamsType(method.getParameterTypes());
                in.setMethodName(method.getName());
                in.setParams(args);
                client.connect2(in);
                return in.getResult();
            }
        };
        T t = (T)Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[] {c}, handler );
        return t;
    }


}




