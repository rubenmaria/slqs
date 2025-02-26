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
      String fileName = data.getString(Protocol.FILE_NAME_KEY);
      long fileSize = data.getLong(Protocol.FILE_SIZE_KEY);
      receiveFile(fileName, fileSize);
    }
  }

  private void receiveFile(String fileName, long fileSize) throws Exception {
    printFileRequest(fileName, fileSize);
    if (!isPositiveAnswer()) {
      sendRejectResponse();
      return;
    }
    sendAcceptResponse();
    if (!receiveTransmissionBegin()) {
      return;
    }
    receiveFileData(fileName);
  }

  private void sendRejectResponse() throws IOException {
    sendMessage(Protocol.createRejectResponse().toString());
  }

  private void receiveFileData(String fileName) throws Exception {
    createNewFile(fileName);
    byte[] fileData;
    FileOutputStream out_file = new FileOutputStream(fileName);
    JSONObject message = new JSONObject(receiveMessage());
    while (!Protocol.hasValidCommand(message, Protocol.END_TRANSMISSION_COMMAND)) {
      fileData = decodeFileData(message);
      out_file.write(fileData, 0, fileData.length);
      message = new JSONObject(receiveMessage());
    }
    out_file.close();
  }

  private void createNewFile(String fileName) throws IOException {
    File new_file = new File(fileName);
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

  private void printFileRequest(String fileName, long fileSize) {
    System.out.println(String.format(
        "Do you want to recieve the File \"%s\" with size %.4f KB from \"%s\"? [Y/n]",
        fileName, fileSize / 10e3, clientConnection.getInetAddress()));
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
