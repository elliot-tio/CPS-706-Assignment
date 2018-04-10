import java.io.*;
import java.net.*;

public class HisCinemaServer
{
static int PORT = 40290;
private Socket connectionSocket;

public static void main(String[] args) throws Exception {
        new HisCinemaServer().runServer();
}
public void runServer() throws Exception {

        String path = "src/";
        String messageReceive;
        String fileName;

        ServerSocket welcomeSocket = new ServerSocket(PORT);
        //welcomeSocket.setSoTimeout(60000);

        System.out.println("HisCinemaServer up and ready...");

        while(true) {
                connectionSocket = welcomeSocket.accept();

                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream (connectionSocket.getOutputStream());
                messageReceive = inFromClient.readLine();
                System.out.println(messageReceive);

                if (messageReceive.split(" ").length == 3)
                {
                        String[] tokens = messageReceive.split(" ");
                        String getRequestFile = "src/" + tokens[1];
                        File fileCheck = new File(getRequestFile);

                        if (!(tokens[0].equals("GET")))
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
                                outToClient.writeBytes("200 OK HTTP/1.1");
                                //outToClient.flush();
                                fileName = this.parseRequest(messageReceive);
                                System.out.println(fileName);
                                File file = new File(path + fileName);
                                sendFile(file);
                        }
                }
                else
                        outToClient.writeBytes("400 BAD REQUEST HTTP/1.1");
        }

}

public String parseRequest(String message){
        String[] tokens = message.split(" ");
        message = tokens[1].toString();
        return message;
}

public void sendFile(File file) throws Exception {
        FileInputStream readFile = new FileInputStream(file);
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

        byte[] buffer = new byte[4096];
        while (readFile.read(buffer) > 0) {
                outToClient.write(buffer);
        }
        outToClient.flush();
        readFile.close();
        outToClient.close();
        System.out.println("File sent successfully to: " + connectionSocket.getInetAddress().getHostName());
}

}
