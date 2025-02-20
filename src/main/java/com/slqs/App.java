package com.slqs;

import org.json.*;

/**
 * Hello world!
 *
 */
public class App {
  private final static int PORT = 8080;

  public static void main(String[] args) {
    System.out.print("Server host:");
    final String host = System.console().readLine();

    JSONObject jo = new JSONObject("{ \"abc\" : \"def\" }");
    System.out.println(jo);

    Client client = new Client(host, PORT);
    Server server = new Server(PORT);

    Thread serverThread = new Thread(server);
    Thread clientThread = new Thread(client);
    serverThread.start();
    clientThread.start();
  }
}
