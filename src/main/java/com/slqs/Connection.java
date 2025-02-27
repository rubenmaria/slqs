package com.slqs;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class Connection implements Runnable {
  private Socket clientConnection;
  private BufferedReader in;
  private PrintWriter out;
  private Server server;
  private UUID currentFileID;
  private long currentFileSize;
  private String currentFilePath;

  public Connection(Server server, Socket client) {
    this.server = server;
    this.clientConnection = client;
    System.out.println(String.format(
        "Received connection request from \"%s\"", client.getInetAddress()));
  }

  @Override
  public void run() {
    try {
      initIO();
      receiveRequest();
    } catch (JSONException e) {
      System.out.println("Received invalid message format!");
      close();
    } catch (Exception e) {
      System.out.println("Error occured: " + e.getMessage());
      close();
    }
  }

  private void initIO() throws IOException {
    out = new PrintWriter(clientConnection.getOutputStream(), true);
    in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
  }

  private void receiveRequest() throws Exception {
    JSONObject protocolMessage = new JSONObject(receiveMessage());
    JSONObject data = protocolMessage.getJSONObject(Protocol.DATA_KEY);
    if (Protocol.hasValidCommand(protocolMessage, Protocol.SEND_FILE_COMMAND)) {
      currentFilePath = data.getString(Protocol.NAME_KEY);
      currentFileSize = data.getLong(Protocol.SIZE_KEY);
      receiveFile();
    }
  }

  private void receiveFile() throws Exception {
    printFileRequest();
    if (!isPositiveAnswer()) {
      sendRejectResponse();
      return;
    }
    sendAcceptResponse();
    if (!receiveTransmissionBegin()) {
      return;
    }
    receiveFileData();
    TUI.printFileReceived(currentFilePath);
  }

  private void sendRejectResponse() throws IOException {
    sendMessage(Protocol.createRejectResponse().toString());
  }

  private void receiveFileData() throws Exception {
    createNewFile();
    byte[] fileData;
    FileOutputStream out_file = new FileOutputStream(currentFilePath);
    JSONObject message = new JSONObject(receiveMessage());
    long totalReceived = 0;
    while (!Protocol.hasValidCommand(message, Protocol.END_TRANSMISSION_COMMAND)) {
      fileData = decodeFileData(message);
      out_file.write(fileData, 0, fileData.length);
      message = new JSONObject(receiveMessage());
      totalReceived += fileData.length;
      TUI.printProgressBar(totalReceived, currentFileSize);
    }
    out_file.close();
  }

  private void createNewFile() throws IOException {
    File new_file = new File(currentFilePath);
    new_file.createNewFile();
  }

  private byte[] decodeFileData(JSONObject message) {
    JSONObject data = message.getJSONObject(Protocol.DATA_KEY);
    JSONArray rawFileData = data.getJSONArray(Protocol.FILE_DATA_KEY);
    byte[] decodedData = new byte[rawFileData.length()];
    for (int i = 0; i < rawFileData.length(); i++) {
      decodedData[i] = (byte) rawFileData.getInt(i);
    }
    return decodedData;
  }

  private boolean receiveTransmissionBegin() throws IOException {
    JSONObject message = new JSONObject(receiveMessage());
    if (!Protocol.hasValidCommand(message, Protocol.BEGIN_TRANSMISSION_COMMAND)) {
      return false;
    }
    return Protocol.hasValidFileID(message, currentFileID);
  }

  private String receiveMessage() throws IOException {
    return in.readLine();
  }

  private void sendAcceptResponse() throws Exception {
    currentFileID = UUID.randomUUID();
    JSONObject acceptResponse = Protocol.createAcceptResponse(currentFileID);
    sendMessage(acceptResponse.toString());
  }

  private boolean isPositiveAnswer() {
    return System.console().readLine().compareToIgnoreCase("y") == 0;
  }

  private void printFileRequest() {
    System.out.println(String.format(
        "Do you want to recieve the File \"%s\" with size %.4f KB from \"%s\"? [Y/n]",
        currentFilePath, currentFileSize / 10e3, clientConnection.getInetAddress()));
  }

  public void close() {
    System.out.println(
        "Closing connetion to: \""
            + clientConnection.getInetAddress()
            + "\"");
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
