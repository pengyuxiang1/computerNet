package p2pChatEnd;

import Iotry.input;
import Iotry.output;
import iofile.ServerFile;
import iofile.SocketFileJFrame;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Vector;

/**
 * 功能：
 * 1. 聊天过程中，检测其他P2P端是否都已经退出子线程，如果退出，则提示连接断开，是否也退出。同时接受其他端口来的tcp请求，如果是5，文件传输请求，则让output可视化。线程开启类（beginMonitor，在其他方法中肯定会被调用）
 * 2. 新建聊天窗口，此窗口可以支持群聊。
 * 3. 基础的发送信息功能。
 */

public class ChatWindow extends JFrame implements ActionListener, Runnable {
    //修改
    private JButton sendFileButton;
    private JButton receiFileButton;
    private JButton expression;
    private JTextArea messageArea,inputArea;
    private JButton sendButton,quitButton,clearButton;
    private JLabel hintMessage1,hintMessage2,statusBar;

    private String registerName;
    private DatagramSocket socket;
    private Vector<InetSocketAddress> chatP2PEndAddress;
    private P2PChatEnd p2pChatEnd;

    private Thread chatP2PEndMonitor;                                             //聊天过程中，检测其他P2P端是否都已经退出子线程  ？？
    private boolean monitoring;
    private String newLine;
    public ChatWindow(String registerName, DatagramSocket socket, P2PChatEnd p2pChatEnd){
        //初始化
        super("聊天窗口");
        this.registerName=registerName;
        this.socket=socket;
        this.p2pChatEnd=p2pChatEnd;

        monitoring=true;
        newLine= System.getProperty("line.separator");//换行符，作用与'\n'相同，这样可以保证在其他操作系统也能过执行
        //界面初始化
        hintMessage1=new JLabel("显示聊天记录");
        hintMessage2=new JLabel("编辑信息");
        messageArea=new JTextArea(4, 20);
        messageArea.setEditable(false);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        inputArea=new JTextArea(4, 20);
        inputArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
         sendButton=new JButton("发送");
         sendButton.addActionListener(this);
         quitButton=new JButton("退出");
         quitButton.addActionListener(this);
         clearButton=new JButton("清空");//清空当前的聊天记录
         clearButton.addActionListener(this);
         statusBar=new JLabel("在线： "+registerName);
         statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
         JPanel messagePanel=new JPanel();
         messagePanel.setLayout(new BorderLayout());
         messagePanel.add(hintMessage1, BorderLayout.NORTH);
         messagePanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
         JPanel buttonPanel=new JPanel();
         buttonPanel.setLayout(new GridLayout(3,1));
        //新建
        sendFileButton=new JButton("发送文件");
        sendFileButton.addActionListener(this);
        receiFileButton=new JButton("接收文件");
        receiFileButton.addActionListener(this);
        expression=new JButton("表情");
        buttonPanel.add(sendButton);
        buttonPanel.add(expression);

         //新建
        buttonPanel.add(sendFileButton);
        buttonPanel.add(receiFileButton);
        buttonPanel.add(quitButton);
        buttonPanel.add(clearButton);

         Box box1=new Box(BoxLayout.X_AXIS);
         box1.add(new JScrollPane(inputArea));
         box1.add(buttonPanel);
         Box box=new Box(BoxLayout.Y_AXIS);
         box.add(hintMessage2);
         box.add(box1);
         messagePanel.add(box, BorderLayout.SOUTH);
         Container contenPane=getContentPane();
         contenPane.add(messagePanel, BorderLayout.CENTER);
         contenPane.add(statusBar, BorderLayout.SOUTH);
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 close();
             }
         });
         setSize(500,500 );
         Exit.setChatWindow(this);

    }

    public void setChatP2PEndAddress(Vector<InetSocketAddress> chatP2PEndAddress){//获得聊天对象地址列表
        this.chatP2PEndAddress=chatP2PEndAddress;
    }
    //新建线程管理类
    public void beginMonitor(boolean monitoring){
        this.monitoring=monitoring;
        chatP2PEndMonitor=new Thread(this);
        chatP2PEndMonitor.start();  //功能：检测对方是否退出。
        //这里可以加一个线程：负责监听是否有必要开启文件传输窗口  直接用server就好了。
    }

    //接收窗口开始监听：
