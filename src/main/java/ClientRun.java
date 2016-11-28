import serverInterface.serverInterface1;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by innkp on 2016/11/21.
 */
public class ClientRun{
    public static String getTime(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }
    public static void main(String[] Args){
        String host="";
        try{
            host = InetAddress.getLocalHost().getHostAddress();
            // System.out.print(host);
            //   Scanner sc=new Scanner(System.in);
            //  System.out.print("请输入服务器IP:");
            //   host=sc.nextLine();

        }catch(Exception e){
            e.printStackTrace();
        }
        final client client = new client(host,20382);
        try {
            client.init();
        }catch(Exception e){
            e.printStackTrace();
        }
        client.signUp(host);
        try {
            client.init();
        }catch(Exception e){
            e.printStackTrace();
        }
        final serverInterface1 s = client.getProxy(serverInterface1.class, host,20382,client);
      //  System.out.println("服务器时间："+s.getTime());
        final long timeInterval = 1000;
        Runnable runnable = new Runnable() {
                public void run() {
                    while (true) {
                        System.out.println("客户端时间：" +getTime());
                        System.out.println("服务器时间："+s.getTime());

                        try {
                            Thread.sleep(timeInterval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
        };
            Thread thread = new Thread(runnable);
            thread.start();
        }
}
