package com.slqs;

public class App {
  private final static int PORT = 8080;
  private final static String DEFAULT_HOST = "localhost";
  static final String PATH_DIR = "/home/rubs/pictures/";
  static final String PATH_FILE = "/home/rubs/Downloads/B5_Triwari.pdf";

  public static void main(String[] args) {
    if (TUI.isSendCommand(args)) {
      send(args);
    } else if (TUI.isReceiveCommand(args)) {
      receive(args);
    } else {
      TUI.printHelp();
    }
  }

  public static void receive(String[] args) {
    Receiver receiver = new Receiver();
    if (!TUI.hasPort(args)) {
      receiver.receive(PORT);
      return;
    }
    Integer port = TUI.parsePort(args);
    if (port == null) {
      TUI.printInvalidPort();
    }
    receiver.receive(port.intValue());
  }

  public static void send(String[] args) {
    Sender sender = new Sender();
    boolean forceFlag = TUI.isForceSet(args);
    boolean isDirectory = TUI.isRecursiveSet(args);
    String host = TUI.parseHost(args);
    String path = TUI.parsePath(args);
    if (!TUI.hasHost(args)) {
      TUI.printMissingHost();
      return;
    }
    if (!TUI.hasPath(args)) {
      TUI.printMissingPath();
      return;
    }
    if (host == null) {
      TUI.printInvalidHost();
      return;
    }
    if (path == null) {
      TUI.printInvalidPath();
      return;
    }
    try {
      if (isDirectory) {
        sender.sendDirectory(path, forceFlag);
      } else {
        sender.sendFile(path);
      }
    } catch (Exception e) {
      TUI.printError(e);
    }
  }
}
