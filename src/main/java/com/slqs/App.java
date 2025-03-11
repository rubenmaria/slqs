package com.slqs;

import java.util.Hashtable;

public class App {
  private final static int PORT = 8080;

  public static void main(String[] args) {
    try {
      Hashtable<String, Object> arguments = TUI.parseArguments(args);
      if (arguments.containsKey("send")) {
        send(arguments);
      } else if (arguments.containsKey("receive")) {
        receive(arguments);
      } else {
        TUI.printHelp();
      }
    } catch (Exception e) {
      TUI.printError(e);
    }
  }

  public static void receive(Hashtable<String, Object> arguments) throws Exception {
    Receiver receiver = new Receiver();
    int port = (Integer) arguments.getOrDefault("port", PORT);
    receiver.receive(port);
    receiver.close();
  }

  public static void send(Hashtable<String, Object> arguments) throws Exception {
    Sender sender = new Sender();
    boolean forceFlag = arguments.containsKey("force");
    boolean isDirectory = arguments.containsKey("recursive");
    String host = (String) arguments.get("host");
    String path = (String) arguments.get("path");
    int port = (Integer) arguments.getOrDefault("port", PORT);
    if (isDirectory) {
      if (!FileSystem.isValidDirectoryPath(path)) {
        throw new RuntimeException("\"" + path + "\" is not a valid directory!");
      }
      sender.connectTo(host, port);
      sender.sendDirectory(path, forceFlag);
    } else {
      if (!FileSystem.isValidFilePath(path)) {
        throw new RuntimeException("\"" + path + "\" is not a valid file!");
      }
      sender.connectTo(host, port);
      sender.sendFile(path);
    }
    sender.disconnect();
  }
}
