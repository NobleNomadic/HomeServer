import java.net.*;
import java.io.*;
import java.nio.file.*;

// Main file server class
public class FileServer {
  // Properties
  // Server socket
  ServerSocket serverSocket;
  int serverPort;
  // Constructor
  public FileServer(int serverPort) {
    this.serverPort = serverPort;

    // Create the socket
    try {
      this.serverSocket = new ServerSocket(this.serverPort);
    }
    // Catch socket failures
    catch (IOException e) {
      System.out.println("[-] Erorr creating server socket: " + e);
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

  // Main loop function
  public void mainLoop() {
    System.out.println("[*] Waiting for connection on port " + this.serverPort);

    // Accept client connection
    Socket clientSocket = this.acceptConnection();
    System.out.println("[+] New connection");

    // Setup client connection object
    ClientConnection clientConnection = new ClientConnection(clientSocket);
    return;
  } // End main loop

  // Entry
  public static void main(String[] args) {
    // Bad usage error
    if (args.length < 1) {
      System.out.println("Usage java FileServer <Port>");
    }
    // Extract arguments
    int serverPort = Integer.parseInt(args[0]);

    // Main instance of server
    FileServer fileServer = new FileServer(serverPort);
    System.out.println("[*] Server starting");
    
    // Start loop
    while (true) {
      fileServer.mainLoop();
    }
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
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
      }
      // Failed to setup data streams for client socket
      catch (IOException e) {
        System.out.println("[-] Error opening data streams with client");
      }
    } // End constructor
  } // End ClientConnection
} // FileServer
