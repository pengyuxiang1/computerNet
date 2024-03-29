package p2pChatEnd;

import appProtocol.Request;
import appProtocol.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 功能：
 * 1. socket连接服务器
 * 2.请求注册并且线程挂起等待。
 */
public class CommWithServer extends Thread {
    //socket三部曲
    private String serverIP;//ip注册名，通过getbyname获得ip地址，用于新建serversocket
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ObjectOutputStream pipedOut;

    //dto
    private Request request;
    private Response response;

    public static final int PORT=8000;
    public boolean keepCommunicating=true;

    public CommWithServer(){}
    public  void connect(String serverIP, Request request, ObjectOutputStream pipedOut)throws IOException {
        this.serverIP=serverIP;
        this.request=request;
        this.pipedOut=pipedOut;
        InetAddress address= InetAddress.getByName(serverIP);//获得IP地址
        //根据IP地址和端口创建套接字，端口号为服务器的端口号，以此连接到服务器
        InetSocketAddress serverSocketA=new InetSocketAddress(address,PORT);
        //Socket,TCP连接,此时MessageServer类中s.accept()就知道了有人连接了  等于 socket=new Socket(ip，port);
        //就是socket三部曲
        socket=new Socket();
        socket.connect(serverSocketA);
        out=new ObjectOutputStream(socket.getOutputStream());
        in=new ObjectInputStream(socket.getInputStream());
    }

    public  void setRequest(Request request){
        this.request=request;
    }
    public void setPipedOut(ObjectOutputStream pipedOut){
        this.pipedOut=pipedOut;
    }

    public synchronized void close(){
        try{
            in.close();
            out.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //与服务器通信
    public void run(){
        //此用户没有退出的时候。
        while(keepCommunicating){
            synchronized (this){
                try{
                    out.writeObject(request);//被唤醒后向信息服务器发生请求
                    response=(Response)in.readObject();//从信息服务器接收响应
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                }catch (IOException e){
                    close();
                    System.out.println("与服务器通信出现错误...");
                    return;
                }

                try {
                    pipedOut.writeObject(response);//利用管道将响应发生给主程序
                    //response为1，表示退出。
                    if(response.getResponseType()==1){
                        String message=response.getMessage();
                        if(message!=null&&message.equals(request.getRegisterName()+",你已经从服务器退出！"))
                            return;
                    }
                    request=null;//上面写入后再指向null，此时数据已经出去。
                    response=null;
                    wait();//使子线程进入同步等待状态，等待其他程序将其唤醒
                }catch (IOException e){
                    System.out.println("管道通信出现错误...");
                }catch (InterruptedException e){
                    System.out.println("线程同步出现错误...");
                }
            }
        }
    }

    public synchronized  void notifyCommWithServer(){
        notify();   //notify：唤醒上面陷入wait的线程。
    }
}
