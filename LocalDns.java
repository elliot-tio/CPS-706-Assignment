import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.*;

public class LocalDns {
private static final int PORT_HIS_AUTH = 40293;
private static final int PORT_HER_AUTH = 40294;
private static final int PORT_LOCAL_DNS = 40295;
private static String query;

public static void main(String args[]) throws Exception
{


        DatagramSocket connectionSocket = new DatagramSocket(PORT_LOCAL_DNS);
        // connectionSocket.setSoTimeout(60000);
        System.out.println("Local DNS up and ready...");
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        while(true) {

                // accept request from client (string video.hiscinema.com)
                // need to be prepared to accept requests in the form of records too
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                connectionSocket.receive(packet);
                query = new String(packet.getData());
                System.out.println("RECEIVED: " + query.trim() + "\n");

                InetAddress clientIP = packet.getAddress();
                int client_port = packet.getPort();

                // change this to get record based on query
                // find ip address based on client request, format: video.hiscinema.com/F1
                String parsedQuery = parseQuery(query);     // extracts "video.hiscinema.com" from query, full form
                                                            // "http://video.hiscinema.com/F1"
                System.out.print("Parsed Query: " + parsedQuery + "\n");

                String returnip = "";
                String value = "";

                // obtain records from file
                String filePath = "src/LocalDnsFiles/localrecords.txt";
                File file = new File(filePath);
                Scanner scan = new Scanner(file);

                // extract value from records
                while(scan.hasNext()) {
                        String line = scan.nextLine().toString();
                        if(!line.contains("A") && line.contains(parsedQuery)) {
                                value = line.split(",")[1].trim();
                        }
                }
                scan.close();

                scan = new Scanner(file);
                // extract ip from records
                if (!value.isEmpty()) {
                        while(scan.hasNext()) {
                                String line = scan.nextLine().toString();
                                if(line.contains("A") && line.contains(value)) {
                                        returnip = line.split(",")[1].trim();
                                        //sendData(line);
                                }
                        }
                }
                scan.close();

                // grab records
                sendData = parsedQuery.getBytes();

                // grab target ip address based on records
                InetAddress IPAddress = InetAddress.getByName(returnip);

                // currently swapping port targets based on query
                // should target extracted ipaddress, which is authoritative DNS for hiscinema
                // then targets authoritative DNS for herCDN
                // then targets back to clientip


                DatagramPacket sendPacket =
                        new DatagramPacket(sendData, sendData.length, IPAddress, PORT_HIS_AUTH);
                System.out.print("Datagram sent to: " + IPAddress + " at port " + PORT_HIS_AUTH + "\n");
                connectionSocket.send(sendPacket);

                // received reply from hiscinemadns, a record type R in the form of a string

                // send query to hercdndns

                // receive reply from hercdndns, which is ip address of content server, type a

        }
}

public static String parseQuery(String message){
        if(message.contains("/")) {
                String[] tokens = message.split("/");
                query = tokens[2].toString();
                // still have to split the video.hiscinema.com to just hiscinema.com
                String[] new_tokens = query.split("\\.");
                String result = new_tokens[1].toString() + "." + new_tokens[2].toString();
                return result;
        } else {
                String[] tokens = message.split(",");
                query = tokens[1].toString();
                return query;
        }
}
}
