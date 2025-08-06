import java.net.*;
import java.io.*;
import java.nio.file.*;

// Main file server class
public class FileServer {
  // Properties
  ServerSocket serverSocket;
  int serverPort;
  final String SHARED_DIR = "../shared"; // Directory to serve files from

  // Constructor
  public FileServer(int serverPort) {
    this.serverPort = serverPort;

    // Create the socket
    try {
      this.serverSocket = new ServerSocket(this.serverPort);
    }
    // Catch socket failures
    catch (IOException e) {
      System.out.println("[-] Error creating server socket: " + e);
      System.exit(1);
    }
  } // End constructor

  // Accept and return a client socket
  private Socket acceptConnection() {
    try {
      // Accept client socket object
      Socket clientSocket = this.serverSocket.accept();
      return clientSocket;
    }
    // Failed to accept client
    catch (IOException e) {
      System.out.println("[-] Error accepting client: " + e);
      return null;
    }
  }

  // Handle client request and send file
  private void handleRequest(ClientConnection client) {
    try {
      // Read request line
      String requestLine = client.in.readLine();

      // Null or empty request
      if (requestLine == null || requestLine.isEmpty()) {
        return;
      }

      System.out.println("[+] Received request\n" + requestLine);

      // Parse HTTP request
      String[] tokens = requestLine.split(" ");
      if (tokens.length < 3) {
        sendErrorResponse(client, "400 Bad Request", "Malformed request line");
        return;
      }

      // Extract method and path
      String method = tokens[0];
      String path = tokens[1];

      // Only allow GET
      if (!method.equals("GET")) {
        sendErrorResponse(client, "405 Method Not Allowed", "Only GET is supported.");
        return;
      }

      // Check for "/get?file=filename"
      if (!path.startsWith("/get?file=")) {
        sendErrorResponse(client, "400 Bad Request", "Missing or invalid file parameter.");
        return;
      }

      // Extract filename
      String filename = path.substring("/get?file=".length());
      filename = URLDecoder.decode(filename, "UTF-8");

      // Build absolute path to file
      Path baseDir = Paths.get(SHARED_DIR).toAbsolutePath().normalize();
      Path filePath = baseDir.resolve(filename).normalize();

      // Prevent directory traversal
      if (!filePath.startsWith(baseDir)) {
        sendErrorResponse(client, "403 Forbidden", "Access denied.");
        return;
      }

      // Check if file exists
      if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
        sendErrorResponse(client, "404 Not Found", "File not found.");
        return;
      }

      // Read file contents
      byte[] fileBytes = Files.readAllBytes(filePath);
      String contentType = Files.probeContentType(filePath);
      if (contentType == null) {
        contentType = "application/octet-stream";
      }

      // Send success response with file
      sendFileResponse(client, "200 OK", contentType, fileBytes);
    }
    // Error during handling
    catch (IOException e) {
      System.out.println("[-] Error handling request: " + e);
    }
  }

  // Send HTTP error response
  private void sendErrorResponse(ClientConnection client, String status, String message) {
    String response = "HTTP/1.1 " + status + "\r\n" +
                      "Content-Type: text/plain\r\n" +
                      "Content-Length: " + message.length() + "\r\n" +
                      "Access-Control-Allow-Origin: *\r\n" +
                      "\r\n" +
                      message;
    client.out.print(response);
    client.out.flush();
  }

  // Send file response
  private void sendFileResponse(ClientConnection client, String status, String contentType, byte[] fileBytes) throws IOException {
    String header = "HTTP/1.1 " + status + "\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + fileBytes.length + "\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "\r\n";

    client.out.print(header);
    client.out.flush();
    client.clientSocket.getOutputStream().write(fileBytes);
    client.clientSocket.getOutputStream().flush();
  }

  // Main loop function
  public void mainLoop() {
    System.out.println("[*] Waiting for connection on port " + this.serverPort);

    while (true) {
      // Accept client connection
      Socket clientSocket = this.acceptConnection();
      if (clientSocket == null) return;

      System.out.println("[+] New connection");

      // Setup client connection object
      ClientConnection clientConnection = new ClientConnection(clientSocket);

      // Handle request and send response
      this.handleRequest(clientConnection);

      // Close streams and socket
      try {
        clientConnection.in.close();
        clientConnection.out.close();
        clientConnection.clientSocket.close();
      } catch (IOException e) {
        System.out.println("[-] Error closing connection: " + e);
      }
    }
  } // End main loop

  // Entry point
  public static void main(String[] args) {
    // Bad usage error
    if (args.length < 1) {
      System.out.println("Usage: java FileServer <Port>");
      return;
    }

    // Extract port
    int serverPort = Integer.parseInt(args[0]);

    // Create server instance
    FileServer fileServer = new FileServer(serverPort);
    System.out.println("[*] File server starting");

    // Run main loop
    fileServer.mainLoop();
  } // End main

  // Class to contain data for client connection
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
      // Failed to setup data streams for client socket
      catch (IOException e) {
        System.out.println("[-] Error opening data streams with client");
      }
    } // End constructor
  } // End ClientConnection
} // End FileServer
