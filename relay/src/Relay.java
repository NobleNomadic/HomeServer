import java.net.*;
import java.io.*;

// Main class
public class Relay {
  // Properties
  int serverPort;
  ServerSocket serverSocket;

  // Constructor
  public Relay(int serverPort) {
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

  private generateResponse

  // Main loop function for getting connections, and sending response
  public void mainLoop() {
    System.out.println("[*] Waiting for connection on port " + this.serverPort);

    // Accept a client connection
    Socket clientSocket = this.acceptConnection();
    if (clientSocket == null) {
      return;
    }

    // Generate a response
    String response = generateResponse(String request);

  } // Main loop


  // Entry
  public static void main(String[] args) {
    // Usage failure
    if (args.length < 1) {
      System.out.println("Usage: java Relay <Port>");
      System.exit(1);
    }

    // Extract arguments
    int serverPort = Integer.parseInt(args[0]);

    // Create main instance of server
    Relay relay = new Relay(serverPort);
    System.out.println("[*] Starting server");

    // Start main server loop
    while (true) {
      relay.mainLoop();
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
      this.clientSocket = clientSocket;
      this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      this.out = new PrintWriter(clientSocket.getOuputStream(), true);
    }
  } // End ClientConnection
} // End Relay
