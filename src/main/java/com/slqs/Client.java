package slqs;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;

public class Client implements Runnable {
  private Socket clientSocket;
  private PrintWriter out;
  private BufferedReader in;
  private boolean running;
  private String host;
  private int port;

  public Client(String host, int port) {
    running = true;
    this.host = host;
    this.port = port;
  }

  @Override
  public void run() {
    try {
      connectTo(host, port);
    } catch (Exception e) {
      System.out.println("Error: " + e);
      disconnect();
    }
  }

  public void connectTo(String ip, int port) throws IOException {
    connectToServer(ip, port);
  }

  public void sendFileRequest(int fileName) throws IOException {

  }

  private void connectToServer(String ip, int port) throws IOException {
    clientSocket = new Socket(ip, port);
    out = new PrintWriter(clientSocket.getOutputStream(), true);
    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
  }

  private String receiveMessage() throws IOException {
    return in.readLine();
  }

  public boolean isRunning() {
    return running;
  }

  public void stopRunning() {
    running = false;
  }

  public void sendMessage(String message) throws IOException {
    out.println(message);
  }

  public void disconnect() {
    running = false;
    try {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
      if (clientSocket != null) {
        clientSocket.close();
      }
    } catch (Exception e) {
      System.out.println("Error occured disconnecting: " + e);
    }
  }
}
