import java.net.*;
import java.io.*;
import java.nio.file.*;

// Main class
public class Server {
  // Properties
  // Server data
  int serverPort;
  ServerSocket serverSocket;

  // Route file data
  String[] routingData;
  String routeFilename;

  // Constructor
  public Server(int serverPort, String routeFilename) {
    this.serverPort = serverPort;
    this.routeFilename = routeFilename;

    // Create server socket
    try {
      this.serverSocket = new ServerSocket(this.serverPort);
    }
    // Socket creation failed
    catch (IOException e) {
      System.out.println("[-] Failed to create server socket: " + e);
      System.exit(1);
    }

    // Read routing file into memory
    try {
      String routeFileContents = Files.readString(Paths.get(this.routeFilename));
      this.routingData = routeFileContents.split("\n");
    }
    // Failed to read route file
    catch (IOException e) {
      System.out.println("[-] Error reading route file: " + e);
      System.exit(1);
    }
  } // End constructor

  // Accept and return a client socket
  private Socket acceptConnection() {
    try {
      Socket clientSocket = this.serverSocket.accept();
      return clientSocket;
    }
    // Failed to accept client
    catch (IOException e) {
      System.out.println("[-] Failed to accept client connection: " + e);
      return null;
    }
  }

  // Determine content type based on file extension
  private String getContentType(String filename) {
    if (filename.endsWith(".html") || filename.endsWith(".htm")) return "text/html";
    if (filename.endsWith(".txt")) return "text/plain";
    if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
    if (filename.endsWith(".png")) return "image/png";
    if (filename.endsWith(".css")) return "text/css";
    if (filename.endsWith(".js")) return "application/javascript";
    return "application/octet-stream";
  }

  // Handle request and send raw file as response
  private void handleRequest(ClientConnection client) {
    try {
      // Read request line
      String requestLine = client.in.readLine();

      // Invalid or empty request
      if (requestLine == null || requestLine.isEmpty()) return;

      System.out.println("[+] Received request\n" + requestLine);

      // Split into tokens
      String[] tokens = requestLine.split(" ");
      if (tokens.length < 3) {
        sendErrorResponse(client, "400 Bad Request", "Malformed request");
        return;
      }

      // Get requested path
      String method = tokens[0];
      String path = tokens[1];

      // Only support GET
      if (!method.equals("GET")) {
        sendErrorResponse(client, "405 Method Not Allowed", "Only GET is supported");
        return;
      }

      // Match against route file
      for (String route : this.routingData) {
        String[] parts = route.strip().split(":", 2);

        // Skip malformed lines
        if (parts.length != 2) continue;

        String routePath = parts[0];
        String localPath = parts[1];

        // Match request to route
        if (routePath.equals(path)) {
          Path filePath = Paths.get(localPath).normalize();

          // Check if file exists
          if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendErrorResponse(client, "404 Not Found", "File not found");
            return;
          }

          // Read file
          byte[] fileBytes = Files.readAllBytes(filePath);
          String contentType = getContentType(localPath);

          // Send response
          sendFileResponse(client, "200 OK", contentType, fileBytes);
          return;
        }
      }

      // No matching route
      sendErrorResponse(client, "404 Not Found", "No matching route");
    }
    // Handle read error
    catch (IOException e) {
      System.out.println("[-] Error handling request: " + e);
    }
  }

  // Send raw file response with headers
  private void sendFileResponse(ClientConnection client, String status, String contentType, byte[] content) throws IOException {
    String header = "HTTP/1.1 " + status + "\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + content.length + "\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "\r\n";
    client.out.print(header);
    client.out.flush();
    client.clientSocket.getOutputStream().write(content);
    client.clientSocket.getOutputStream().flush();

    System.out.println("[+] Sent file response\n" + header);
  }

  // Send error with plain text message
  private void sendErrorResponse(ClientConnection client, String status, String message) {
    String response = "HTTP/1.1 " + status + "\r\n" +
                      "Content-Type: text/plain\r\n" +
                      "Content-Length: " + message.length() + "\r\n" +
                      "Access-Control-Allow-Origin: *\r\n" +
                      "\r\n" +
                      message;
    client.out.print(response);
    client.out.flush();

    System.out.println("[+] Sent file response\n" + response);
  }

  // Main server loop
  public void mainLoop() {
    System.out.println("[*] Waiting for connection on port " + this.serverPort);

    while (true) {
      Socket clientSocket = this.acceptConnection();
      if (clientSocket == null) continue;

      System.out.println("[+] New connection");

      // Setup client connection object
      ClientConnection client = new ClientConnection(clientSocket);

      // Handle request
      this.handleRequest(client);

      // Close connection
      try {
        client.in.close();
        client.out.close();
        client.clientSocket.close();
      }
      catch (IOException e) {
        System.out.println("[-] Failed to close client connection: " + e);
      }
    }
  } // End main loop

  // Entry point
  public static void main(String[] args) {
    // Usage check
    if (args.length < 2) {
      System.out.println("Usage: java Server <Port> <Route file>");
      System.exit(1);
    }

    // Extract args
    int serverPort = Integer.parseInt(args[0]);
    String routeFilename = args[1];

    // Create server instance
    Server server = new Server(serverPort, routeFilename);
    System.out.println("[*] Starting server");

    // Start server loop
    server.mainLoop();
  } // End main

  // Class to contain data for a client connection
  class ClientConnection {
    // Properties
    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;

    // Constructor
    public ClientConnection(Socket clientSocket) {
      try {
        this.clientSocket = clientSocket;
        this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
      }
      // Failed to setup data streams
      catch (IOException e) {
        System.out.println("[-] Error opening data streams with client socket: " + e);
      }
    }
  } // End ClientConnection
} // End Server
