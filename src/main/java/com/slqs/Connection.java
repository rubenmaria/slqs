package slqs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Runnable {
  private Socket clientConnection;
  private BufferedReader in;
  private PrintWriter out;
  private String username;
  private Server server;

  public Connection(Server server, Socket client) {
    this.server = server;
    this.clientConnection = client;
    System.out.println("Client connected! " + client.getInetAddress());
  }

  @Override
  public void run() {
    try {
      initIO();
      sendMessage("pong");
      handleClientMessages();
    } catch (Exception e) {
      out.println("Error occured: " + e + "\n Connection closed!");
      this.close();
    }
  }

  private void initIO() throws IOException {
    out = new PrintWriter(clientConnection.getOutputStream(), true);
    in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
  }

  private void handleClientMessages() throws IOException {
    String message;
    while ((message = in.readLine()) != null) {
      System.out.println(message);
    }
  }

  public void close() {
    server.broadcast(username + " left the chat!");
    System.out.println(
        "client: " + username + " ("
            + clientConnection.getInetAddress()
            + ")" + " disconnected!");
    try {
      if (!clientConnection.isClosed()) {
        clientConnection.close();
      }
      in.close();
      out.close();
    } catch (Exception e) {
      System.out.println("Error occured closing client connection: " + e);
    }
  }

  public void sendMessage(String message) {
    out.println(message);
  }
}
