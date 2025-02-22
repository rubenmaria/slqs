package com.slqs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.JSONObject;

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
      try {
        handleProtocolMessage(new JSONObject(message));
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }

  private void handleProtocolMessage(JSONObject protocolMessage) throws Exception {
    JSONObject data = protocolMessage.getJSONObject(Protocol.DATA);
    if (protocolMessage.getString(Protocol.COMMAND).compareTo(Protocol.SEND_FILE_COMMAND) == 0) {
      receiveFile(data.getString(Protocol.FILE_NAME), data.getLong(Protocol.FILE_SIZE));
    }
  }

  private void receiveFile(String fileName, long fileSize) throws Exception {
    printFileRequest(fileName, fileSize);
    if (!isPositiveAnswer()) {
      sendMessage(Protocol.createRejectResponse().toString());
      return;
    }
    sendMessage(Protocol.createAcceptResponse().toString());
  }

  private boolean isPositiveAnswer() {
    return System.console().readLine().compareToIgnoreCase("y") == 0;
  }

  private void printFileRequest(String fileName, long fileSize) {
    System.out.println(String.format(
        "Do you want to recieve the File \"%s\" with size %.4f KB from \"%s\"? [Y/n]",
        fileName, fileSize / 10e3, clientConnection.getInetAddress()));
  }

  public void close() {
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
