import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Client
{
public static final int PORT_HOME = 40290;
public static final String IPADDRESS_HOME = "localhost";
public static final int PORT_HIS = 40291;
public static final String IPADDRESS_HIS = "localhost";     //99.246.236.65
public static final int PORT_HER = 40292;
public static final String IPADDRESS_HER = "localhost";     //99.246.236.65
public static final int PORT_HIS_AUTH = 40293;
public static final String IPADDRESS_HIS_AUTH = "localhost";
public static final int PORT_HER_AUTH = 40294;
public static final String IPADDRESS_HER_AUTH = "localhost";
public static final int PORT_LOCAL_DNS = 40295;
public static final String IPADDRESS_LOCAL_DNS = "localhost";

public static void main(String[] args) throws Exception {
        new Client().runClient();
}


public void runClient() throws Exception {

        LinkedList<String> links = getIndexTCP();

        if (!links.isEmpty())
        {
                BufferedReader brClientFileChoice = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Pick a file from 1-4");
                String clientChoice = brClientFileChoice.readLine();

                if (clientChoice.equalsIgnoreCase("exit"))
                {
                        System.out.println("Exiting program...");
                        System.exit(0);
                } else
                {
                        for (int i = 0; i < links.size(); i++)
                        {
                                if (links.get(i).contains(clientChoice))
                                {
                                        String option = links.get(Integer.parseInt(clientChoice)).trim();
                                        DatagramSocket clientSocket = new DatagramSocket(PORT_HOME);

                                        byte[] sendData = new byte[1024];
                                        byte[] receiveData = new byte[1024];

                                        sendData = option.getBytes();

                                        DatagramPacket sendPacket =
                                                new DatagramPacket(sendData, sendData.length, InetAddress.getByName(IPADDRESS_LOCAL_DNS), PORT_LOCAL_DNS);

                                        clientSocket.send(sendPacket);

                                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                                        clientSocket.receive(receivePacket);

                                        String IPADDRESS_HER =
                                                new String(receivePacket.getData());

                                        System.out.println("FROM SERVER:" + IPADDRESS_HER);
                                        clientSocket.close();

                                        // getCDNFileTCP(links.get(i));
                                        break;
                                }
                        }
                }
        }
        else
                System.exit(0);
}


public LinkedList<String> getIndexTCP() throws IOException     //HisCinemaServer
{
        InputStream inputStream;
        OutputStream outputStream;
        byte[] bytes;
        Socket clientSocket;
        PrintWriter outToServer;
        int count;
        File file = new File("src/ClientFiles/HisCinemaIndex.html");
        LinkedList<String> linksFromIndex;
        String serverHTTPMessage;

        //clientSocket = new Socket("localhost", PORT);
        clientSocket = new Socket(InetAddress.getByName(IPADDRESS_HIS), PORT_HIS);

        outToServer = new PrintWriter(clientSocket.getOutputStream(), true); //outputs to server

        outToServer.println("GET index.html HTTP/1.1");
        outToServer.flush();

        bytes = new byte[1024 * 2];
        inputStream = clientSocket.getInputStream();
        outputStream = new FileOutputStream(file);
        while ((count = inputStream.read(bytes)) >= 0) {
                outputStream.write(bytes, 0, count);
        }

        serverHTTPMessage = htmlParserHTTPMessage(file);
        System.out.println("message from HisCinemaServer: " + serverHTTPMessage);
        linksFromIndex = htmlParser(file);
        return linksFromIndex;
}


public void getCDNFileTCP(String videoLink) throws IOException     //HerCDNServer
{
        InputStream inputStream;
        OutputStream outputStream;
        byte[] bytes;
        Socket clientSocket;
        PrintWriter outToServer;
        int count;
        File file = new File("src/ClientFiles/HerCDNIndex.html");
        String serverHTTPMessage;
        String CDNServerName = "www.herCDN.com/F";

        clientSocket = new Socket(InetAddress.getByName(IPADDRESS_HER), PORT_HER);
        outToServer = new PrintWriter(clientSocket.getOutputStream(), true); //outputs to server


        String outHTTPRequest = "GET " + CDNServerName + Integer.parseInt(videoLink.replaceAll("[\\D]", "")) + " HTTP/1.1";
        System.out.println("outHTTPRequest: " + outHTTPRequest);

        outToServer.println(outHTTPRequest);
        outToServer.flush();

        /*
           bytes = new byte[1024 * 16];
           while ((count = inputStream.read(bytes)) >= 0) {
            outputStream.write(bytes, 0, count);
           }
         */

        inputStream = clientSocket.getInputStream();
        outputStream = new FileOutputStream(file);
        long length = file.length();
        byte[] bytes1 = new byte[16 * 1024];
        int count1;
        while ((count1 = inputStream.read(bytes1)) != -1) {
                outputStream.write(bytes1, 0, count1);
        }

        serverHTTPMessage = htmlParserHTTPMessage(file, Integer.parseInt(videoLink.replaceAll("[\\D]", "")));
        System.out.println("message from HerCDNServer: " + serverHTTPMessage);
}


public LinkedList<String> htmlParser (File htmlFile) throws IOException
{
        LinkedList<String> links = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(htmlFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                        if(!(line.contains("<") || line.contains("HTTP/1.1")))
                                links.add(line);
                }
        }
        System.out.println("list " + links);
        return links;
}

