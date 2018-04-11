import java.io.*;
import java.net.*;
import java.util.Scanner;

public class HerCDNAuthoritativeDns {
private static final int PORT = 40294;

private static DatagramPacket receivePacket;
private static DatagramSocket serverSocket;

public static void main(String[] args) throws Exception {
        new HisCinemaAuthoritativeDns().runUDPServer();
}

public void runUDPServer() throws Exception {
        serverSocket = new DatagramSocket(PORT);

        // serverSocket.setSoTimeout(60000); // Set timeout for server of 1 minute
        System.out.println("HerCDN Authoritative Server up and ready...");

        while(true) {
                String filePath = "src/HerCDNFiles/herrecords.txt";
                File file = new File(filePath);
                Scanner scan = new Scanner(file);

                while(scan.hasNext()) {
                        String line = scan.nextLine().toString(); //Error here!!!!
                        if(line.contains(receiveData().trim())) {
                                sendData(line);
                                System.out.println("File sent successfully to: " + serverSocket.getInetAddress().getHostName());
                        }
                }
                scan.close();
        }
}

public String receiveData() throws Exception {
        String query;
        byte[] receiveData = new byte[1024];

        receivePacket =
                new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);

        query = new String(receivePacket.getData());
        return query;
}

public void sendData(String message) throws Exception {
        byte[] sendData = new byte[1024];
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();

        sendData = message.getBytes();

        DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, IPAddress, port);
        serverSocket.send(sendPacket);
}
}
