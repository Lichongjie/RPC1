import java.net.InetAddress;

import serverClass.serverClass1;
import serverInterface.serverInterface1;

/**
 * Created by innkp on 2016/11/21.
 */
public class ServerRun {
    public static void main(String [] Args){
        tools.server s = new Server();
        try {
            s.setHost(InetAddress.getLocalHost().getHostAddress());
            s.setPort(20382);
            s.register(serverInterface1.class, serverClass1.class);
            s.start();
        }catch(Exception e){
            e.printStackTrace();
        }


    }
}