@SuppressWarnings("Duplicates")
public String htmlParserHTTPMessage (File htmlFile) throws IOException
{
        String message = "";
        File file = new File("src/ClientFiles/HerCDNIndex.html");
        try (BufferedReader br = new BufferedReader(new FileReader(htmlFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                        if (line.contains("200 OK HTTP/1.1"))
                        {
                                message = "200 OK HTTP/1.1";
                                break;
                        }
                        else if (line.contains("505 Version Not Supported HTTP/1.1"))
                        {
                                message = "505 Version Not Supported HTTP/1.1";
                                break;
                        }
                        else if (line.contains("400 BAD REQUEST HTTP/1.1"))
                        {
                                message = "400 BAD REQUEST HTTP/1.1";
                                break;
                        }
                        else if (line.contains("404 File Not Found HTTP/1.1"))
                        {
                                message = "404 File Not Found HTTP/1.1";
                                break;
                        }
                }
        }
        return message;
}

@SuppressWarnings("Duplicates")
public String htmlParserHTTPMessage (File htmlFile, int fileNum) throws IOException
{
        String message = "";
        String filePath = "src/ClientFiles/" + fileNum + ".txt";

        try (BufferedReader br = new BufferedReader(new FileReader(htmlFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                        if (line.contains("200 OK HTTP/1.1"))
                        {
                                message = "200 OK HTTP/1.1";
                                break;
                        }
                        else if (line.contains("505 Version Not Supported HTTP/1.1"))
                        {
                                message = "505 Version Not Supported HTTP/1.1";
                                break;
                        }
                        else if (line.contains("400 BAD REQUEST HTTP/1.1"))
                        {
                                message = "400 BAD REQUEST HTTP/1.1";
                                break;
                        }
                        else if (line.contains("404 File Not Found HTTP/1.1"))
                        {
                                message = "404 File Not Found HTTP/1.1";
                                break;
                        }
                        else
                        {
                                BufferedReader inputStream = new BufferedReader(new FileReader (htmlFile));
                                File UIFile = new File(filePath);
                                // if File doesnt exists, then create it
                                if (!UIFile.exists()) {
                                        UIFile.createNewFile();
                                }
                                FileWriter filewriter = new FileWriter(UIFile.getAbsoluteFile());
                                BufferedWriter outputStream= new BufferedWriter(filewriter);
                                String count;
                                while ((count = inputStream.readLine()) != null) {
                                        outputStream.write(count);
                                }
                                outputStream.flush();
                                outputStream.close();
                                inputStream.close();
                        }
                }
        }
        return message;
}
}
