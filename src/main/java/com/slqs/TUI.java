package com.slqs;

import java.util.Hashtable;

public class TUI {
  private final static int progressBarLength = 50;

  public static void printHelp() {
    System.out.println(
        "Usage:\n" +
            "slqs [Command]\n" +
            "Commands:\n" +
            " send [Flag] [Paramter]\t" +
            "sends a file or directory\n" +
            " receive [Optional]\t" +
            "receives a file or directory\n" +
            " help\t\t\t" + "displays help \n" +
            "Paramter:\n" +
            " --host, -h [HOST]" +
            "\tlocal adress of the receiver\n" +
            " --path, -p [PATH]" +
            "\tpath to object you want to share\n" +
            "Flags: \n" +
            " --force, -f" + "\t\tremoves receiving prompt\n" +
            " --recursive, -r" + "\trecursively send a directory\n" +
            "Optional:\n" +
            " --port [PORT]" + "\t\tcommunication port\n");
  }

  public static Hashtable<String, Object> parseArguments(String[] args) throws Exception {
    Hashtable<String, Object> arguments = parse(args);
    hasRequiredArguments(arguments);
    isValidPort(arguments);
    return arguments;
  }

  public static void isValidPort(Hashtable<String, Object> args) throws Exception {
    if (args.containsKey("port") && ((Integer) args.get("port")).intValue() < 0) {
      throw new RuntimeException("Port has to be a positve integer!");
    }
  }

  public static void hasRequiredArguments(Hashtable<String, Object> args) throws Exception {
    if (args.containsKey("send") && (!args.containsKey("path") || !args.containsKey("host"))) {
      throw new RuntimeException("Path and host are required arguments!");
    }
  }

  public static Hashtable<String, Object> parse(String[] args) throws Exception {
    Hashtable<String, Object> arguments = new Hashtable<String, Object>();
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "send":
          arguments.put("send", Boolean.valueOf(true));
          break;
        case "receive":
          arguments.put("receive", Boolean.valueOf(true));
          break;
        case "help":
          arguments.put("help", Boolean.valueOf(true));
          break;
        case "-rf":
          arguments.put("force", Boolean.valueOf(true));
          arguments.put("recursive", Boolean.valueOf(true));
          break;
        case "--force":
        case "-f":
          arguments.put("force", Boolean.valueOf(true));
          break;
        case "--recursive":
        case "-r":
          arguments.put("recursive", Boolean.valueOf(true));
          break;
        case "--port":
          if (args.length > i + 1) {
            arguments.put("port", Integer.valueOf(args[i + 1]));
          }
          break;
        case "--host":
        case "-h":
          if (args.length > i + 1) {
            arguments.put("host", args[i + 1]);
          }
          break;
        case "--path":
        case "-p":
          if (args.length > i + 1) {
            arguments.put("path", args[i + 1]);
          }
          break;
      }
    }
    return arguments;
  }

  public static void printRejection() {
    System.out.println("Remote host rejected transmission!");
  }

  public static void printError(Exception e) {
    System.out.println("Error occurred:\n" +
        " Type: " + e.getClass().getSimpleName() + "\n" +
        " Message: " + e.getMessage() + "\n" +
        " Stack Trace:");
    e.printStackTrace();
  }

  public static void printWaitingForConnection() {
    System.out.println("Waiting for requests...");
  }

  public static void printProgressBar(double current, double total) {
    char return_char = current == total ? '\n' : '\r';
    StringBuilder sb = new StringBuilder();
    final int progressLength = TUI.progressBarLength - 2;
    int progress = (int) Math.floor(current / total * progressLength);
    sb.append("[");
    sb.append("#".repeat(progress));
    sb.append(" ".repeat(progressLength - progress));
    sb.append("]" + return_char);
    System.out.print(sb.toString());
  }

  public static void printFileReceived(String fileName) {
    System.out.println(String.format(
        "Succesfully received file \"%s\"!", fileName));
  }

  public static void printDirectoryReceived(String directoryName) {
    System.out.println(String.format(
        "Succesfully received directory \"%s\"!", directoryName));
  }

  public static void printFileSent(String fileName) {
    System.out.println(String.format(
        "Succesfully sent file \"%s\"!", fileName));
  }

  public static void printFileRequest(String path, long size, String host) {
    System.out.println(String.format(
        "Do you want to recieve the File \"%s\" with size %.4f KB from \"%s\"? [Y/n]",
        path, size / 10e3, host));
  }

  public static void printDirectoryRequest(String path, long size, String host, boolean force) {
    System.out.println(String.format(
        "Do you want to recieve the Directory \"%s\" with size %.4f KB from \"%s\" " +
            (force ? "with force enabled"
                : "with force disabled")
            + "? [Y/n]",
        path, size / 10e3, host));
  }

  public static boolean promptContiue() {
    System.out.println("Do you want to continue receiving files or directories? [Y/n]");
    return isPositiveAnswer();
  }

  public static String promptSafePath() {
    System.out.print("Enter a valid path to store the data: ");
    String path = System.console().readLine();
    while (!FileSystem.isValidDirectoryPath(path)) {
      System.out.print("Invalid path: Make sure the path"
          + "to the directory exists!");
      System.out.print("Enter a path to store the data: ");
      path = System.console().readLine();
    }
    return path;
  }

  public static boolean isPositiveAnswer() {
    return System.console().readLine().compareToIgnoreCase("y") == 0;
  }

  public static void printInvalidComannd() {
    System.out.println("Received invalid protocal message! Aborting...");
  }
}
