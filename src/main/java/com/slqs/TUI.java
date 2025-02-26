package com.slqs;

public class TUI {
  private final static int progressBarLength = 20;

  public static void printProgressBar(double current, double total) {
    StringBuilder sb = new StringBuilder();
    final int progressLength = TUI.progressBarLength - 2;
    int progress = (int) Math.floor(current / total * progressLength);
    sb.append("[");
    sb.append("#".repeat(progress));
    sb.append(" ".repeat(progressLength - progress));
    sb.append("]\r");
    System.out.print(sb.toString());
  }
}
