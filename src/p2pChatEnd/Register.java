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
import java.net.DatagramSocket;

public class Register extends JPanel implements ActionListener {
    //ui
    private JLabel hintLabel;
    private JTextField registerNameField,serverIPField;//注册名和IP地址文本框
    private JButton submit;//提交按钮
    //通信
    private CommWithServer commWithServer;
    private Chat chat;
    private Request request;
    private Response response;
    private ObjectOutputStream pipedOut;
    private ObjectInputStream pipedIn;

    private int clickNum=0;//判读是否注册
    private boolean isRegister=false;//判读是否注册

    public Register(CommWithServer commWithServer){
        this.commWithServer=commWithServer;
        setLayout(new BorderLayout());
        ImageIcon background = new ImageIcon("5.jpg");// 背景图片
        hintLabel=new JLabel(background);
        hintLabel.setBounds(0, 0,600 , 200);
        registerNameField=new JTextField(10);
        serverIPField=new JTextField(10);
        submit=new JButton("提交");
        submit.addActionListener(this);
        Box box1= Box.createHorizontalBox();
        box1.add(new JLabel("注 册 名  ： ", JLabel.CENTER));
        box1.add(registerNameField);
        Box box2= Box.createHorizontalBox();
        box2.add(new JLabel("服 务 器IP： ", JLabel.CENTER));
        box2.add(serverIPField);
        Box boxH= Box.createVerticalBox();
        boxH.add(box1);
        boxH.add(box2);
        boxH.add(submit);
        JPanel panelC=new JPanel();
        panelC.add(boxH);
        add(panelC, BorderLayout.CENTER);
        JPanel panelN=new JPanel();
        panelN.add(hintLabel);
        add(panelN, BorderLayout.NORTH);
    }

    //set传参
    public void setChat(Chat chat){
        this.chat=chat;
    }

    public void actionPerformed(ActionEvent e) {
        if(isRegister){
            String hint="不能重复注册";
            JOptionPane.showMessageDialog(this, hint,"警告", JOptionPane.WARNING_MESSAGE);
            clear();
            return;
        }
        clickNum++;
        String registerName=registerNameField.getText().trim();
        String serverIP=serverIPField.getText().trim();
        if (registerName.length()==0||serverIP.length()==0){
            String hint="必须输入注册名和服务器IP";
            JOptionPane.showMessageDialog(this, hint,"警告", JOptionPane.WARNING_MESSAGE);
            clear();
            return;
        }
        try {
            if(clickNum==1){
                //使用管道通信，让线程commWithServer可以和该类进行通信
                PipedInputStream pipedI=new PipedInputStream();
                PipedOutputStream pipedO=new PipedOutputStream(pipedI);
                //序列化和反序列化
                pipedOut=new ObjectOutputStream(pipedO);
                pipedIn=new ObjectInputStream(pipedI);
            }

            DatagramSocket socket=new DatagramSocket();
            Chat.setSocket(socket);             //传递用于聊天的UDP套接字地址给Chat对象
            int UDPPort=socket.getLocalPort();//获得一个UDP端口号，实现之后的聊天信息接收端口
            //注册实现
            request=new Request(1, registerName,UDPPort);
            if(commWithServer!=null){
                if(commWithServer.isAlive()){
                    commWithServer.close();
                    commWithServer.connect(serverIP,request,pipedOut); //此处需要自己输入，考虑写一个表，然后实现互联网分组
                    commWithServer.notifyCommWithServer();//将线程唤醒
                }else{
                    commWithServer.connect(serverIP,request,pipedOut);
                    commWithServer.start();//启动线程，与信息服务器通信
                }
            }
            //pipedIn读取缓存区的响应
            response=(Response)pipedIn.readObject();
        }catch (Exception ex){
            JOptionPane.showMessageDialog(this, "无法连接或与服务器通信出错","警告", JOptionPane.WARNING_MESSAGE);
            clear();
            return;
        }
        //收尾
        String message=response.getMessage();
        boolean flag=true;
        if(message!=null&&message.equals(request.getRegisterName()+",你已经注册成功！")){
            message+="现在可以查看一下该局域网有哪些用户在线了，请单击上方的\"好友列表\"";
            flag=false;
        }
        JOptionPane.showMessageDialog(null, message,"信息提示", JOptionPane.PLAIN_MESSAGE);
        if(flag){//注册没有成功，清除单行文本域，返回重新注册
            clear();
            return;
        }

        /*注册成功，将注册名传递给GetOnlineP2PEnds类对象，Chat类对象和Exit对象*/
        GetOnlineP2PEnds.setRegisterName(registerName);
        Chat.setRegisterName(registerName);
        Exit.setRegisterName(registerName);
        isRegister=true;//设置注册成功标志，控制不能重复注册

        //注册成功，开启接收消息线程
        new Thread(chat).start();
        clear();
    }
    private void clear(){
        registerNameField.setText(" ");
        serverIPField.setText(" ");
    }
}
