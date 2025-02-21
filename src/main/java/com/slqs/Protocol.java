package com.slqs;

import org.json.*;

public class Protocol {
  public static final String SEND_FILE_COMMAND = "sendFile";

  public static JSONObject sendFileRequest(String fileName, int fileSize) {
    JSONObject data = new JSONObject()
      .put("name", fileName)
      .put("size", fileSize);
    return basicFormat(SEND_FILE_COMMAND, data);
  }

  static private JSONObject basicFormat(String command, JSONObject data) {
    return new JSONObject()
      .put("command", command)
      .put("data", data);
  }
}
