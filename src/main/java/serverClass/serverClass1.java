package serverClass;

import serverInterface.serverInterface1;
import java.util.Date;
import java.text.SimpleDateFormat;
/**
 * Created by innkp on 2016/11/21.
 */
public class serverClass1 implements serverInterface1 {
    public String getTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }
}
