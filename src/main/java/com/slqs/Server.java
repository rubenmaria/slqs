package com.slqs;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server implements Runnable {
  private ArrayList<Connection> clientConnections;
  private ServerSocket serverSocket;
  private ExecutorService connectionThreadPool;
  private boolean running;
  private int port;

  public Server(int port) {
    clientConnections = new ArrayList<>();
    running = true;
    this.port = port;
  }

  @Override
  public void run() {
    try {
      listen();
    } catch (Exception e) {
      System.out.println("Error: " + e);
      close();
    }
  }

  public void listen() throws IOException {
    serverSocket = new ServerSocket(port);
    connectionThreadPool = Executors.newCachedThreadPool();
    while (running) {
      Socket connectionSocket = serverSocket.accept();
      Connection clientConnection = new Connection(this, connectionSocket);
      clientConnections.add(clientConnection);
      connectionThreadPool.execute(clientConnection);
    }
  }

  public void broadcast(String message) {
    for (Connection connection : clientConnections) {
      if (connection != null) {
        connection.sendMessage(message);
      }
    }
  }

  public void close() {
    if (serverSocket == null) {
      return;
    }
    try {
      running = false;
      if (!serverSocket.isClosed()) {
        serverSocket.close();
      }
      for (Connection connection : clientConnections) {
        connection.close();
      }
    } catch (Exception e) {
      System.out.println("Error occured closing server: " + e);
    }
  }
}
