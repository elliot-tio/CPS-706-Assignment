import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;

import com.sun.net.httpserver.*;

public class HerCDNcom {

public static final int PORT = 8080;
public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // URL of http://localhost:8080/Fi will trigger the handlers and produce the contents of Fi
        server.createContext("/F1", new GetHandler());
        server.createContext("/F2", new GetHandler());
        server.createContext("/F3", new GetHandler());
        server.createContext("/F4", new GetHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server is listening on port: " + PORT);
}
}

// GET request handler, returns file based on choice from www.hiscinema.com
class GetHandler implements HttpHandler {

// Helper method to read files
static String readFile(String path, Charset encoding)
throws IOException
{
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
}

public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {
                String uri = exchange.getRequestURI().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz");

                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Last-Modified", sdf.format(new File("contents/" + uri + ".txt").lastModified()));
                responseHeaders.set("Content-Type", "text/html");


                String response = readFile("contents/" + uri + ".txt", StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.getBytes().length);

                Set<String> keySet = responseHeaders.keySet();
                Iterator<String> iter = keySet.iterator();
                while (iter.hasNext()) {
                        String key = iter.next();
                        List values = responseHeaders.get(key);
                        String s = key + ": " + values.toString();
                        System.out.println(s);
                }

                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(response.getBytes());
                responseBody.close();
        }
}
}
