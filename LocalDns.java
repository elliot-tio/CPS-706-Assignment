import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.*;

public class LocalDns {
private static final int PORT_CLIENT = 40290;
private static final int PORT_HIS_AUTH = 40293;
private static final int PORT_HER_AUTH = 40294;
private static final int PORT_LOCAL_DNS = 40295;

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
                String fullQuery = new String(packet.getData());
                System.out.println("RECEIVED: " + fullQuery.trim() + "\n");

                InetAddress clientIP = packet.getAddress();
                int client_port = packet.getPort();

                // change this to get record based on query
                // find ip address based on client request, format: video.hiscinema.com/F1
                String parsedQuery = parseQuery(fullQuery);     // extracts "video.hiscinema.com" from query, full form
                // "http://video.hiscinema.com/F1"
                System.out.print("Parsed Query: " + parsedQuery + "\n");

                // obtain records from file
                String filePath = "src/LocalDnsFiles/localrecords.txt";
                File file = new File(filePath);
                String returnip = "";

                if(parsedQuery.equalsIgnoreCase("hiscinema.com") || parsedQuery.equalsIgnoreCase("hercdn.com")) {
                        returnip = extract(parsedQuery, file);
                        sendData = parsedQuery.getBytes();
                } else {
                        returnip = fullQuery.trim();
                        sendData = fullQuery.getBytes();
                }

                // grab target ip address based on records
                InetAddress IPAddress = InetAddress.getByName(returnip);

                // currently swapping port targets based on query
                // should target extracted ipaddress, which is authoritative DNS for hiscinema
                // then targets authoritative DNS for herCDN
                // then targets back to clientip

                // received reply from hiscinemadns, a record type R in the form of a string
                if(parsedQuery.equalsIgnoreCase("hiscinema.com")) {
                        DatagramPacket sendPacket =
                                new DatagramPacket(sendData, sendData.length, IPAddress, PORT_HIS_AUTH);
                        System.out.print("Datagram sent to: " + IPAddress + " at port " + PORT_HIS_AUTH + "\n");
                        connectionSocket.send(sendPacket);
                }
                // send query to hercdndns
                else if (parsedQuery.equalsIgnoreCase("hercdn.com")) {
                        DatagramPacket sendPacket =
                                new DatagramPacket(sendData, sendData.length, IPAddress, PORT_HER_AUTH);
                        System.out.print("Datagram sent to: " + IPAddress + " at port " + PORT_HER_AUTH + "\n");
                        connectionSocket.send(sendPacket);
                }
                // receive reply from hercdndns, which is ip address of content server, type a
                else {
                        DatagramPacket sendPacket =
                                new DatagramPacket(sendData, sendData.length, InetAddress.getByName(parsedQuery), PORT_CLIENT);
                        System.out.print("Datagram sent to: " + parsedQuery + " at port " + PORT_CLIENT + "\n");
                        connectionSocket.send(sendPacket);
                }
        }
}

public static String parseQuery(String message){
        if(message.contains("/")) {
                String[] tokens = message.split("/");
                String query = tokens[2].toString();
                // still have to split the video.hiscinema.com to just hiscinema.com
                String[] new_tokens = query.split("\\.");
                String result = new_tokens[1].toString() + "." + new_tokens[2].toString();
                return result;
        } else {
                String[] tokens = message.split(",");
                String query = tokens[1].toString();
                return query;
        }
}

public static String extract(String parsedQuery, File file) throws Exception {
        Scanner scan = new Scanner(file);
        String returnip = "";
        String value = "";
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
                        }
                }
        }
        scan.close();

        return returnip;
}


}
