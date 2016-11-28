package tools;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;

/**
 * Created by innkp on 2016/11/14.
 */
public interface server {
    public void start();
    public void stop();
    public void register(Class interfaceDefiner,Class impl);
    public void call(Invocation invo);
    public boolean isRunning();
    public int getPort();
    public String getHost();
    public void setPort(int port);
    public void setHost(String host);
    public String addUser(String s);
    public boolean judgeUser(String host);
}
