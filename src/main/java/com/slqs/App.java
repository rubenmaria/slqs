package com.slqs;

public class App {
  private final static int PORT = 8080;
  private final static String DEFAULT_HOST = "localhost";

  public static void main(String[] args) {
    System.out.println("server/client?");
    final boolean isServer = System.console().readLine().contains("server");

    if (isServer) {
      Server server = new Server(PORT);
      try {
        server.listen();
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    } else {
      Client client = new Client(DEFAULT_HOST, PORT);
      try {
        final String PATH = "/home/rubs/workspace/java/slqs/src/main/java/com/slqs/App.java";
        client.sendFileRequest(PATH);
        client.handleFileResponse(PATH);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }
}
