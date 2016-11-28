import tools.Invocation;
import tools.server;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by innkp on 2016/11/14.
 */
public class Server implements server {
    private String  host;
    private int port;
    private boolean isRunning = false;
    private Map<String,Object> serverRepertory = new HashMap<String,Object>();
    private static Set<String> users = new HashSet<String>();
    private Lister lister;

    public String getHost() {
        return host;
    }

    public void start() {
        setRunning(true);
        System.out.println("开启服务...");
        Lister lister = new Lister(this);
        lister.start();
    }

    public void stop() {
        System.out.println("停止服务...");
        setRunning(false);
    }

    public void register(Class interfaceDefiner, Class impl) {
        System.out.println("添加服务："+interfaceDefiner.getName());
        try{
            serverRepertory.put(interfaceDefiner.getName(),impl.newInstance());

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public String  addUser(String  s){
        if (s.charAt(0)=='$'){
            if (users.contains(s))
                return "已经注册";
            else {
                users.add(s);
                return "注册成功";
            }
        }
        else {
            return "注册失败";
        }
    }

    public boolean judgeUser(String host){
        String s = '$'+host;
        String t = (String)users.toArray()[0];
        if (users.contains(s)){
            return true;
        }
        else{
            System.out.print("用户授权未通过！");
            return false;
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void call(Invocation invo) {
        Object obj = serverRepertory.get(invo.getInterfaces().getName());
        if(obj != null){
            try{
                Method m = obj.getClass().getMethod(invo.getMethodName(),invo.getParamsType());
                Object res = m.invoke(obj,invo.getParams());
                invo.setResult(res);
            }catch(Exception e){
                e.printStackTrace();
            }
        }



    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean b){
        isRunning = b;
    }

    public int getPort() {
        return this.port;
    }

    public void setHost(String host) {
        this.host = host;
    }
}

