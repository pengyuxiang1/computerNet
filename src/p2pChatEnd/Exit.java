package p2pChatEnd;

import appProtocol.Request;
import appProtocol.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * 功能：
 * 1.新建退出窗口（编辑退出按钮的功能）
 * 2.通过commwitjserver和server通信，传递退出信息，以删除地址表，所以是一个完全退出按钮
 */
public class Exit extends JPanel implements ActionListener {
    private CommWithServer commWithServer;
    private Request request;
    private Response response;
    private ObjectOutputStream pipedOut;
    private ObjectInputStream pipedIn;
    private static String registerName;
    private P2PChatEnd P2PChatEnd;
    private static ChatWindow chatWindow;
    public Exit(CommWithServer commWithServer,P2PChatEnd P2PChatEnd){
        this.commWithServer=commWithServer;
        this.P2PChatEnd=P2PChatEnd;
        JLabel hint=new JLabel("确认要退出信息服务器吗？");
        JButton quit=new JButton("退出");
        quit.addActionListener(this);
        setLayout(new BorderLayout());
        add(hint, BorderLayout.CENTER);
        add(quit, BorderLayout.SOUTH);
    }
    public static void setRegisterName(String name){
        registerName=name;
    }
    public static void setChatWindow(ChatWindow cw){
        chatWindow=cw;
    }

    public void actionPerformed(ActionEvent e) {
        if(registerName!=null&&commWithServer.isAlive()){
            try {
                //pipe的用法，输入输出一个管
                PipedInputStream pipedI=new PipedInputStream();
                PipedOutputStream pipedO=new PipedOutputStream(pipedI);
                pipedOut=new ObjectOutputStream(pipedO);
                pipedIn=new ObjectInputStream(pipedI);
                request=new Request(4, registerName);//退出请求
                commWithServer.setRequest(request);
                commWithServer.setPipedOut(pipedOut);//绑定一个就相当于输入输出都绑定了
                commWithServer.notifyCommWithServer();//唤醒连接线程，进入CommWithServer的run方法，
                response=(Response)pipedIn.readObject();//获得服务器响应                                    与CommWithServer里的通道时同步获得的？？内容：registerName+",你已经从服务器退出！"
            }catch (Exception ex){
                JOptionPane.showMessageDialog(this, "与服务器通信出错","警告", JOptionPane.WARNING_MESSAGE);

            }
            String message=response.getMessage()+"单击|确定|退出";
            JOptionPane.showMessageDialog(null, message,"信息提示", JOptionPane.PLAIN_MESSAGE);
            commWithServer.keepCommunicating=false;
            commWithServer.interrupt();
            commWithServer.close();
        }
        commWithServer=null;
        //关闭聊天窗口
        if(chatWindow==null||!chatWindow.isVisible())
            System.exit(0);
        else
            P2PChatEnd.setVisible(false);
    }
}
