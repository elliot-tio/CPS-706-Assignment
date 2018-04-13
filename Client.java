import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Client
{
public static final int PORT_HOME = 40290;
public static final String IPADDRESS_HOME = "localhost";
public static final int PORT_HIS = 40291;                   // TCP
public static final String IPADDRESS_HIS = "localhost";     //99.246.236.65
public static final int PORT_HER = 40292;                   // TCP
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

        // Client contacting dummy webserver message
        System.out.print("Contacting www.hiscinema.com...\n");
        System.out.print("Request: GET index.html HTTP/1.1\n");
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
                                        String option = links.get(Integer.parseInt(clientChoice) - 1).trim();
                                        DatagramSocket clientSocket = new DatagramSocket(PORT_HOME);

                                        byte[] sendData = new byte[1024];
                                        byte[] receiveData = new byte[1024];

                                        InetAddress IPADDRESS_LOCAL_DNS = clientSocket.getInetAddress();
                                        sendData = option.getBytes();

                                        DatagramPacket sendPacket =
                                                new DatagramPacket(sendData, sendData.length, InetAddress.getByName(IPADDRESS_LOCAL_DNS), PORT_LOCAL_DNS);

                                        clientSocket.send(sendPacket);

                                        // Control message saying client selected a URL and contacts local DNS
                                        String[] tokens = option.split("/");
                                        String query = tokens[2].toString();
                                        System.out.print("Query sent to Local DNS: " + query + "\n");

                                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                                        clientSocket.receive(receivePacket);

                                        String IPADDRESS_HER =
                                                new String(receivePacket.getData());

                                        // Control message for Local DNS reply with resolved IP address
                                        System.out.print("Authoritative answer:\n");
                                        System.out.println("Addresses: " + IPADDRESS_HER.trim() + "\n");
                                        clientSocket.close();

                                        getCDNFileTCP(links.get(i));
                                        break;
                                }
                        }
                }
        }
        else
                System.exit(0);
}


public LinkedList<String> getIndexTCP() throws IOException //HisCinemaServer
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

        bytes = new byte[1024 * 8];
        inputStream = clientSocket.getInputStream();
        outputStream = new FileOutputStream(file);
        while ((count = inputStream.read(bytes)) >= 0)
        {
                outputStream.write(bytes, 0, count);
        }

        serverHTTPMessage = htmlParserHTTPMessage(file);
        // Control message for dummy his cinema server reply with index
        System.out.println("Response: " + serverHTTPMessage);
        System.out.print("File: " + file.getName() + "\n\n");
        linksFromIndex = htmlParser(file);
        return linksFromIndex;
}


public void getCDNFileTCP(String videoLink) throws IOException //HerCDNServer
{
        Socket clientSocket;
        int count;
        int fileRequestedNum = Integer.parseInt(videoLink.replaceAll("[\\D]", ""));
        String CDNServerName = "www.herCDN.com/F";
        clientSocket = new Socket(InetAddress.getByName(IPADDRESS_HER), PORT_HER);
        String outHTTPRequest = "GET " + CDNServerName + fileRequestedNum + " HTTP/1.1\n";
        // Control message - client contacts content server
        System.out.print("Contacting www.herCDN.com...\n");
        System.out.println("Request: " + outHTTPRequest);


        //OutputStreamWriter out = new OutputStreamWriter(clientSocket.getOutputStream());
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        //System.out.println("1");
        output.write(outHTTPRequest);
        output.newLine(); //IMPORTANT
        output.flush();
        //System.out.println("2");
        //System.out.println("3");

        String receivedFile = "src/ClientFiles/received" + fileRequestedNum + ".png";

        InputStreamReader inputFromServer = new InputStreamReader(clientSocket.getInputStream());
        BufferedReader bfInputFromServer = new BufferedReader(inputFromServer);
        //System.out.println("4");
        String serverResponse = bfInputFromServer.readLine();
        System.out.println(serverResponse);

        File file = new File(receivedFile);

        byte[] bytes = new byte[16 * 1024];
        InputStream in = clientSocket.getInputStream();
        OutputStream out = new FileOutputStream(receivedFile);
        while ((count = in.read(bytes)) != -1)
        {
                out.write(bytes, 0, count);
        }
}


public LinkedList<String> htmlParser(File htmlFile) throws IOException
{
        LinkedList<String> links = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(htmlFile)))
        {
                String line;
                while ((line = br.readLine()) != null)
                {
                        line = line.trim();
                        if (!(line.contains("<") || line.contains("HTTP/1.1") || line.equals("")))
                                links.add(line);
                }
        }
        System.out.println("Options: " + links);
        return links;
}

public String htmlParserHTTPMessage(File htmlFile) throws IOException
{
        String message = "";
        File file = new File("src/ClientFiles/HerCDNIndex.html");
        try (BufferedReader br = new BufferedReader(new FileReader(htmlFile)))
        {
                String line;
                while ((line = br.readLine()) != null)
                {
                        if (line.contains("200 OK HTTP/1.1"))
                        {
                                message = "200 OK HTTP/1.1";
                                break;
                        } else if (line.contains("505 Version Not Supported HTTP/1.1"))
                        {
                                message = "505 Version Not Supported HTTP/1.1";
                                break;
                        } else if (line.contains("400 BAD REQUEST HTTP/1.1"))
                        {
                                message = "400 BAD REQUEST HTTP/1.1";
                                break;
                        } else if (line.contains("404 File Not Found HTTP/1.1"))
                        {
                                message = "404 File Not Found HTTP/1.1";
                                break;
                        }
                }
        }
        return message;
}
}
