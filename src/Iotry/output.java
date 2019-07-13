package Iotry;

import iofile.ServerFile;
import iofile.SocketFileJFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;

public class output extends JFrame {
    private JPanel contentPane;
    private JTextField textField;
    private JProgressBar progressBar_2;
    private OutputStream outputStream;
    private InputStream inputStream;
    private String fileName;
    private int len;
    private long size;

    private static ServerSocket server = null;

//    public static void main(String[] args) {
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                try {
//                    output frame = new output();
//                    frame.setVisible(true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    public output() {
        setIconImage(Toolkit.getDefaultToolkit()
                .getImage(SocketFileJFrame.class.getResource("/javax/swing/plaf/metal/icons/ocean/newFolder.gif")));
        setForeground(Color.WHITE);

        setResizable(false);
        setTitle("局域网文件传输 V1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int width = Toolkit.getDefaultToolkit().getScreenSize().width;//获取分辨率宽
        int heiht = Toolkit.getDefaultToolkit().getScreenSize().height;//获取分辨率高

        //分辨率宽高减去软件的宽高除以2把软件居中显示
        setBounds((width - 500) / 2, (heiht - 272) / 2, 500, 272);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);


        //文件接受服务窗口
        JPanel panel_1 = new JPanel();
        panel_1.setToolTipText("");
        panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "文件接受服务窗口", TitledBorder.LEADING, TitledBorder.TOP, null,
                new Color(0, 0, 0)));
        panel_1.setBackground(Color.WHITE);
        panel_1.setBounds(39, 28, 400, 119);
        contentPane.add(panel_1);
        panel_1.setLayout(null);

        //端口标签
        JLabel label = new JLabel("接收端口:");
        label.setFont(new Font("新宋体", Font.PLAIN, 22));
        label.setBounds(16, 31, 100, 35);
        panel_1.add(label);
        //端口文本框
        textField = new JTextField();
        textField.setFont(new Font("宋体", Font.PLAIN, 19));
        textField.setText("8080");
        textField.setBounds(129, 36, 100, 26);
        panel_1.add(textField);
        textField.setColumns(10);

        //服务器关闭启动的按钮
        JToggleButton tglbtnNewToggleButton = new JToggleButton("启动服务器");
        tglbtnNewToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (tglbtnNewToggleButton.isSelected()) {
                    //如果是按下显示关闭服务器
                    tglbtnNewToggleButton.setText("关闭服务器");
                    textField.setEnabled(false);//按下之后 端口文本框要设置不能写入
                    try {
                        //启动服务器
                        new Thread() {
                            public void run() {
                                try {
                                    if (server != null && !server.isClosed()) {
                                        server.close();
                                    }
                                    server = new ServerSocket(8080);//server = new ServerSocket(list.getByName(注册名));
                                    new ServerFile(server.accept(), progressBar_2).start();
                                } catch (IOException e) {
                                    JOptionPane.showMessageDialog(null, "IOException");
                                }
                            }
                        }.start();
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(null, "Exception");
                    }
                } else {
                    //否启动服务器
                    tglbtnNewToggleButton.setText("启动服务器");
                    textField.setEnabled(true);////弹起之后 端口文本框要设置可写状态
                    ServerFile.closeServer();//关闭服务器
                }

            }
        });
        tglbtnNewToggleButton.setFont(new Font("微软雅黑 Light", Font.PLAIN, 19));
        tglbtnNewToggleButton.setBackground(Color.WHITE);
        tglbtnNewToggleButton.setForeground(Color.DARK_GRAY);
        tglbtnNewToggleButton.setBounds(245, 34, 138, 28);
        panel_1.add(tglbtnNewToggleButton);

        //文件接收端的进度条
        progressBar_2 = new JProgressBar();
        progressBar_2.setBackground(Color.WHITE);
        progressBar_2.setForeground(new Color(255, 218, 185));
        progressBar_2.setStringPainted(true);
        progressBar_2.setBounds(60, 90, 250, 14);
        panel_1.add(progressBar_2);
    }


    //try
