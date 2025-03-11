package com.slqs;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.net.ProtocolException;
import java.net.ServerSocket;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class Receiver {
  private ServerSocket serverSocket;
  private Socket clientConnection;
  private BufferedReader in;
  private PrintWriter out;
  private UUID currentID;
  private long currentSize;
  private String currentFileName;
  private String directoryName;
  private boolean currentForceFlag;
  private String currentSafePath;

  public void receive(int port) throws Exception {
    serverSocket = new ServerSocket(port);
    do {
      TUI.printWaitingForConnection();
      clientConnection = serverSocket.accept();
      initIO();
      receiveRequest();
    } while (TUI.promptContiue());
  }

  private void initIO() throws IOException {
    out = new PrintWriter(clientConnection.getOutputStream(), true);
    in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
  }

  private void receiveRequest() throws Exception {
    JSONObject message = new JSONObject(receiveMessage());
    JSONObject data = message.getJSONObject(Protocol.DATA_KEY);
    if (Protocol.hasValidCommand(message, Protocol.SEND_FILE_COMMAND)) {
      currentFileName = data.getString(Protocol.NAME_KEY);
      currentSize = data.getLong(Protocol.SIZE_KEY);
      receiveFile();
    } else if (Protocol.hasValidCommand(message, Protocol.SEND_DIRECTORY_COMMAND)) {
      directoryName = data.getString(Protocol.NAME_KEY);
      currentSize = data.getLong(Protocol.SIZE_KEY);
      currentForceFlag = data.getBoolean(Protocol.FORCE_KEY);
      receiveDirectory();
    }
  }

  private void receiveDirectory() throws Exception {
    TUI.printDirectoryRequest(directoryName, currentSize,
        clientConnection.getInetAddress().toString(), currentForceFlag);
    if (!TUI.isPositiveAnswer()) {
      sendRejectResponse();
      return;
    }
    currentSafePath = TUI.promptSafePath();
    sendAcceptResponse();
    if (!receiveTransmissionBegin()) {
      throw new ProtocolException("Expected transmission begin!");
    }
    receiveDirectoryFiles();
    TUI.printDirectoryReceived(directoryName);
  }

  private void receiveDirectoryFiles() throws Exception {
    JSONObject message = new JSONObject(receiveMessage());
    while (!Protocol.hasValidCommand(message, Protocol.END_TRANSMISSION_COMMAND)) {
      receiveDirectoryFile(message);
      message = new JSONObject(receiveMessage());
    }
  }

  private void receiveDirectoryFile(JSONObject request) throws Exception {
    if (!receiveFileRequest(request)) {
      return;
    }
    if (!currentForceFlag) {
      TUI.printFileRequest(currentFileName, currentSize,
          clientConnection.getInetAddress().toString());
      if (!TUI.isPositiveAnswer()) {
        sendRejectResponse();
        return;
      }
    }
    sendAcceptResponse();
    if (!receiveTransmissionBegin()) {
      throw new ProtocolException("Expected transmission begin!");
    }
    receiveFileData();
    TUI.printFileReceived(currentFileName);
  }

  private boolean receiveFileRequest(JSONObject request) throws Exception {
    JSONObject data = request.getJSONObject(Protocol.DATA_KEY);
    if (!Protocol.hasValidCommand(request, Protocol.SEND_FILE_COMMAND)) {
      return false;
    }
    currentFileName = data.getString(Protocol.NAME_KEY);
    currentSize = data.getLong(Protocol.SIZE_KEY);
    return true;
  }

  private void receiveFile() throws Exception {
    TUI.printFileRequest(currentFileName, currentSize,
        clientConnection.getInetAddress().toString());
    if (!TUI.isPositiveAnswer()) {
      sendRejectResponse();
      return;
    }
    currentSafePath = TUI.promptSafePath();
    sendAcceptResponse();
    if (!receiveTransmissionBegin()) {
      throw new ProtocolException("Expected transmission begin!");
    }
    receiveFileData();
    TUI.printFileReceived(currentFileName);
  }

  private void sendRejectResponse() throws IOException {
    sendMessage(Protocol.createRejectResponse().toString());
  }

  private void receiveFileData() throws Exception {
    byte[] fileData;
    FileOutputStream out_file = setupFile();
    JSONObject message = new JSONObject(receiveMessage());
    long totalReceived = 0;
    while (!Protocol.hasValidCommand(message, Protocol.END_TRANSMISSION_COMMAND)) {
      fileData = decodeFileData(message);
      out_file.write(fileData, 0, fileData.length);
      message = new JSONObject(receiveMessage());
      totalReceived += fileData.length;
      TUI.printProgressBar(totalReceived, currentSize);
    }
    out_file.close();
  }

  private FileOutputStream setupFile() throws IOException {
    String filePath = FileSystem.joinPaths(currentSafePath, currentFileName);
    FileSystem.createDirectories(filePath);
    FileSystem.createNewFile(filePath);
    return new FileOutputStream(filePath);
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
    return Protocol.hasValidFileID(message, currentID);
  }

  private String receiveMessage() throws IOException {
    String raw = in.readLine();
    if (raw == null) {
      throw new SocketException("Remote host closed connection!");
    }
    return raw;
  }

  private void sendAcceptResponse() throws Exception {
    currentID = UUID.randomUUID();
    JSONObject acceptResponse = Protocol.createAcceptResponse(currentID);
    sendMessage(acceptResponse.toString());
  }

  public void close() {
    try {
      if (!clientConnection.isClosed()) {
        clientConnection.close();
      }
      in.close();
      out.close();
    } catch (Exception e) {
    }
  }

  public void sendMessage(String message) {
    out.println(message);
  }
}
