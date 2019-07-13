package messageServer;
import appProtocol.Request;
import appProtocol.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * MessageHandler是子线程的线程体类，定义了子线程所需完成的各种方法
 * 与客户端TCP连接的线程完成接收请求，解析请求，发送的响应的操作
 * 功能：1是注册；2是获取所有地址表； 3是获取个人地址 ；4退出在线表
 */
public class MessageHandler implements  Runnable {
    //三部曲
    private Socket socket;
    //与聊天端的通信时的输入，输出端
    private ObjectInputStream datainput;
    private ObjectOutputStream dataoutput;

    //实现第一个功能：监听信息的传递过来
    private Thread listener;//子线程对象
    private  static Hashtable<String,InetSocketAddress>clientMessage= new Hashtable<>();//保存p2p注册名和地址
    private Request request;//请求变量
    private Response response;//响应变量
    private boolean keepListening=true;
    //创建客户端子线程的线程体
    public MessageHandler(Socket socket){
        this.socket=socket;
    }

    public synchronized  void start(){
        if(listener==null){
            try{
                //初始化通信与子线程，建立起与commWithServer通信
                datainput=new ObjectInputStream(socket.getInputStream());
                dataoutput=new ObjectOutputStream(socket.getOutputStream());
                listener=new Thread(this);                       //启动本类这个线程，之后运行run（）函数
                listener.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public synchronized  void stop(){
        if(listener!=null){
            try{
                listener.interrupt();;
                listener=null;
                datainput.close();
                dataoutput.close();
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            while(keepListening){
                receiveRequest();//接收请求
                parseRequest();//解析请求
                sendResponse();//发送响应
                request=null;
            }
            stop();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            stop();
            System.err.println("与客户端通信出现错误...");
        }
    }

    private void receiveRequest()throws IOException,ClassNotFoundException{
        request=(Request)datainput.readObject();//从commWithServer发送的请求
    }

    private void parseRequest(){
        if(request==null)
            return;

        response=null;
        int requestType=request.getRequestTyper();
        String registerName=request.getRegisterName();

        /**      开始解析请求       **/
        //  1 则是没有注册
        if(requestType!=1&&!registerNameHasBeenUsed(registerName)){
            response=new Response(1,registerName+"你还未注册！" );
            return;
        }
        switch (requestType){//测试请求类型   1是注册；2是获取所有地址表； 3是获取个人地址 ；4退出在线表
            case 1:
                if(registerNameHasBeenUsed(registerName)){
                    response=new Response(1,"|"+registerName+"|"+"已被其他人使用，请使用其他名字注册" );
                    break;
                }
                clientMessage.put(registerName, new InetSocketAddress(socket.getInetAddress(), request.getUDPPort()));  //存入注册名和地址端口
                response=new Response(1,registerName+",你已经注册成功！" );
                System.out.println("|"+registerName+"| 注册成功...");
                break;
            case 2://发送地址表
                Vector<String> allNameOfRegister= new Vector<>();
                for(Enumeration<String>e=clientMessage.keys();e.hasMoreElements(); ){
                    //生成已注册的P2P端注册名列表
                    allNameOfRegister.addElement(e.nextElement());
                }
                response=new Response(2,allNameOfRegister );
                break;
            case 3://私聊选择，将被选中的人的地址发送
                String chatRegisterName=request.getChatRegisterName();
                InetSocketAddress chatP2PEndAddress=clientMessage.get(chatRegisterName);
                response=new Response(3, chatP2PEndAddress);
                break;
            case 4://登出提示 同时也说明了，这个服务器只记录在线用户
                clientMessage.remove(registerName);
                response=new Response(1,registerName+",你已经从服务器退出！" );
                keepListening=false;
                System.out.println("|"+registerName+"| 从服务器退出...");
        }
    }

    private boolean registerNameHasBeenUsed(String registerName){
        if(registerName!=null&&clientMessage.get(registerName)!=null)
            return true;
        return false;
    }

    private void sendResponse()throws IOException{
        if(response!=null){
            dataoutput.writeObject(response);//将响应写回到commWithServer
        }
    }
}