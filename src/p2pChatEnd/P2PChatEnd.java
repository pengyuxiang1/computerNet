package p2pChatEnd;

import javax.swing.*;
import java.awt.*;

public class P2PChatEnd extends JFrame {
    private p2pChatEnd.Register register;
    private p2pChatEnd.GetOnlineP2PEnds getOnlineP2PEnds;
    private p2pChatEnd.Chat chat;
    private p2pChatEnd.Exit exit;
    private p2pChatEnd.CommWithServer commWithServer;//与服务器通信的线程

    private JLabel label;
    private JTabbedPane tabbedPane;

    //初始化界面
    public P2PChatEnd(){
        //界面设置
//        try{ javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); }catch(Exception e){ e.printStackTrace(); }
        try{
//            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");

            JFrame.setDefaultLookAndFeelDecorated(true);
            //            UIManager.setLookAndFeel("com.jtattoo.plaf.mint.MintLookAndFeel");
//            UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");com.jtattoo.plaf.hifi.HiFiLookAndFeel
            UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
        }catch(Exception e){ e.printStackTrace(); }


        setTitle("P2P聊天端");
        ImageIcon background = new ImageIcon("7.jpg");
        JLabel label = new JLabel(background);
        // 把标签的大小位置设置为图片刚好填充整个面板
        label.setBounds(0, 0, background.getIconWidth()/10, background.getIconHeight()/8);

        //创建好"commWithServer"的线程，并作为参数，传递给其他类
        commWithServer=new p2pChatEnd.CommWithServer();
        register=new p2pChatEnd.Register(commWithServer);
        chat=new p2pChatEnd.Chat(this);
        getOnlineP2PEnds=new p2pChatEnd.GetOnlineP2PEnds(commWithServer,chat);

        register.setChat(chat);  //为此注册用户绑定了对应的chat类
        exit=new p2pChatEnd.Exit(commWithServer,this);
        tabbedPane=new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.add("主页",label);
        tabbedPane.add("注册",register);
        tabbedPane.add("好友列表",getOnlineP2PEnds);
        tabbedPane.add("退出P2P",exit);

        add(tabbedPane, BorderLayout.CENTER);
        setBounds(520,260,600,547 );
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) {
        new P2PChatEnd();
    }
}