//    private static ServerSocket server = null;
//    public void listenTcp(){
//        try {
//            //启动服务器
//            new Thread() {
//                public void run() {
//                    try {
//                        if (server != null && !server.isClosed()) {
//                            server.close();
//                        }
//                        server = new ServerSocket(8080);//server = new ServerSocket(list.getByName(注册名));
////                        new ServerFile(server.accept(), progressBar_2).start();
//                        new ServerFile(server.accept()).start();
//                        System.out.println("listenTcp启动");
//                    } catch (IOException e) {
//                        JOptionPane.showMessageDialog(null, "IOException");
//                    }
//                }
//            }.start();
//        } catch (Exception e1) {
//            JOptionPane.showMessageDialog(null, "Exception");
//        }
//    }
    //显示对话功能
    public void setReceived(String received){
        messageArea.append(received+newLine);
    }
    //退出功能，自动发送再见
    public void endChat(InetSocketAddress isa){//向聊天对象也发送一个再见
        String message=registerName+">再见！";
        messageArea.append(message+newLine);
        byte[] buf=message.getBytes();
        try{
            DatagramPacket packet=null;
            packet=new DatagramPacket(buf,buf.length ,isa.getAddress() ,isa.getPort() );
            socket.send(packet);
        }catch (IOException ee){
            JOptionPane.showMessageDialog(this, "发送|再见|时，网络连接错误!");
        }
        chatP2PEndAddress.remove(isa);
    }

    //发送+退出+清空聊天界面+文件传输
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==sendButton){
            String message=registerName+">"+inputArea.getText().trim();
            sendMessage(message);
            inputArea.setText("");
        }
        if(e.getSource()==quitButton){
            close();
        }
        if(e.getSource()==clearButton){
            messageArea.setText("");
        }
        if(e.getSource()==sendFileButton){
            input in = new input(chatP2PEndAddress);//也许需要传入地址表input.setChatP2PEndAddress(chatP2PEndAddress)
            in.setVisible(true);
        }
        if(e.getSource()==receiFileButton){
            output get = new output();
            get.setVisible(true);
        }
    }
    //发送功能：包括了群聊
    public void sendMessage(String message){
        messageArea.append(message+newLine);
        byte[] buf=message.getBytes();
        DatagramPacket packet=null;
        try {
            for(InetSocketAddress isa:chatP2PEndAddress){//将信息发送给聊天对象地址列表中每一个（群聊功能的实现）
                packet=new DatagramPacket(buf,buf.length ,isa.getAddress(),isa.getPort());
                System.out.println(isa.getAddress()+" ："+isa.getPort());
                //UDP是无连接通信，获得IP地址和端口号即可通信
                socket.send(packet);
            }
        }catch (IOException ee){
            JOptionPane.showMessageDialog(this, "发送信息时，网络连接错误！");
        }
    }
    public void close(){
        if(!chatP2PEndAddress.isEmpty()){
            int option= JOptionPane.showConfirmDialog(this, "正在聊天，确认要退出窗口吗？");
            if(option!=0)
                return;
            String message=registerName+">再见";
            sendMessage(message);
            monitoring=false;
            int i=0;
            do{

            }while (!chatP2PEndAddress.isEmpty()&&++i<=30);         //延迟30秒
        }
        monitoring=false;
        chatP2PEndMonitor=null;
        //如果主界面不存在或者主界面不可见就退出程序
        if(p2pChatEnd==null||!p2pChatEnd.isVisible())
            System.exit(0);
        else{
            setVisible(false);
            messageArea.setText("");
        }
    }

    //此线程的主要功能是为了检测对方是否退出。
    public void run() {
        while(monitoring){
            input.setChatP2PEndAddress(chatP2PEndAddress);
            if(!isVisible()||!chatP2PEndAddress.isEmpty())
                continue;
            int option= JOptionPane.showConfirmDialog(this, "对方都已经退出，是否关闭本窗口？");
            if(option!=0){
                try {
                    chatP2PEndMonitor.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                continue;
            }
            close();
        }
    }
}
