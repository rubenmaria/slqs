package com.slqs;

import java.net.Socket;
import java.net.SocketException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.UUID;
import java.util.List;

import org.json.JSONObject;

public class Sender {
  private Socket clientSocket;
  private PrintWriter out;
  private BufferedReader in;
  private UUID currentFileID;
  private UUID currentDirectoryID;
  private String currentFilePath;
  private String currentDirectoryPath;

  public void connectTo(String ip, int port) throws IOException {
    clientSocket = new Socket(ip, port);
    out = new PrintWriter(clientSocket.getOutputStream(), true);
    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
  }

  public void sendFileRequest() throws IOException {
    JSONObject request = Protocol.createSendFileRequest(
        FileSystem.getLeafName(currentFilePath),
        FileSystem.getFileSize(currentFilePath));
    sendMessage(request.toString());
  }

  public void sendDirectoryFileRequest() throws IOException {
    JSONObject request = Protocol.createSendFileRequest(
        FileSystem.getRelativePath(currentFilePath, currentDirectoryPath),
        FileSystem.getFileSize(currentFilePath));
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

  public void sendDirectory(String path, boolean force) throws Exception {
    currentDirectoryPath = path;
    sendDirectoryRequest(force);
    if (!wasDiretoryAccepted()) {
      TUI.printRejection();
      return;
    }
    sendBeginDirectoryTransmission();
    sendDirectoryFiles();
    sendEndDirectoryTransmission();
  }

  private void sendDirectoryRequest(boolean force) throws IOException {
    sendMessage(Protocol.createSendDirectoryRequest(
        FileSystem.getLeafName(currentDirectoryPath),
        FileSystem.getDirectorySize(currentDirectoryPath), force)
        .toString());
  }

  private boolean wasDiretoryAccepted() throws IOException {
    JSONObject message = new JSONObject(receiveMessage());
    JSONObject data = message.getJSONObject(Protocol.DATA_KEY);
    if (!Protocol.hasValidCommand(message, Protocol.ACCEPT_COMMAND)) {
      return false;
    }
    currentDirectoryID = UUID.fromString(data.getString(Protocol.UUID_KEY));
    return true;
  }

  private void sendBeginDirectoryTransmission() throws IOException {
    sendMessage(Protocol.createBeginDirectoryTransmission(currentDirectoryID).toString());
  }

  private void sendDirectoryFiles() throws Exception {
    List<String> files = Files.walk(Paths.get(currentDirectoryPath))
        .map((x) -> x.toString())
        .filter((x) -> !FileSystem.isDirectory(x))
        .toList();
    for (String path : files) {
      sendDirectoryFile(path);
    }
  }

  private void sendDirectoryFile(String filePath) throws Exception {
    currentFilePath = filePath;
    sendDirectoryFileRequest();
    if (!wasFileAccepted()) {
      TUI.printRejection();
      return;
    }
    sendBeginFileTransmission();
    transmitFileData();
    sendEndFileTransmission();
    TUI.printFileSent(FileSystem.getRelativePath(
        currentFilePath, currentDirectoryPath));
  }

  private void sendEndDirectoryTransmission() throws IOException {
    sendMessage(Protocol.createEndDirectoryTransmission(currentDirectoryID).toString());
  }

  public void sendFile(String filePath) throws Exception {
    currentFilePath = filePath;
    sendFileRequest();
    if (!wasFileAccepted()) {
      TUI.printRejection();
      return;
    }
    sendBeginFileTransmission();
    transmitFileData();
    sendEndFileTransmission();
    TUI.printFileSent(FileSystem.getLeafName(currentFilePath));
  }

  private void sendBeginFileTransmission() throws IOException {
    sendMessage(
        Protocol.createBeginFileTransmission(currentFileID).toString());
  }

  private void sendEndFileTransmission() throws IOException {
    sendMessage(Protocol.createEndFileTransmission(currentFileID).toString());
  }

  public void transmitFileData() throws Exception {
    final long fileSize = FileSystem.getFileSize(currentFilePath);
    byte[] fileData = new byte[Protocol.PACKAGE_SIZE];
    FileInputStream in = new FileInputStream(new File(currentFilePath));
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

  private String receiveMessage() throws IOException {
    String raw = in.readLine();
    if (raw == null) {
      throw new SocketException("Remote host has closed the connection!");
    }
    return raw;
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
      TUI.printError(e);
    }
  }
}
