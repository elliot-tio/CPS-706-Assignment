import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.*;

public class LocalDns {
private static final int PORT_CLIENT = 40290;
private static final int PORT_HIS_AUTH = 40293;
private static final int PORT_HER_AUTH = 40294;
private static final int PORT_LOCAL_DNS = 40295;
public static String parsedRecord = "";

public static void main(String args[]) throws Exception
{


        DatagramSocket connectionSocket = new DatagramSocket(PORT_LOCAL_DNS);
        //connectionSocket.setSoTimeout(60000);
        System.out.println("Local DNS up and ready...");

        while(true) {

                byte[] receiveData = new byte[1024];
                byte[] sendData = new byte[1024];
                // accept request from client (string video.hiscinema.com)
                // need to be prepared to accept requests in the form of records too
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                connectionSocket.receive(packet);
                String fullQuery = new String(packet.getData());

                String parsedQuery = "";
                String queryName = "";
                String queryType = "";

                // extracts "hiscinema.com" from query, full form:
                // "http://video.hiscinema.com/F1"
                // also extracts "herCDN.com", full form:
                // (video.hiscinema.com,herCDN.com,R)
                parsedQuery = parseQuery(fullQuery);

                // InetAddress queryIP = packet.getAddress();
                // int queryPort = packet.getPort();

                // obtain records from file
                String filePath = "src/LocalDnsFiles/localrecords.txt";
                File file = new File(filePath);
                String returnip = "";

                if(parsedQuery.equalsIgnoreCase("hiscinema.com")) {
                        returnip = extract(parsedQuery, file);
                        queryName = parseName(parsedRecord);
                        queryType = parseType(parsedRecord);
                        System.out.print("Name: " + queryName + "\n");
                        System.out.print("Type: " + queryType + "\n");
                } else if (parsedQuery.equalsIgnoreCase("hercdn.com")) {
                        returnip = extract(parsedQuery, file);
                        queryName = parseName(fullQuery);
                        queryType = parseType(fullQuery);
                        System.out.print("Name: " + queryName + "\n");
                        System.out.print("Type: " + queryType + "\n");
                } else {
                        returnip = parsedQuery;
                        queryName = parseName(fullQuery);
                        queryType = parseType(fullQuery);
                        System.out.print("Name: " + queryName + "\n");
                        System.out.print("Type: " + queryType + "\n");
                }

                // grab target ip address based on records
                InetAddress IPAddress = InetAddress.getByName(returnip);
                sendData = parsedQuery.getBytes();

                // currently swapping port targets based on query
                // should target extracted ipaddress, which is authoritative DNS for hiscinema
                // then targets authoritative DNS for herCDN
                // then targets back to clientip

                // received reply from hiscinemadns, a record type R in the form of a string
                if(parsedQuery.equalsIgnoreCase("hiscinema.com")) {
                        DatagramPacket sendPacket =
                                new DatagramPacket(sendData, sendData.length, IPAddress, PORT_HIS_AUTH);
                        System.out.print("Query passed to: " + IPAddress + " at port " + PORT_HIS_AUTH + "\n");
                        connectionSocket.send(sendPacket);
                }
                // send query to hercdndns
                else if (parsedQuery.equalsIgnoreCase("hercdn.com")) {
                        DatagramPacket sendPacket =
                                new DatagramPacket(sendData, sendData.length, IPAddress, PORT_HER_AUTH);
                        System.out.print("Query passed to: " + IPAddress + " at port " + PORT_HER_AUTH + "\n");
                        connectionSocket.send(sendPacket);
                }
                // receive reply from hercdndns, which is ip address of content server, therefore send back to client
                else {
                        DatagramPacket sendPacket =
                                new DatagramPacket(sendData, sendData.length, IPAddress, PORT_CLIENT);
                        System.out.print("Reply to: " + IPAddress + " at port " + PORT_CLIENT + "\n");
                        connectionSocket.send(sendPacket);
                }
        }
}

public static String parseQuery(String message){
        // parases query if it has /
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

public static String parseType(String record) {
        String[] tokens = record.split(",");
        String type = tokens[2].toString().trim().replace(")", "");
        return type;
}

public static String parseName(String record) {
        String[] tokens = record.split(",");
        String name = tokens[0].toString().trim().replace("(", "");
        return name;
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
                        parsedRecord = line;
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
