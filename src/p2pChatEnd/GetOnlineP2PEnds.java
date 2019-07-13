package p2pChatEnd;

import Iotry.input;
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
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Vector;

/**
 * 功能：
 * 1.新建界面
 */
public class GetOnlineP2PEnds extends JPanel implements ActionListener {
    private Chat chat1;

    private JButton getOnlineP2PEnds,submit;
    private JList list;
    private CommWithServer commWithServer;
    private Request request;
    private Response response;
    private ObjectOutputStream pipedOut;
    private ObjectInputStream pipedIn;
    private static String registerName;
    private int clickNum=0;
    public GetOnlineP2PEnds(CommWithServer commWithServer,Chat chat1){
        this.commWithServer=commWithServer;
        this.chat1=chat1;
        //界面设置
        setLayout(new BorderLayout());
        getOnlineP2PEnds=new JButton("查看在线好友");
        submit=new JButton("提 交");
        getOnlineP2PEnds.addActionListener(this);
        submit.addActionListener(this);
        list=new JList();
        list.setFont(new Font("楷体", Font.BOLD, 15));
        JScrollPane scroll=new JScrollPane();
        scroll.getViewport().setView(list);
        Box box= Box.createHorizontalBox();
        box.add(new JLabel("               ", JLabel.CENTER));
        box.add(getOnlineP2PEnds);
        JPanel panelR=new JPanel(new BorderLayout());
        panelR.add(submit, BorderLayout.SOUTH);
        JPanel panel=new JPanel(new BorderLayout());
        panel.add(box, BorderLayout.NORTH);
        panel.add(new JLabel("选择聊天P2P端："), BorderLayout.WEST);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelR, BorderLayout.EAST);
        add(panel, BorderLayout.CENTER);
        panel.add(chat1,BorderLayout.SOUTH);
        submit.setEnabled(false);
        validate();
    }

    public void setChat(Chat chat){
        chat1=chat;
    }

    public static void setRegisterName(String name){
        registerName=name;
    }
    public void actionPerformed(ActionEvent e) {
        if(registerName==null||commWithServer==null||!commWithServer.isAlive()){
            JOptionPane.showMessageDialog(null, "你还没有注册！","信息提示", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        try{
            //获取好友名单
            if(e.getSource()==getOnlineP2PEnds){
                clickNum++;
                //注册类中写过一个，到时候抽象出来一个工具类，反正通道可以重复使用，所以comm中加一个get就可以收到回复，而不用每次都用通道了
                if(clickNum==1){
                    PipedInputStream pipedI=new PipedInputStream();
                    PipedOutputStream pipedO=new PipedOutputStream(pipedI);
                    pipedOut=new ObjectOutputStream(pipedO);
                    pipedIn=new ObjectInputStream(pipedI);
                }
                request=new Request(2, registerName);
                commWithServer.setRequest(request);
                commWithServer.setPipedOut(pipedOut);
                commWithServer.notifyCommWithServer();//在注册的时候已经启动了这个类（而且通过静态set绑定了同一个实例），所以，现在用notify
                response=(Response)pipedIn.readObject();
                //从响应中得到在线的P2P端注册名列表
                Vector<String> onLineP2PEnds=response.getAllNameOfRegister();
                list.setListData(onLineP2PEnds);
                submit.setEnabled(true);
            }
            if(e.getSource()==submit){
                List<Object> list2=list.getSelectedValuesList();
                int len=list2.size();
                if(len==0){
                    JOptionPane.showMessageDialog(this, "你还未选择聊天P2P端！","信息提示", JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                String friend []=new String[list2.size()];
                for(int i=0;i<list2.size();i++)
                    friend[i]=(String)list2.get(i);//获得选择聊天的注册名

                input.setFriend(friend);         //发送端获取好友名单
                Vector<InetSocketAddress> P2PEndAddress=new Vector<>();
                int chatP2PEnds=0;
                //获得聊天对象名字
                for(int i=0;i<len;i++){
                    if(friend[i].equals(registerName))//如果聊天对象名与当前相同，则跳过
                        continue;
                    request=new Request(3, registerName, friend[i]);
                    commWithServer.setRequest(request);
                    commWithServer.setPipedOut(pipedOut);
                    commWithServer.notifyCommWithServer();
                    response=(Response)pipedIn.readObject();
                    //从响应中得到的聊天对象地址加入到列表中
                    P2PEndAddress.add(response.getChatP2PEndAddress());
                    chatP2PEnds++;
                }
                String message=null;
                if(chatP2PEnds==0){
                    message="你只选择了与自己聊天，请重新选择聊天端！";
                }else{
                    Chat.setChatP2PEndAddress(P2PEndAddress);
                    message="已获取到你选择P2P端的地址，请单击下方的|聊天|按钮";
                }
                JOptionPane.showMessageDialog(this, message,"信息提示", JOptionPane.PLAIN_MESSAGE);

                //input.setChatP2PEndAddress(P2PEndAddress);   //发送端获得地址，但是这时候获得，那之后有人中途退出也不会更新，所以应该在聊天窗口中获得
                P2PEndAddress.clear();//清空地址列表
                list.setListData(P2PEndAddress);
            }
        }catch (Exception e1){
            JOptionPane.showMessageDialog(this, "与服务器通信出错","警告", JOptionPane.WARNING_MESSAGE);
        }
    }
}
