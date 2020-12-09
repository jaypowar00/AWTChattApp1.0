import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatApp {
    public static void main(String[] args) {
        ChatAppUI ui = new ChatAppUI();
    }
}

class ChatAppUI extends Frame implements ActionListener {

    static volatile String recvData, sendData, errMsg;
    static String serverip = "";
    static String serverport = "";
    static boolean serverActive = false, clientActive = false, errorIO = false, leftmsg=false;
    static ServerSocket ss = null;
    static Socket cs = null;
    static boolean dataSent = false;
    static boolean dataRecv = false;
    static Thread runServ, runClient;

    int flag = 0, createflag = 0, joinflag = 0, msgTyped = 0;
    Label l1, l2;
    Button createServer, joinServer;
    TextArea ChatOutput;
    TextField TypeMsg;

    static Dialog d;
    static Label dl1, dl2, dl3;
    static Button db1, db2;
    static TextField dip, dport;

    public ChatAppUI() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        super.setTitle("App Chat");
        super.setSize(420, 460);
        super.setLocationRelativeTo(null);
        super.setVisible(true);
        super.setResizable(false);
        setLayout(null);

        l1 = new Label("Welcome to AppChat");
        l1.setBounds(150, 26, 120, 30);
        l2 = new Label("status: Disconnected!");
        l2.setBounds(150, 50, 120, 30);
        l2.setForeground(Color.red);


        createServer = new Button("Create Server");
        createServer.setBounds(10, 50, 100, 30);
        joinServer = new Button("Join Server");
        joinServer.setBounds(310, 50, 100, 30);

        ChatOutput = new TextArea("status: Disconnected", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
        ChatOutput.setEditable(false);
        ChatOutput.setEnabled(false);
        ChatOutput.setBounds(10, 90, 400, 330);
        ChatOutput.setCursor(Cursor.getDefaultCursor());

        TypeMsg = new TextField("status: Disconnected");
        TypeMsg.setBounds(10, 420, 400, 30);
        TypeMsg.setEnabled(false);

        add(l1);
        add(l2);
        add(createServer);
        add(joinServer);
        add(ChatOutput);
        add(TypeMsg);


        createServer.addActionListener(this);
        joinServer.addActionListener(this);
        TypeMsg.addActionListener(this);
    }

