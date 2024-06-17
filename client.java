package client;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class client {
    static String filePath;
    static List<String> segments = null;
    static int Lmin = 0;
    static int Lmax = 0;
    private static String serverIP = null;
    static int sumnum = 0;
    int nowpi=1;
    String total="";
    private static int serverPort = 0;
    static Socket socket;
    OutputStream os = null;
    InputStream is = null;

    public client() {
        try {
            socket = new Socket(serverIP, serverPort);
            Baowen bw = new Baowen();
            bw.inbaowen();
            send(bw.getMessage());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] message) {
        try {
            os = socket.getOutputStream();
            os.write(message);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Baowen {
        private byte[] message;

        public void inbaowen() {
            short type = 1;
            int num = sumnum;
            ByteBuffer buffer = ByteBuffer.allocate(6);
            buffer.putShort(type);
            buffer.putInt(num);
            message = buffer.array();
        }

        public void requbaowen(String info) {
            short type = 3;
            byte[] data = info.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(6 + data.length);
            buffer.putShort(type);
            buffer.putInt(data.length);
            buffer.put(data);
            message = buffer.array();
        }

        public byte[] getMessage() {
            return message;
        }
    }

    class receive extends Thread {
    	public void writefile(String info) {
            String filePath = "D:/outputb.txt";
            try {
            	total=info+total;
                FileWriter writer = new FileWriter(filePath); 
                writer.write(total);
                writer.close();
            } catch (IOException e) {
                System.out.println("写入文件时出错: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                is = socket.getInputStream();
                DataInputStream dis = new DataInputStream(is);
                while (true) {
                    short type = dis.readShort();
                    if (type == 4) {
                        int length = dis.readInt();
                        byte[] data = new byte[length];
                        dis.readFully(data);
                        String info = new String(data);
                        System.out.println("第"+nowpi+"块"+info);
                        nowpi++;
                        writefile(info);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void file() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            segments = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                Random random = new Random();
                int lineLength = line.getBytes().length;
                int size = 0;
                int nowlength = 0;
                if (Lmin != Lmax) {
                    size = random.nextInt(Lmax - Lmin) + Lmin;
                } else {
                    size = Lmin;
                }
                while (nowlength + size <= lineLength) {
                    segments.add(line.substring(nowlength, nowlength + size));
                    nowlength += size;
                    if (Lmin != Lmax) {
                        size = random.nextInt(Lmax - Lmin) + Lmin;
                    }
                }
                if (nowlength < lineLength) {
                    segments.add(line.substring(nowlength));
                }
            }
            sumnum = segments.size();
        } catch (IOException e) {
            System.err.println("读取文件时发生错误：" + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入ServerIP:");  
        serverIP = scanner.next();  
        System.out.print("请输入ServerPort: ");
        serverPort = scanner.nextInt();
        System.out.print("请输入文件地址：");
        filePath = scanner.next();
        System.out.print("请输入最小字节长度：");
        Lmin = scanner.nextInt();
        System.out.print("请输入最大字节长度：");
        Lmax = scanner.nextInt();
        file();
        client clien = new client();
        for (String info : segments) {
            client.Baowen bw = clien.new Baowen();
            bw.requbaowen(info);
            clien.send(bw.getMessage());
        }
        client.receive re = clien.new receive();
        re.start();
    }
}



