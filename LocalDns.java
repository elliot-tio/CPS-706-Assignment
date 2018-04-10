import java.io.*;
import java.net.*;

public class LocalDns {
private static final int PORT = 40300;
private static final String[] records = { "(herCDN.com, NSherCDN.com, NS)",
                                          "(NSherCDN.com, 127.0.0.1, A)",
                                          "(video.hiscinema.com, NShiscinema.com, NS)",
                                          "(NShiscinema.com, 8.8.8.8, A)" };

public static void main(String args[]) throws Exception
{


        DatagramSocket connectionSocket = new DatagramSocket(PORT);
        connectionSocket.setSoTimeout(60000);
        System.out.println("Local DNS up and ready...");
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        while(true) {

                // accept request from client
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                connectionSocket.receive(packet);
                String query = new String(packet.getData());
                System.out.println("RECEIVED: " + query);

                // need to remove this to change target
                InetAddress IPAddress = packet.getAddress();
                int port = packet.getPort();

                // change this to get record based on query
                // find ip address based on client request, format: video.hiscinema.com/F1
                String parsedQuery = parseQuery(query);     // extracts "video.hiscinema.com" from query, full form
                                                            // "http://video.hiscinema.com/F1:80"

                String returnip = "";

                // extract ip from records
                for(String record:records) {
                        if(record.contains("NS") && record.contains(parsedQuery)) { // checks for NS type
                                String value = record.split(", ")[1];   // splits the record by comma, takes first argument
                                for(String entry:records) {
                                        if(entry.contains("A") && entry.contains(value)) { // checks for A type
                                                returnip = entry.split(", ")[1];           // extracts ipaddress
                                        }
                                }
                        }
                }

                sendData = returnip.getBytes();
                // currently sending packet back to client, should target extracted ipaddress
                DatagramPacket sendPacket =
                        new DatagramPacket(sendData, sendData.length, IPAddress, port);

                connectionSocket.send(sendPacket);

                // receive reply from hiscinemadns, which is ip address of hercdndns, type ns

                // send query to hercdndns

                // receive reply from hercdndns, which is ip address of content server, type a

        }
}

public static String parseQuery(String message){
        String[] tokens = message.split("/");
        message = tokens[2].toString();
        return message;
}
}
