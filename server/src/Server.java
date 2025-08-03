import java.net.*;
import java.io.*;

// Main class
public class Server {
  // Properties
  int serverPort;
  ServerSocket serverSocket;

  // Constructor
  public Server(int serverPort) {
    try {
      this.serverPort = serverPort;
      serverSocket = new ServerSocket(this.serverPort);
    }
    // Catch socket creation failures
    catch (IOException e) {
      System.out.println("[-] Failed to create server socket:" + e);
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

  // Generate a string response to send back over socket
  private String  generateResponse(String request) {
    // Convert string to tokens
    String[] tokens = request.split(" ");
    return null;
  }

  // Main loop function for getting connections, and sending response
  public void mainLoop() {
    try {
      System.out.println("[*] Waiting for connection on port " + this.serverPort);

      // Accept a client connection
      Socket clientSocket = this.acceptConnection();

      // Setup client connection object
      ClientConnection clientConnection = new ClientConnection(clientSocket);

      // Receive request
      String request = clientConnection.in.readLine();

      // Generate a response
      String response = generateResponse("request");

      // Send response to client
      clientConnection.out.println(response)

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
    if (args.length < 1) {
      System.out.println("Usage: java Server <Port>");
      System.exit(1);
    }

    // Extract arguments
    int serverPort = Integer.parseInt(args[0]);

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
