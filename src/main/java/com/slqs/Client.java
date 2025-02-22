package com.slqs;

import java.net.Socket;
import java.io.*;
import java.nio.file.*;
import java.io.IOException;

import org.json.*;

public class Client {
  private Socket clientSocket;
  private PrintWriter out;
  private BufferedReader in;
  private boolean running;
  private String host;
  private int port;

  public Client(String host, int port) {
    running = true;
    this.host = host;
    this.port = port;
    try {
      connectTo(host, port);
    } catch (Exception e) {
      System.out.println("Error: " + e);
      disconnect();
    }
  }

  public void connectTo(String ip, int port) throws IOException {
    connectToServer(ip, port);
  }

  public void sendFileRequest(String filePath) throws IOException {
    JSONObject request = Protocol.createSendFileRequest(getFileName(filePath), getFileSize(filePath));
    sendMessage(request.toString());
  }

  public boolean wasFileAccepted() throws Exception {
    JSONObject response = new JSONObject(receiveMessage());
    return response.getString(Protocol.COMMAND).compareTo(Protocol.ACCEPT_COMMAND) == 0;
  }

  public void sendFile(String filePath) throws Exception {
    sendFileRequest(filePath);
    if (!wasFileAccepted()) {
      System.out.println("Remote host rejected file transmission!");
      return;
    }
    transmitFileData(filePath);
  }

  public void transmitFileData(String filePath) throws Exception {
    char[] bytes = new char[16 * 1024];
    BufferedReader in = new BufferedReader(new FileReader(filePath));
    long fileSize = getFileSize(filePath);
    long sent = 0;
    int currentSent;
    while ((currentSent = in.read(bytes)) > 0) {
      sent += currentSent;
      out.write(bytes, 0, currentSent);
      printUploadProgress(sent, fileSize);
    }
    in.close();
  }

  private void printUploadProgress(long sentAmount, long all) {
    StringBuilder sb = new StringBuilder();
    final long progressLength = 100;
    int progress = (int) ((all / sentAmount) * progressLength);
    sb.append("[");
    sb.append("#".repeat(progress));
    sb.append("]");
  }

  private String getFileName(String filePath) {
    Path path = Paths.get(filePath);
    return path.getFileName().toString();
  }

  private long getFileSize(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    return Files.size(path);
  }

  private void connectToServer(String ip, int port) throws IOException {
    clientSocket = new Socket(ip, port);
    out = new PrintWriter(clientSocket.getOutputStream(), true);
    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
  }

  private String receiveMessage() throws IOException {
    return in.readLine();
  }

  public boolean isRunning() {
    return running;
  }

  public void stopRunning() {
    running = false;
  }

  public void sendMessage(String message) throws IOException {
    out.println(message);
  }

  public void disconnect() {
    running = false;
    try {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
      if (clientSocket != null) {
        clientSocket.close();
      }
    } catch (Exception e) {
      System.out.println("Error occured disconnecting: " + e);
    }
  }
}
