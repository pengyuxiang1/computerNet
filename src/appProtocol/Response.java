package appProtocol;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Vector;

/**
 * 功能:
 * 1. 传递信息
 * 2. 传递地址表
 *
 */
public class Response implements Serializable {
    private int responseType;  //返回类型的名字
    private String message;  //返回内容
    private Vector<String> allNameOfRegister;   //在线用户
    private InetSocketAddress chatP2PEndAddress;   //地址


    public  Response(int responseType){
        this.responseType=responseType;
    }
    //正常通讯的初始化
    public Response(int responseType, String message) {
        this.responseType = responseType;
        this.message = message;
    }

    //发送地址表的初始化
    public Response(int responseType, Vector<String> allNameOfRegister) {
        this(responseType);
        this.allNameOfRegister = allNameOfRegister;
    }

    public Response(int responseType, InetSocketAddress chatP2PEndAddress) {
        this(responseType);
        this.chatP2PEndAddress = chatP2PEndAddress;
    }

    public int getResponseType() {
        return responseType;
    }

    public String getMessage() {
        return message;
    }

    public Vector<String> getAllNameOfRegister() {
        return allNameOfRegister;
    }

    public InetSocketAddress getChatP2PEndAddress() {
        return chatP2PEndAddress;
    }
}