    public void paint(Graphics g) {
        super.paint(g);
        System.out.println("lol2");
        if (createflag==2 & !serverActive){
            System.out.println("server stop paint");
            if (ChatOutput.isEnabled())
                ChatOutput.setEnabled(false);
            createflag=0;
            ChatOutput.setText(ChatOutput.getText()+"\n\n$ Server Stopped !");
            ChatOutput.setCaretPosition(ChatOutput.getText().length());
            createServer.setLabel("Create Server");
        }
        if (createflag == 1 & serverActive) {
            System.out.println("server create paint");
            if (!ChatOutput.isEnabled())
                ChatOutput.setEnabled(true);
            createflag = 0;
            ChatOutput.setText(ChatOutput.getText() + "\n\n$ Server - Created!\n[+] Ip Address: " + serverip + "\n[+] Port No.: " + serverport);
            createServer.setLabel("Stop Server");
            ChatOutput.setCaretPosition(ChatOutput.getText().length());
        }
        if (joinflag == 1 & clientActive) {
            System.out.println("client connected to server paint");
            joinflag = 0;
            if (!ChatOutput.isEnabled())
                ChatOutput.setEnabled(true);
            ChatOutput.setText(ChatOutput.getText() + "\n\n[+] Connected to Server (" + serverip + "@" + serverport + ")");
            ChatOutput.setCaretPosition(ChatOutput.getText().length());
            joinServer.setLabel("Disconnect");
            l2.setText("status: Connected!");
            l2.setForeground(Color.green);
            TypeMsg.setEnabled(true);
            TypeMsg.setText("");
        }
        if (joinflag==2 & !clientActive){
            System.out.println("client disconnected paint");
            joinflag=0;
            if (ChatOutput.isEnabled())
                ChatOutput.setEnabled(false);
            if (TypeMsg.isEnabled())
                TypeMsg.setEnabled(false);
            l2.setText("status: Disconnected!");
            l2.setForeground(Color.red);
            serverip="";
            serverport="";
            ChatOutput.setText(ChatOutput.getText()+"\n\nDisconnected from server!");
            ChatOutput.setCaretPosition(ChatOutput.getText().length());
            joinServer.setLabel("Join Server");
        } else if (msgTyped == 1) {
            msgTyped = 0;
            ChatOutput.setText(ChatOutput.getText() + "\nYou: " + TypeMsg.getText());
            ChatOutput.setCaretPosition(ChatOutput.getText().length());
            TypeMsg.setText("");
        } else if (dataRecv) {
            dataRecv = false;
            ChatOutput.setText(ChatOutput.getText() + "\n" + recvData);
            ChatOutput.setCaretPosition(ChatOutput.getText().length());
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object ob = e.getSource();
        if (ob == createServer) {
            System.out.println("creating");
            if (!serverActive)
                showDialogueFrame(this, "Create Server", this.getLocation(), 0);
            else{
                try {
                    ss.close();
                    ss=null;
                } catch (IOException e3) {
                    System.out.println("Error: "+e3.getLocalizedMessage());
                }
                serverActive=false;
                createflag=2;
                if (clientActive){
                    try {
                        cs.close();
                        cs=null;
                    } catch (IOException e3) {
                        System.out.println("Error: "+e3.getLocalizedMessage());
                    }
                    clientActive=false;
                    joinflag=2;
                }
                repaint();
            }
        } else if (ob == joinServer) {
            System.out.println("joining");
            if (!clientActive)
                showDialogueFrame(this, "Join Server", this.getLocation(), 1);
            else{
                leftmsg=true;
                dataSent=true;
                try {
                    Thread.sleep(205);
                } catch (InterruptedException ignored) {}
                try {
                    cs.close();
                    cs=null;
                } catch (IOException e3) {
                    System.out.println("Error: "+e3.getLocalizedMessage());
                }
                clientActive=false;
                joinflag=2;
                repaint();
            }
        } else if (ob == TypeMsg) {
            msgTyped = 1;
            sendData = TypeMsg.getText();
            dataSent = true;
        }
    }

    public void showDialogueFrame(Frame f, String str, Point pt, int task) {
        d = new Dialog(f, str, true);

        d.setLayout(null);
        d.setResizable(false);
        d.setSize(300, 300);
        d.setLocation(pt.x + 50, pt.y + 50);

        d.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                d.dispose();
            }
        });
        dl1 = new Label("[+] Task : " + str);
        dl1.setForeground(Color.blue);
        dl1.setBounds(10, 35, 200, 30);

        db2 = new Button("Close");
        db2.setBounds(240, 35, 50, 30);
        db2.addActionListener(e -> ChatAppUI.d.dispose());

        d.add(dl1);
        d.add(db2);

        switch (task) {
            case 0 -> {
                dl2 = new Label("Enter IP Address (localhost if not provided):");
                dl2.setBounds(40, 95, 225, 30);
                dip = new TextField("");
                dip.setBounds(100, 135, 100, 30);
                db1 = new Button("Create Server");
                db1.setBounds(100, 180, 100, 30);
                d.add(db1);
                d.add(dl2);
                d.add(dip);
                db1.addActionListener(e -> {
                    System.out.println("create server clicked");
                    runServ = new Thread(new ChatServer());
                    runServ.start();
                    createflag = 1;
                    try {
                        runServ.join(1000);
                    } catch (InterruptedException interruptedException) {
                        serverActive = false;
                        createflag = 0;
                    }
                    if (serverActive)
                        repaint();
                    Toolkit.getDefaultToolkit().beep();
                    d.dispose();
                });
            }
            case 1 -> {
                dl2 = new Label("Enter IP Address:");
                dl2.setBounds(20, 60, 100, 30);
                dip = new TextField("");
                dip.setBounds(20, 95, 100, 30);
                dl3 = new Label("Enter Port Number:");
                dl3.setBounds(20, 130, 100, 30);
                dport = new TextField("");
                dport.setBounds(20, 165, 100, 30);
                db2 = new Button("Connect");
                db2.setBounds(100, 220, 100, 30);
                d.add(dl2);
                d.add(dip);
                d.add(dl3);
                d.add(dport);
                d.add(db2);
                db2.addActionListener(e -> {
                    runClient = new Thread(new ChatClient());
                    runClient.start();
                    try {
                        runClient.join(1000);
                        if (serverip.isEmpty() | !serverip.equals(dip.getText()))
                            if (dip.getText().equals(""))
                                serverip = "127.0.0.1";
                            else
                                serverip = dip.getText();
                        if (serverport.isEmpty() | !serverport.equals(dport.getText()))
                            serverport = dport.getText();
                    } catch (InterruptedException interruptedException) {
                        joinflag = 0;
                    }
                    if (joinflag == 0)
                        ChatAppUI.super.repaint();
                    d.dispose();
                });
            }
        }
        d.setVisible(true);
    }

    public static class ChatServer implements Runnable {

        static Vector<ClientHandler> ar = new Vector<>();
        static int i = 0;

        public void run() {
            System.out.println("in chatserver class");
            int socket = 0;
            while (socket == 0) {
                try {
                    if (!dip.getText().equals(""))
                        ss = new ServerSocket(new Random().nextInt(16384) + 49152, 50, InetAddress.getByName(dip.getText()));
                    else
                        ss = new ServerSocket(new Random().nextInt(16384) + 49152, 50, InetAddress.getByName("127.0.0.1"));
                    socket = 1;
                    serverip = ss.getInetAddress().toString().split("/")[1];
                    serverport = String.valueOf(ss.getLocalPort());
                    serverActive = true;
                } catch (IOException ignored) {
                    serverActive=false;
                }
            }
            while (true) {
                if (ss==null){
                    serverActive = false;
                    break;
                }
                else if (ss.isClosed()) {
                    serverActive = false;
                    break;
                }
                try {
                    Socket s = ss.accept();
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                    ClientHandler mtch = new ClientHandler(s, "client " + i, dis, dos);
                    Thread t = new Thread(mtch);
                    ar.add(mtch);
                    t.start();
                    i++;
                } catch (IOException ignored) {
                }
            }
        }
    }

    static class ClientHandler implements Runnable {
        Scanner sc = new Scanner(System.in);
        private String name;
        final DataInputStream dis;
        final DataOutputStream dos;
        Socket s;
        boolean isActive;

        public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos) {
            this.name = name;
            this.s = s;
            this.dos = dos;
            this.dis = dis;
            this.isActive = true;
        }

        public void run() {
            try {
                for (ClientHandler mc : ChatServer.ar) {
                    if (!mc.name.equals(this.name) & mc.isActive) {
                        mc.dos.writeUTF("[+] " + this.name + " has just joined the Server.");
                    }
                }
            } catch (IOException e) {
                System.out.println("[+] Error: " + e.getMessage());
            }
            String received;
            while (true) {
                try {
                    received = dis.readUTF();
                    if (received.equals("exit")) {
                        this.isActive = false;
                        for (ClientHandler mc : ChatServer.ar) {
                            if (!mc.name.equals(this.name) & mc.isActive) {
                                mc.dos.writeUTF("[+] " + this.name + " has left the server.");
                            }
                        }
                        this.s.close();
                        break;
                    }
                    for (ClientHandler mc : ChatServer.ar) {
                        if (!mc.name.equals(this.name) & mc.isActive) {
                            mc.dos.writeUTF(this.name + " : " + received);
                        }
                    }
                } catch (IOException ignored) {
                }
            }
            try {
                this.dos.close();
                this.dis.close();
            } catch (IOException ignored) {
            }
        }
    }

    public class ChatClient implements Runnable {
        public void run() {
            try {
                System.out.println("in client class");
                clientActive = true;
                InetAddress ip;
                if (dip.getText().equals(""))
                    ip = InetAddress.getByName("127.0.0.1");
                else
                    ip = InetAddress.getByName(dip.getText());
                cs = new Socket(ip, Integer.parseInt(dport.getText()));
                System.out.println("in client class");
                Toolkit.getDefaultToolkit().beep();
                joinflag = 1;
                ChatAppUI.super.repaint();
                DataInputStream dis = new DataInputStream(cs.getInputStream());
                DataOutputStream dos = new DataOutputStream(cs.getOutputStream());
                Thread sendMsg = new Thread(() -> {
                    while (true) {
                        if (cs==null)
                            break;
                        else if (cs.isClosed())
                            break;
                        if (dataSent) {
                            System.out.println("dataSent true");
                            try {
                                if (leftmsg) {
                                    sendData = "exit";
                                    leftmsg=false;
                                }
                                dos.writeUTF(sendData);
                                dos.flush();
                                dataSent = false;
                                if (sendData.equals("exit")) {
                                    clientActive = false;
                                    ChatAppUI.super.repaint();
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                                System.out.println("[+] msg sent" + sendData);
                                ChatAppUI.super.repaint();
                            } catch (IOException e) {
                                errMsg = "Error occured while sending msg.";
                                errorIO = true;
                                dataSent = false;
                                ChatAppUI.super.repaint();
                            }
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            System.out.println("[+] Error " + e.getLocalizedMessage());
                        }
                    }
                });
                Thread recvMsg = new Thread(() -> {
                    while (true) {
                        if (cs==null)
                            break;
                        else if (cs.isClosed())
                            break;
                        try {
                            recvData = dis.readUTF();
                            dataRecv = true;
                            System.out.println("[+] msg received" + recvData);
                            ChatAppUI.super.repaint();
                        } catch (IOException e) {
                            errorIO = true;
                            errMsg = "[+] Error while reading the msg";
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            System.out.println("[+] Error: " + e.getLocalizedMessage());
                        }
                    }
                });
                sendMsg.start();
                recvMsg.start();
                while (!sendMsg.isInterrupted()) {
                }
                recvMsg.interrupt();
                clientActive = false;
                cs.close();
            } catch (IOException e) {
                clientActive = false;
                joinflag=0;
                Thread.currentThread().interrupt();
            }
        }
    }
}
