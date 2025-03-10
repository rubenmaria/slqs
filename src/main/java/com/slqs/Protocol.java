package com.slqs;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONObject;

public class Protocol {
  public static final String ACCEPT_COMMAND = "accept";
  public static final String REJECT_COMMAND = "reject";
  public static final String SEND_FILE_COMMAND = "sendFile";
  public static final String BEGIN_TRANSMISSION_COMMAND = "beginTransmission";
  public static final String END_TRANSMISSION_COMMAND = "endTransmission";
  public static final String FILE_DATA_PACKAGE_COMMAND = "fileDataPackagae";
  public static final String SEND_DIRECTORY_COMMAND = "sendDir";

  public static final String COMMAND_KEY = "command";
  public static final String DATA_KEY = "data";
  public static final String NAME_KEY = "name";
  public static final String SIZE_KEY = "size";
  public static final String FORCE_KEY = "force";
  public static final String UUID_KEY = "uuid";
  public static final String FILE_DATA_KEY = "fileData";

  public static final int PACKAGE_SIZE = 1024;

  public static JSONObject createSendDirectoryRequest(
      String directory,
      long size,
      boolean force) {
    JSONObject data = new JSONObject()
        .put(NAME_KEY, directory)
        .put(SIZE_KEY, size)
        .put(FORCE_KEY, force);
    return basicFormat(SEND_DIRECTORY_COMMAND, data);
  }

  public static JSONObject createBeginDirectoryTransmission(UUID directoryID) {
    JSONObject uuid = new JSONObject()
        .put(UUID_KEY, directoryID.toString());
    return basicFormat(BEGIN_TRANSMISSION_COMMAND, uuid);
  }

  public static JSONObject createEndDirectoryTransmission(UUID directoryID) {
    JSONObject uuid = new JSONObject()
        .put(UUID_KEY, directoryID.toString());
    return basicFormat(END_TRANSMISSION_COMMAND, uuid);
  }

  public static JSONObject createBeginFileTransmission(UUID fileID) {
    JSONObject uuid = new JSONObject()
        .put(UUID_KEY, fileID.toString());
    return basicFormat(BEGIN_TRANSMISSION_COMMAND, uuid);
  }

  public static JSONObject createEndFileTransmission(UUID fileID) {
    JSONObject uuid = new JSONObject()
        .put(UUID_KEY, fileID.toString());
    return basicFormat(END_TRANSMISSION_COMMAND, uuid);
  }

  public static JSONObject createFileDataPackage(UUID fileID, byte[] fileData, int sentBytes) {
    List<Integer> encodedFileData = IntStream.range(0, sentBytes)
        .map((i) -> fileData[i])
        .boxed()
        .collect(Collectors.toList());

    JSONObject data = new JSONObject()
        .put(UUID_KEY, fileID.toString())
        .put(FILE_DATA_KEY, encodedFileData);
    return basicFormat(FILE_DATA_PACKAGE_COMMAND, data);
  }

  public static JSONObject createSendFileRequest(String fileName, long fileSize) {
    JSONObject data = new JSONObject()
        .put(NAME_KEY, fileName)
        .put(SIZE_KEY, fileSize);
    return basicFormat(SEND_FILE_COMMAND, data);
  }

  public static JSONObject createAcceptResponse(UUID newFileID) {
    JSONObject uuid = new JSONObject()
        .put(UUID_KEY, newFileID.toString());
    return basicFormat(ACCEPT_COMMAND, uuid);
  }

  public static JSONObject createRejectResponse() {
    return basicFormat(REJECT_COMMAND, new JSONObject());
  }

  private static JSONObject basicFormat(String command, JSONObject data) {
    return new JSONObject()
        .put(COMMAND_KEY, command)
        .put(DATA_KEY, data);
  }

  public static boolean hasValidCommand(JSONObject message, String command) {
    return message.getString(Protocol.COMMAND_KEY).compareTo(command) == 0;
  }

  public static boolean hasValidFileID(JSONObject message, UUID fileID) {
    JSONObject data = message.getJSONObject(DATA_KEY);
    return UUID.fromString(data.getString(UUID_KEY)).compareTo(fileID) == 0;
  }
}
