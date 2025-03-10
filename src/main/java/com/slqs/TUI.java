package com.slqs;

import org.json.JSONException;

public class TUI {
  private final static int progressBarLength = 50;

  public static void printHelp() {
    System.out.println(
        "slqs [Command]\n" +
            "Commands:\n" +
            "\t send\n [Flag] [Paramter]" +
            "\t receive [Optional]\n" +
            "\t help\n" +
            "Paramter:\n" +
            "\t --host, -h [HOST]\n" +
            "\t --path, -p [PATH]\n" +
            "Flags: \n" +
            "\t --force, -f\n" +
            "\t --recursive, -r\n" +
            "Optional:\n" +
            "\t --port [PORT]\n");
  }

  public static void printProtocolError(JSONException e) {
    System.out.println("Invalid protocol message received: " + e.getMessage());
  }

  public static void printRejection() {
    System.out.println("Remote host rejected transmission!");
  }

  public static void printInvalidDirectory() {
    System.out.println("Invalid directory path: Make sure the directory exists!");
  }

  public static void printError(Exception e) {
    System.out.println("Something went wrong: " + e.getMessage());
  }

  public static void printInvalidPath() {
    System.out.println("Given path is Invalid!");
    printHelp();
  }

  public static void printInvalidHost() {
    System.out.println("Given host is invalid!");
    printHelp();
  }

  public static void printMissingPath() {
    System.out.println("No path were given!");
    printHelp();
  }

  public static void printMissingHost() {
    System.out.println("No host were given!");
    printHelp();
  }

  public static void printInvalidPort() {
    System.out.println("Given port is invalid!");
    printHelp();
  }

  public static boolean hasHost(String[] args) {
    return hasOption(args, "-h", "--host");
  }

  public static boolean hasPath(String[] args) {
    return hasOption(args, "-p", "--path");
  }

  public static boolean hasPort(String[] args) {
    return hasOption(args, "--port");
  }

  public static Integer parsePort(String[] args) {
    String port = getStringAfter(args, "--port");
    if (port == null) {
      return null;
    }
    if (port.chars().allMatch(Character::isDigit)) {
      return Integer.valueOf(port);
    }
    return null;
  }

  public static String parseHost(String[] args) {
    return getStringAfter(args, "--host", "-h");
  }

  public static String parsePath(String[] args) {
    return getStringAfter(args, "--path", "-p");
  }

  private static String getStringAfter(String[] args, String... options) {
    int parameterValueIndex;
    boolean indexFound = false;
    for (int i = 0; i < args.length; i++) {
      for (String option : options) {
        if (args[i].compareTo(option) == 0) {
          indexFound = true;
          break;
        }
      }
      if (!indexFound) {
        continue;
      }
      parameterValueIndex = i + 1;
      if (parameterValueIndex >= args.length) {
        return null;
      }
      return args[parameterValueIndex];
    }
    return null;
  }

  public static boolean isForceSet(String[] args) {
    return hasOption(args, "-f", "--force");
  }

  public static boolean isRecursiveSet(String[] args) {
    return hasOption(args, "-r", "--recursive");
  }

  private static boolean hasOption(String[] args, String... options) {
    for (String arg : args) {
      for (String option : options) {
        if (arg.compareTo(option) == 0) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isSendCommand(String[] args) {
    return args[1].compareTo("send") == 0;
  }

  public static boolean isHelpCommand(String[] args) {
    return args[1].compareTo("help") == 0;
  }

  public static boolean isReceiveCommand(String[] args) {
    return args[1].compareTo("receive") == 0;
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
