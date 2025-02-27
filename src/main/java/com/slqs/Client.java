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
import java.nio.file.LinkOption;
import java.io.IOException;
import java.util.UUID;
import java.util.List;

import org.json.JSONObject;

public class Client {
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
        getFileName(currentFilePath), getFileSize(currentFilePath));
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

  public void sendDirectory(String path, String safePath, boolean force) throws Exception{
    currentDirectoryPath = path;
    sendDirectoryRequest(safePath, force);
    if(!wasDiretoryAccepted()) {
      System.out.println("Remote host rejected directory transmission!");
      return;
    }
    sendBeginDirectoryTransmission();
    sendDirectoryFiles();
    sendEndDirectoryTransmission();
  }

  private void sendDirectoryRequest(String safePath, boolean force) throws IOException{
    sendMessage(Protocol.createSendDirectoryRequest(
      currentDirectoryPath, safePath, getDirectorySize(), force).toString());
  }

  private long getDirectorySize() throws IOException {
    List<String> paths = Files.walk(Paths.get(currentDirectoryPath))
      .map((x) -> x.toString())
      .toList();
    long size = 0;
    for(String filePath : paths) {
      if(!isDirectory(filePath)) {
        size += getFileSize(filePath);
      }
    }
    return size;
  }

  private boolean wasDiretoryAccepted() {
    return false;
  }

  private void sendBeginDirectoryTransmission() throws IOException{
    currentDirectoryID = UUID.randomUUID();
    sendMessage(Protocol.
        createBeginDirectoryTransmission(currentDirectoryID).toString());
  }

  private void sendDirectoryFiles() throws Exception {
    List<String> paths = Files.walk(Paths.get(currentDirectoryPath))
      .map((x) -> x.toString())
      .toList();
    for(String filePath : paths) {
      if(!isDirectory(filePath)) {
        sendFile(filePath);
      }
    }
  }

  private void sendEndDirectoryTransmission() throws IOException{
    sendMessage(Protocol.
        createEndDirectoryTransmission(currentDirectoryID).toString());
  }

  private boolean isDirectory(String path) {
    File dir = new File(path);
    return dir.isDirectory();
  }

  public void sendFile(String filePath) throws Exception {
    currentFilePath = filePath;
    if(!isValidFilePath()) {
      System.out.println("Invalid file path: Make sure the file path exists!");
      return;
    }
    sendFileRequest();
    if (!wasFileAccepted()) {
      System.out.println("Remote host rejected file transmission!");
      return;
    }
    sendBeginFileTransmission();
    transmitFileData();
    sendEndFileTransmission();
    TUI.printFileSent(getFileName());
  }

  private String getFileName() {
    Path path = Paths.get(currentFilePath);
    return path.getFileName().toString();
  }

  private void sendBeginFileTransmission() throws IOException {
    sendMessage(
        Protocol.createBeginFileTransmission(currentFileID).toString());
  }

  private void sendEndFileTransmission() throws IOException {
    sendMessage(Protocol.createEndFileTransmission(currentFileID).toString());
  }

  public void transmitFileData() throws Exception {
    final long fileSize = getFileSize(currentFilePath);
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

  private boolean isValidFilePath() {
    Path path = Paths.get(currentFilePath);
    return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
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
