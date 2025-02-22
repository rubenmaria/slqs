package com.slqs;

import org.json.*;
import java.util.UUID;

public class Protocol {
  public static final String SEND_FILE_COMMAND = "sendFile";
  public static final String ACCEPT_COMMAND = "accept";
  public static final String REJECT_COMMAND = "reject";
  public static final String COMMAND = "command";
  public static final String DATA = "data";
  public static final String FILE_NAME = "name";
  public static final String FILE_SIZE = "size";
  public static final String UUID_KEY = "uuid";

  public static JSONObject createSendFileRequest(String fileName, long fileSize) {
    JSONObject data = new JSONObject()
        .put(FILE_NAME, fileName)
        .put(FILE_SIZE, fileSize);
    return basicFormat(SEND_FILE_COMMAND, data);
  }

  public static JSONObject createAcceptResponse() {
    JSONObject uuid = new JSONObject()
        .put(UUID_KEY, UUID.randomUUID());
    return basicFormat(ACCEPT_COMMAND, uuid);
  }

  public static JSONObject createRejectResponse() {
    return basicFormat(REJECT_COMMAND, new JSONObject());
  }

  private static JSONObject basicFormat(String command, JSONObject data) {
    return new JSONObject()
        .put(COMMAND, command)
        .put(DATA, data);
  }
}
