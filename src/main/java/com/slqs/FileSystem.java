package com.slqs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.LinkOption;
import java.util.List;
import java.io.IOException;

public class FileSystem {
  public static long getDirectorySize(String path) throws IOException {
    List<String> paths = Files.walk(Paths.get(path))
        .map((x) -> x.toString())
        .toList();
    long size = 0;
    for (String filePath : paths) {
      if (!isDirectory(filePath)) {
        size += getFileSize(filePath);
      }
    }
    return size;
  }

  public static boolean isDirectory(String path) {
    File dir = new File(path);
    return dir.isDirectory();
  }

  public static boolean isValidFilePath(String filePath) {
    Path path = Paths.get(filePath);
    return Files.exists(path, LinkOption.NOFOLLOW_LINKS) &&
        !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
  }

  public static boolean isValidDirectoryPath(String filePath) {
    Path path = Paths.get(filePath);
    return Files.exists(path, LinkOption.NOFOLLOW_LINKS) &&
        Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
  }

  public static String getRootName(String filePath) {
    Path path = Paths.get(filePath);
    return path.getFileName().toString();
  }

  public static long getFileSize(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    return Files.size(path);
  }

  public static void createNewFile(String filePath) throws IOException {
    File file = new File(filePath);
    file.createNewFile();
  }

  public static void createDirectories(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    if (path.getParent() == null) {
      return;
    }
    File file = new File(path.getParent().toString());
    file.mkdirs();
  }

  public static String joinPaths(String path, String... other) {
    return Paths.get(path, other).toString();
  }
}
