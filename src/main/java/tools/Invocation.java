package tools;

import java.io.*;
import java.net.URLEncoder;
import java.nio.ByteBuffer;

/**
 * Created by innkp on 2016/11/14.
 */
public class Invocation implements Serializable{
    private Object[] params;
    private Class interfaces;
    private Object Result;
    private Class[] paramsType;
    private String methodName;
    public String getMethodName() {


        return methodName;
    }

    public void setMethodName(String methodName)
    {
        try {

           String  xmlUTF8 = URLEncoder.encode( new String(methodName.getBytes("UTF-8")), "UTF-8");
            this.methodName = xmlUTF8;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public Class[] getParamsType() {
        return paramsType;

    }
    public void setParamsType(Class[] paramsType) {
        this.paramsType = paramsType;
    }
    public Object getResult() {
        return Result;
    }

    public void setResult(Object result) {
        Result = result;
    }

    public Object[] getParams() {
        return params;

    }
    public Class getInterfaces() {
        return interfaces;
    }
    public void setInterfaces(Class interfaces) {
        this.interfaces = interfaces;
    }
    public void setParams(Object[] params) {
        this.params = params;
    }

    public byte[] Tobyte(){
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(this);

            byte[] bytes = bo.toByteArray();
            return bytes;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static Invocation ToObject(byte[] byt){

        try {
         // ByteArrayInputStream bi = new ByteArrayInputStream(byt);
           // ObjectInputStream oi = new ObjectInputStream(bi);
            ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(byt)));
            Invocation in = (Invocation) oi.readObject();


            return in;

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
