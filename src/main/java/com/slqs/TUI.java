package com.slqs;

public class TUI {
  private final static int progressBarLength = 50;

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

  public static void printFileSent(String fileName) {
    System.out.println(String.format(
      "Succesfully sent file \"%s\"!", fileName));
  }
}
