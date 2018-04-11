import java.io.*;
import java.net.*;

public class HerCDNServer
{
static int PORT = 40292;
private Socket connectionSocket;

public static void main(String[] args) throws Exception
{
        new HerCDNServer().runServer();
}

@SuppressWarnings("Duplicates")
public void runServer() throws Exception
{

        String path = "src/HerCDNFiles/";
        String messageReceive;
        String fileName;

        ServerSocket welcomeSocket = new ServerSocket(PORT);
        //welcomeSocket.setSoTimeout(60000);

        System.out.println("HerCDNServer up and ready...");

        while (true)
        {
                connectionSocket = welcomeSocket.accept();

                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                messageReceive = inFromClient.readLine();


                if (messageReceive.split(" ").length == 3)
                {
                        String[] tokens = messageReceive.split(" ");
                        String getRequestFile = path + Integer.parseInt(tokens[1].replaceAll("[\\D]", "")) + ".txt";
                        System.out.println("getRequestFile: " + getRequestFile);
                        File fileCheck = new File(getRequestFile);

                        if (!tokens[0].equals("GET"))
                        {
                                outToClient.writeBytes("400 BAD REQUEST HTTP/1.1");
                                outToClient.close();
                        } else if (!(tokens[2].equals("HTTP/1.1")))
                        {
                                outToClient.writeBytes("505 Version Not Supported HTTP/1.1");
                                outToClient.close();
                        } else if (!fileCheck.exists())
                        {
                                outToClient.writeBytes("404 File Not Found HTTP/1.1");
                                outToClient.close();
                        } else
                        {
                                //outToClient.writeBytes("200 OK HTTP/1.1"); //UNCOMMENT
                                //outToClient.flush(); //UNCOMMENT

                                File file = new File(getRequestFile);
                                sendFile(file);
                                outToClient.close();
                        }
                }
                else
                {
                        outToClient.writeBytes("400 BAD REQUEST HTTP/1.1");
                        outToClient.close();
                }
        }

}

public String parseRequest(String message)
{
        String[] tokens = message.split(" ");
        message = tokens[1].toString();
        return message;
}

@SuppressWarnings("Duplicates")
public void sendFile(File file) throws Exception
{
        FileInputStream readFile = new FileInputStream(file);
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

        byte[] buffer = new byte[16 * 1024];
        int len = 0;
        while ((len = readFile.read(buffer)) != -1) {
                outToClient.write(buffer, 0, len);
                System.out.println("LEN: " + len);
        }

        /*
           byte[] buffer = new byte[1024 * 16];
           while (readFile.read(buffer) > 0)
           {
            outToClient.write(buffer);
           }*/

        outToClient.flush();
        readFile.close();
        outToClient.close();
        System.out.println("File sent successfully to: " + connectionSocket.getInetAddress().getHostName());
}
}
