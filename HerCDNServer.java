import java.io.*;
import java.net.*;

public class HerCDNServer implements Runnable
{
Socket connectionSocket;

HerCDNServer(Socket socket)
{
        connectionSocket = socket;
}

public static void main(String args[]) throws Exception
{
        ServerSocket serverSocket = new ServerSocket(40292);
        System.out.println("HerCDNServer up and running...");
        while (true)
        {
                Socket socket = serverSocket.accept();
                System.out.println("Connected to " + socket.getInetAddress().getHostName());
                socket.setKeepAlive(true);
                new Thread(new HerCDNServer(socket)).start();
        }
}

public void run()
{
        try
        {
                String path = "src/HerCDNFiles/";
                String messageReceive;
                InputStream in;
                OutputStream out;
                OutputStreamWriter outWriter;

                while (true)
                {
                        //System.out.println("1");

                        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                        //System.out.println("2");
                        messageReceive = null;
                        for (String line = inFromClient.readLine(); line != null; line = inFromClient.readLine())
                        {
                                //System.out.println(line);
                                messageReceive = line;
                                if (line.length() > 1) break;
                        }
                        //System.out.println(inFromClient.readLine());
                        //System.out.println("3");
                        System.out.println("Request: " + messageReceive);
                        if (messageReceive.split(" ").length == 3)
                        {
                                String[] tokens = messageReceive.split(" ");
                                String getRequestFile = path + Integer.parseInt(tokens[1].replaceAll("[\\D]", "")) + ".png";
                                System.out.println("Requested file: " + getRequestFile);
                                File fileCheck = new File(getRequestFile);


                                if (!tokens[0].equals("GET"))
                                {
                                        OutputStreamWriter outToClient = new OutputStreamWriter(connectionSocket.getOutputStream());
                                        outToClient.write("400 BAD REQUEST HTTP/1.1");
                                        outToClient.close();
                                } else if (!(tokens[2].equals("HTTP/1.1")))
                                {
                                        OutputStreamWriter outToClient = new OutputStreamWriter(connectionSocket.getOutputStream());
                                        outToClient.write("505 Version Not Supported HTTP/1.1");
                                        outToClient.close();

                                } else if (!fileCheck.exists())
                                {
                                        OutputStreamWriter outToClient = new OutputStreamWriter(connectionSocket.getOutputStream());
                                        outToClient.write("404 File Not Found HTTP/1.1");
                                        outToClient.close();

                                } else
                                {
                                        outWriter = new OutputStreamWriter(connectionSocket.getOutputStream());
                                        outWriter.write("200 OK HTTP/1.1\r\n\n");
                                        outWriter.flush();
                                        in = new FileInputStream(path + Integer.parseInt(getRequestFile.replaceAll("[\\D]", "")) + ".png");
                                        //System.out.println("SOCKET: " + connectionSocket.toString());
                                        out = connectionSocket.getOutputStream();
                                        byte[] buffer = new byte[1024 * 8];
                                        int len = 0;
                                        while ((len = in.read(buffer)) != -1)
                                        {
                                                out.write(buffer, 0, len);
                                                //System.out.println("LEN: " + len);
                                        }
                                        Thread.sleep(5000);
                                        out.close();
                                        //in.close();
                                        //outWriter.close();
                                }
                        }
                }
        } catch (Exception e)
        {
                // e.printStackTrace();
        }
        Thread.currentThread().interrupt();
}
}
