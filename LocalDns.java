import java.io.*;
import java.net.*;

public class LocalDns {
private static final int LOCAL_PORT = 40300;
private static final int HIS_PORT = 40301;
private static final int HER_PORT = 40302;
private static final String[] records = { "(herCDN.com, NSherCDN.com, NS)",
                                          "(NSherCDN.com, 127.0.0.1, A)",
                                          "(hiscinema.com, NShiscinema.com, NS)",
                                          "(NShiscinema.com, 127.0.0.1, A)" };

public static void main(String args[]) throws Exception
{


        DatagramSocket connectionSocket = new DatagramSocket(LOCAL_PORT);
        connectionSocket.setSoTimeout(60000);
        System.out.println("Local DNS up and ready...");
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        while(true) {

                // accept request from client (string video.hiscinema.com)
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                connectionSocket.receive(packet);
                String query = new String(packet.getData());
                System.out.println("RECEIVED: " + query);

                InetAddress clientIP = packet.getAddress();
                int port = packet.getPort();

                // change this to get record based on query
                // find ip address based on client request, format: video.hiscinema.com/F1
                String parsedQuery = parseQuery(query);     // extracts "video.hiscinema.com" from query, full form
                                                            // "http://video.hiscinema.com/F1:80"

                String returnip = "";
                String returnRecords[] = new String[3];

                // extract ip from records
                for(String record:records) {
                        if(record.contains("NS") && record.contains(parsedQuery)) { // checks for NS type
                                String value = record.split(", ")[1].trim();   // splits the record by comma, takes first argument
                                if(!returnRecords.contains(record)) {
                                returnRecords.add(record);
                                }
                                for(String entry:records) {
                                        if(entry.contains("A") && entry.contains(value)) { // checks for A type
                                                returnip = entry.split(", ")[1];           // extracts ipaddress
                                                if(!returnRecords.contains(entry) {
                                                  returnRecords.add(entry);
                                                }
                                        }
                                }
                        }
                }

                // grab records
                returnRecords.add(returnip);

                sendpacket = returnRecords.getBytes();

                InetAddress IPAddress = InetAddress.getByName(returnip);
                // currently sending packet back to client, since ip are the same
                // should target extracted ipaddress, which is authoritative DNS for hiscinema
                DatagramPacket sendPacket =
                        new DatagramPacket(sendData, sendData.length, IPAddress, port);
                System.out.print("Datagram sent to: " + IPAddress + " at port " + port);
                connectionSocket.send(sendPacket);

                // receive reply from hiscinemadns, a record type R in the form of a string

                // send query to hercdndns

                // receive reply from hercdndns, which is ip address of content server, type a

        }
}

public static String parseQuery(String message){
        String[] tokens = message.split("/");
        message = tokens[2].toString();
        // still have to split the video.hiscinema.com to just hiscinema.com
        return message;
}
