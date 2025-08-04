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
  String[] routeFiledata;
  String routeFilename;

  // Constructor
  public Server(int serverPort, String routeFilename) {
    // Setup server socket
    try {
      this.serverPort = serverPort;
      serverSocket = new ServerSocket(this.serverPort);
    }
    // Catch socket creation failures
    catch (IOException e) {
      System.out.println("[-] Failed to create server socket:" + e);
      System.exit(1);
    }

    // Setup route file and read data from file
    this.routeFilename = routeFilename;

    try {
      // String to store the contents of route file as a single raw string
      String routeFileContents;
      Path routePath = Paths.get(this.routeFilename);

      // Read into string
      routeFileContents = Files.readString(routePath);

      // Split the route file data into lines
      this.routingData = routeFileContents.split("\n");
    }
    // Error while reading route file
    catch (IOException e) {
      System.out.println("[-] Error reading route file:" + e);
      System.exit(1);
    }
  } // Constructor

  // Accept and return a client connection object
  private Socket acceptConnection() {
    try {
      Socket clientSocket = this.serverSocket.accept();
      return clientSocket;
    }
    // Failed to accept client connection
    catch (IOException e) {
      System.out.println("[-] Failed to accept client connection: " + e);
      return null;
    }
  }

  private String getContentType(String filename) {
    return null;
  }

  // Generate a string response to send back over socket
  private String  generateResponse(String request) {
    // Convert string to tokens
    String[] tokens = request.split(" ");
    // Verify tokens
    if (tokens.length < 3) {
      return "HTTP/1.1 400 Bad Request\r\n\r\nMalformed HTTP request line";
    }

    // Process the requested path and get local file
    String requestedPath = tokens[1];

    // Attempt to convert the requested path to a local path using the routing data
    for (String route : this.routingData) {
      // Get the requested path with the local path
      String[] parts = route.strip().split(":", 2);

      String requestedPath = parts[0];
      String localPath = parts[1];

      // Remote requested file matches local file?
      if (routePath.equals(requetedPath)) {
        try {
          String contentType = getContentType(filePath);
          return;
        }
      }
    }
  }

  // Main loop function for getting connections, and sending response
  public void mainLoop() {
    try {
      System.out.println("[*] Waiting for connection on port " + this.serverPort);

      // Accept a client connection
      Socket clientSocket = this.acceptConnection();
      System.out.println("[+] New connection");

      // Setup client connection object
      ClientConnection clientConnection = new ClientConnection(clientSocket);

      // Receive request
      String request = clientConnection.in.readLine();
      System.out.println("[+] Received request\n" + request);

      // Generate a response
      String response = generateResponse(request);
      System.out.println("[+] Sending response\n" + response);

      // Send response to client
      clientConnection.out.println(response);

      // Close connection
      clientConnection.in.close();
      clientConnection.out.close();
      clientConnection.clientSocket.close();
      return;
    }
    // Catch socket errors
    catch (IOException e) {
      System.out.println("[-] Server error: " + e);
      return;
    }
  } // Main loop


  // Entry
  public static void main(String[] args) {
    // Usage failure
    if (args.length < 2) {
      System.out.println("Usage: java Server <Port> <Route file>");
      System.exit(1);
    }

    // Extract arguments
    int serverPort = Integer.parseInt(args[0]);
    String routeFilename = args[1];

    // Create main instance of server
    Server server = new Server(serverPort);
    System.out.println("[*] Starting server");

    // Start main server loop
    while (true) {
      server.mainLoop();
    }
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
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
      }
      // Catch data stream setup failures
      catch (IOException e) {
        System.out.println("[-] Error opening data streams with client socket: " + e);
      }
    }
  } // End ClientConnection
} // End Server