//    public output(OutputStream outputStream,InputStream inputStream,String fileName,long size){
//        setIconImage(Toolkit.getDefaultToolkit()
//                .getImage(SocketFileJFrame.class.getResource("/javax/swing/plaf/metal/icons/ocean/newFolder.gif")));
//        setForeground(Color.WHITE);
//
//        setResizable(false);
//        setTitle("局域网文件传输 V1.0");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        int width = Toolkit.getDefaultToolkit().getScreenSize().width;//获取分辨率宽
//        int heiht = Toolkit.getDefaultToolkit().getScreenSize().height;//获取分辨率高
//
//        //分辨率宽高减去软件的宽高除以2把软件居中显示
//        setBounds((width - 500) / 2, (heiht - 272) / 2, 500, 272);
//        contentPane = new JPanel();
//        contentPane.setBackground(Color.WHITE);
//        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
//        setContentPane(contentPane);
//        contentPane.setLayout(null);
//
//
//        //文件接受服务窗口
//        JPanel panel_1 = new JPanel();
//        panel_1.setToolTipText("");
//        panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
//                "文件接受服务窗口", TitledBorder.LEADING, TitledBorder.TOP, null,
//                new Color(0, 0, 0)));
//        panel_1.setBackground(Color.WHITE);
//        panel_1.setBounds(39, 28, 400, 119);
//        contentPane.add(panel_1);
//        panel_1.setLayout(null);
//
//        //端口标签
//        JLabel label = new JLabel("接收端口:");
//        label.setFont(new Font("新宋体", Font.PLAIN, 22));
//        label.setBounds(16, 31, 100, 35);
//        panel_1.add(label);
//        //端口文本框
//        textField = new JTextField();
//        textField.setFont(new Font("宋体", Font.PLAIN, 19));
//        textField.setText("8080");
//        textField.setBounds(129, 36, 100, 26);
//        panel_1.add(textField);
//        textField.setColumns(10);
//
//        //服务器关闭启动的按钮
//        JToggleButton tglbtnNewToggleButton = new JToggleButton("启动服务器");
//        tglbtnNewToggleButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                if (tglbtnNewToggleButton.isSelected()) {
//                    //如果是按下显示关闭服务器
//                    tglbtnNewToggleButton.setText("关闭服务器");
//                    textField.setEnabled(false);//按下之后 端口文本框要设置不能写入
//                    try {
//                        //启动服务器
//                        new Thread() {
//                            public void run() {
//                                try {
//                                    if (server != null && !server.isClosed()) {
//                                        server.close();
//                                    }
//                                    server = new ServerSocket(8080);//server = new ServerSocket(list.getByName(注册名));
//                                    new ServerFile(server.accept(), progressBar_2).start();
//                                } catch (IOException e) {
//                                    JOptionPane.showMessageDialog(null, "IOException");
//                                }
//                            }
//                        }.start();
//                    } catch (Exception e1) {
//                        JOptionPane.showMessageDialog(null, "Exception");
//                    }
//                } else {
//                    //否启动服务器
//                    tglbtnNewToggleButton.setText("启动服务器");
//                    textField.setEnabled(true);////弹起之后 端口文本框要设置可写状态
//                    ServerFile.closeServer();//关闭服务器
//                }
//
//            }
//        });
//        tglbtnNewToggleButton.setFont(new Font("微软雅黑 Light", Font.PLAIN, 19));
//        tglbtnNewToggleButton.setBackground(Color.WHITE);
//        tglbtnNewToggleButton.setForeground(Color.DARK_GRAY);
//        tglbtnNewToggleButton.setBounds(245, 34, 138, 28);
//        panel_1.add(tglbtnNewToggleButton);
//
//        //文件接收端的进度条
//        progressBar_2 = new JProgressBar();
//        progressBar_2.setBackground(Color.WHITE);
//        progressBar_2.setForeground(new Color(255, 218, 185));
//        progressBar_2.setStringPainted(true);
//        progressBar_2.setBounds(60, 90, 250, 14);
//        panel_1.add(progressBar_2);
//
//        this.outputStream=outputStream;
//        this.inputStream=inputStream;
//        this.fileName=fileName;
//        this.size=size;
//
//        try {
//            System.out.println("探测output");
//            outputStream.write("ok".getBytes());
//            outputStream.flush();
//            JFileChooser jf = new JFileChooser();
//            // 存储到本地路径的夹子
//            jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//            jf.showOpenDialog(null);
//            progressBar_2.setMaximum((int) (size / 10000));
//            FileOutputStream fout = new FileOutputStream(new File(jf.getSelectedFile(), fileName));
//            byte[] b = new byte[1024 * 1024 * 2];
//            long size1 = 0;
//            while ((len = inputStream.read(b)) != -1) {
//                fout.write(b, 0, len);
//                size1 += len;
//                progressBar_2.setValue((int) (size1 / 10000));
//            }
//            fout.close();
//            inputStream.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
}
