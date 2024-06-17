package server;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class server {
    private static int SERVERPORT = 2428;
    static ServerSocket socketServer = null;
    
    public void startserver() {
        while (true) {
            try {
                Socket socket = socketServer.accept();
                System.out.println(socket.getRemoteSocketAddress() + "连接成功");
                new ServerReaderThread(socket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void send(byte[] message,Socket socket) {
        try {
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			dos.write(message);
            dos.flush();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
    }
    
    class BW{
    	 byte[] message = new byte[6];
    	 
    	 public void ansbaowen(String info)
    	 {
    		 ByteBuffer answerBuffer = ByteBuffer.allocate(6 + info.length());
             answerBuffer.putShort((short) 4);
             answerBuffer.putInt(info.length());
             answerBuffer.put(info.getBytes());
             message = answerBuffer.array();
    	 }
    	 
    	 public void agrbaowen()
    	 {
    		 ByteBuffer agreeBuffer = ByteBuffer.allocate(2);
             agreeBuffer.putShort((short) 2);
             message = agreeBuffer.array();
    	 }
    	 
    	 public byte[] getMessage() {
             return message;
         }
    }
    
    class ServerReaderThread extends Thread {
        private Socket socket;

        public ServerReaderThread(Socket socket) {
            this.socket = socket;
        }

        public String reverseWord(String old) {
            return new StringBuilder(old).reverse().toString();
        }

        @Override
        public void run() {
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                // 接收 Initialization 报文
                byte[] initMessage = new byte[6];
                dis.readFully(initMessage);
                ByteBuffer initBuffer = ByteBuffer.wrap(initMessage);
                short type = initBuffer.getShort();
                if (type == 1) {
                    int numBlocks = initBuffer.getInt();
                    System.out.println("总共有"+numBlocks+"块");

                    // 发送 agree 报文
                    BW bwa=new BW();
                    bwa.agrbaowen();
                    send(bwa.getMessage(),socket);
                }

                while (true) {
                    try {
                        // 接收 request 报文
                        byte[] header = new byte[6];
                        dis.readFully(header);
                        ByteBuffer headerBuffer = ByteBuffer.wrap(header);
                        type = headerBuffer.getShort();
                        if (type == 3) {
                            int length = headerBuffer.getInt();
                            byte[] data = new byte[length];
                            dis.readFully(data);
                            String requestData = new String(data);
//                            System.out.println("Received: " + requestData);
                            String reversedData = reverseWord(requestData);
                            // 发送 answer 报文
                            BW bw=new BW();
                            bw.ansbaowen(reversedData);
                            send(bw.getMessage(),socket);
                        }
                    } catch (Exception e) {
                        System.out.println(socket.getRemoteSocketAddress() + "断开连接");
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("------------服务端启动--------------");
        socketServer = new ServerSocket(SERVERPORT);
        server ser = new server();
        ser.startserver();
    }
}









