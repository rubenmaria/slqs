package com.slqs;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.UUID;

import org.json.JSONObject;

public class Client {
  private Socket clientSocket;
  private PrintWriter out;
  private BufferedReader in;
  private UUID currentFileID;

  public Client(String host, int port) {
    try {
      connectTo(host, port);
    } catch (Exception e) {
      System.out.println("Error: " + e);
      disconnect();
    }
  }

  public void connectTo(String ip, int port) throws IOException {
    clientSocket = new Socket(ip, port);
    out = new PrintWriter(clientSocket.getOutputStream(), true);
    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
  }

  public void sendFileRequest(String filePath) throws IOException {
    JSONObject request = Protocol.createSendFileRequest(getFileName(filePath), getFileSize(filePath));
    sendMessage(request.toString());
  }

  public boolean wasFileAccepted() throws Exception {
    JSONObject response = new JSONObject(receiveMessage());
    JSONObject data = response.getJSONObject(Protocol.DATA_KEY);
    if (!Protocol.hasValidCommand(response, Protocol.ACCEPT_COMMAND)) {
      return false;
    }
    currentFileID = UUID.fromString(data.getString(Protocol.UUID_KEY));
    return true;
  }

  public void sendFile(String filePath) throws Exception {
    sendFileRequest(filePath);
    if (!wasFileAccepted()) {
      System.out.println("Remote host rejected file transmission!");
      return;
    }
    sendBeginFileTransmission();
    transmitFileData(filePath);
    sendEndFileTransmission();
  }

  private void sendBeginFileTransmission() throws IOException {
    sendMessage(
        Protocol.createBeginFileTransmission(currentFileID).toString());
  }

  private void sendEndFileTransmission() throws IOException {
    sendMessage(Protocol.createEndFileTransmission(currentFileID).toString());
  }

  public void transmitFileData(String filePath) throws Exception {
    final long fileSize = getFileSize(filePath);
    byte[] fileData = new byte[Protocol.PACKAGE_SIZE];
    FileInputStream in = new FileInputStream(new File(filePath));
    long sent = 0;
    int currentSent;
    while ((currentSent = in.read(fileData)) > 0) {
      sent += currentSent;
      sendFileDataPackage(fileData, currentSent);
      TUI.printProgressBar(sent, fileSize);
    }
    in.close();
  }

  private void sendFileDataPackage(byte[] data, int sentBytes) throws IOException {
    sendMessage(Protocol.createFileDataPackage(currentFileID, data, sentBytes)
        .toString());
  }

  private String getFileName(String filePath) {
    Path path = Paths.get(filePath);
    return path.getFileName().toString();
  }

  private long getFileSize(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    return Files.size(path);
  }

  private String receiveMessage() throws IOException {
    return in.readLine();
  }

  public void sendMessage(String message) throws IOException {
    out.println(message);
  }

  public void disconnect() {
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
