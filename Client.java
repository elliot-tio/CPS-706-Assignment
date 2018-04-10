import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Client
{
public static final int PORT = 40290;
public static final String IPADDRESS = "localhost";     //99.246.236.65

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
                }

                /* else if (Integer.parseInt(clientChoice) > links.size())
                   {
                    System.out.println("Pick again");
                    clientChoice = brClientFileChoice.readLine();
                   } */

                else
                {
                        for(int i = 0; i < links.size(); i++)
                        {
                                if (links.get(i).contains(clientChoice))
                                {
                                        String option = links.get(Integer.parseInt(clientChoice)).trim();
                                        DatagramSocket clientSocket = new DatagramSocket(40291);
                                        InetAddress IPAddress = InetAddress.getByName("localhost");

                                        byte[] sendData = new byte[1024];
                                        byte[] receiveData = new byte[1024];

                                        sendData = option.getBytes();

                                        DatagramPacket sendPacket =
                                                new DatagramPacket(sendData, sendData.length, IPAddress, 40300);

                                        clientSocket.send(sendPacket);
                                        System.out.println("Send success");

                                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                                        clientSocket.receive(receivePacket);

                                        String reply =
                                                new String(receivePacket.getData());

                                        System.out.println("FROM SERVER:" + reply);
                                        clientSocket.close();
                                        break;
                                }
                        }
                }
        } else {
                System.exit(0);
        }
}


public LinkedList<String> getIndexTCP() throws IOException
{
        InputStream inputStream;
        OutputStream outputStream;
        byte[] bytes;
        Socket clientSocket;
        PrintWriter outToServer;
        int count;
        File file = new File("index.html");
        LinkedList<String> linksFromIndex = new LinkedList<>();
        String serverHTTPMessage = "";

        //clientSocket = new Socket("localhost", PORT);
        clientSocket = new Socket(InetAddress.getByName(IPADDRESS), PORT);

        outToServer = new PrintWriter(clientSocket.getOutputStream(), true); //outputs to server

        outToServer.println("GET index.html HTTP/1.1\r\n\n");
        outToServer.flush();

        bytes = new byte[1024 * 2];
        inputStream = clientSocket.getInputStream();
        outputStream = new FileOutputStream(file);
        while ((count = inputStream.read(bytes)) >= 0) {
                outputStream.write(bytes, 0, count);
                System.out.println("count " + count);
        }

        serverHTTPMessage = htmlParserHTTPMessage(file);
        System.out.println("message: " + serverHTTPMessage);
        linksFromIndex = htmlParser(file);
        return linksFromIndex;
}


public LinkedList<String> htmlParser (File htmlFile) throws IOException
{
        LinkedList<String> links = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(htmlFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                        if(!line.contains("<") || line.contains("HTTP/1.1")) {
                                links.add(line);
                        }

                }
        }
        System.out.println("list " + links);
        return links;
}
public String htmlParserHTTPMessage (File htmlFile) throws IOException
{
        String message = "";
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
                        else if (line.contains("404 File Not Found HTTP/1.1"))
                        {
                                message = "404 File Not Found HTTP/1.1";
                                break;
                        }
                }
        }
        return message;
}
}
