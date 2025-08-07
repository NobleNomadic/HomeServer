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
      System.out.println("[+] Client connected from: " + clientSocket.getInetAddress());
      return clientSocket;
    }
    // Failed to accept client
    catch (IOException e) {
      System.out.println("[-] Error accepting client: " + e);
      return null;
    }
  }

  // Handle client request and send file or accept upload
  private void handleRequest(ClientConnection client) {
    try {
      // Read request line manually
      String requestLine = readLine(client.rawIn);

      // Null or empty request
      if (requestLine == null || requestLine.isEmpty()) {
        return;
      }

      // Parse HTTP request line
      String[] tokens = requestLine.split(" ");
      if (tokens.length < 3) {
        sendErrorResponse(client, "400 Bad Request", "Malformed request line");
        return;
      }

      // Extract method and path
      String method = tokens[0];
      String path = tokens[1];

      // Only allow GET and POST
      if (!method.equals("GET") && !method.equals("POST")) {
        sendErrorResponse(client, "405 Method Not Allowed", "Only GET and POST are supported.");
        return;
      }

      // Handle GET
      if (method.equals("GET")) {
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

        // Send file
        sendFileResponse(client, "200 OK", contentType, fileBytes);
        System.out.println("[+] Served file: " + filename);
        return;
      }

      // Handle POST
      if (method.equals("POST")) {
        // Check for "/upload?file=filename"
        if (!path.startsWith("/upload?file=")) {
          sendErrorResponse(client, "400 Bad Request", "Missing or invalid file parameter.");
          return;
        }

        // Extract filename
        String filename = path.substring("/upload?file=".length());
        filename = URLDecoder.decode(filename, "UTF-8");

        // Build absolute path to destination file
        Path baseDir = Paths.get(SHARED_DIR).toAbsolutePath().normalize();
        Path filePath = baseDir.resolve(filename).normalize();

        // Prevent directory traversal
        if (!filePath.startsWith(baseDir)) {
          sendErrorResponse(client, "403 Forbidden", "Access denied.");
          return;
        }

        // Handle upload
        handleUpload(client, filePath, filename);
        return;
      }
    }
    // Error during handling
    catch (IOException e) {
      System.out.println("[-] Error handling request: " + e);
    }
  }

  // Handle file upload via POST request
  private void handleUpload(ClientConnection client, Path filePath, String filename) {
    try {
      // Read headers to find Content-Length
      int contentLength = -1;
      String line;
      while ((line = readLine(client.rawIn)) != null && !line.isEmpty()) {
        if (line.toLowerCase().startsWith("content-length:")) {
          contentLength = Integer.parseInt(line.split(":")[1].trim());
        }
      }

      // Missing or invalid length
      if (contentLength <= 0) {
        sendErrorResponse(client, "411 Length Required", "Content-Length is required for uploads.");
        return;
      }

      // Read file bytes
      byte[] buffer = new byte[contentLength];
      int bytesRead = 0;
      while (bytesRead < contentLength) {
        int read = client.rawIn.read(buffer, bytesRead, contentLength - bytesRead);
        if (read == -1) break;
        bytesRead += read;
      }

      // Write to disk
      Files.createDirectories(filePath.getParent());
      Files.write(filePath, buffer);

      // Send response
      String message = "Upload complete.";
      String response = "HTTP/1.1 201 Created\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + message.length() + "\r\n" +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "\r\n" +
                        message;
      client.out.print(response);
      client.out.flush();

      System.out.println("[+] Received upload: " + filename);
    }
    catch (IOException e) {
      System.out.println("[-] Upload failed: " + filename);
      sendErrorResponse(client, "500 Internal Server Error", "Failed to save uploaded file.");
    }
  }

  // Read a line from InputStream manually
  private String readLine(InputStream in) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int previous = -1, current;

    while ((current = in.read()) != -1) {
      if (previous == '\r' && current == '\n') {
        break;
      }
      if (previous != -1) buffer.write(previous);
      previous = current;
    }

    return buffer.toString("UTF-8");
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

      // Setup client connection object
      ClientConnection clientConnection = new ClientConnection(clientSocket);

      // Handle request and send response
      this.handleRequest(clientConnection);

      // Close streams and socket
      try {
        clientConnection.out.close();
        clientConnection.rawIn.close();
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

    // Run main loop
    fileServer.mainLoop();
  } // End main

  // Class to contain data for client connection
  class ClientConnection {
    // Properties
    Socket clientSocket;
    PrintWriter out;
    InputStream rawIn;

    // Constructor
    public ClientConnection(Socket clientSocket) {
      try {
        this.clientSocket = clientSocket;
        this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
        this.rawIn = this.clientSocket.getInputStream();
      }
      // Failed to setup data streams for client socket
      catch (IOException e) {
        System.out.println("[-] Error opening data streams with client");
      }
    } // End constructor
  } // End ClientConnection
} // End FileServer
