// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;

public class FileUtil {
  public static final String ENDL = System.getProperty("line.separator");

  public static File createFile(String path, String content) {
    String names[] = path.split("/");
    if (names.length == 1)
      return createFile(new File(path), content);
    else {
      File parent = null;
      for (int i = 0; i < names.length - 1; i++) {
        parent = parent == null ? new File(names[i]) : new File(parent, names[i]);
        if (!parent.exists())
          parent.mkdir();
      }
      File fileToCreate = new File(parent, names[names.length - 1]);
      return createFile(fileToCreate, content);
    }
  }

  public static File createFile(File file, String content) {
    FileOutputStream fileOutput = null;
    try {
      fileOutput = new FileOutputStream(file);
      fileOutput.write(content.getBytes());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    finally {
      if (fileOutput != null)
        try {
          fileOutput.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
    }
    return file;
  }

  public static boolean makeDir(String path) {
    return new File(path).mkdir();
  }

  public static void deleteFileSystemDirectory(String dirPath) {
    deleteFileSystemDirectory(new File(dirPath));
  }

  public static void deleteFileSystemDirectory(File current) {
    File[] files = current.listFiles();

    for (int i = 0; files != null && i < files.length; i++) {
      File file = files[i];
      if (file.isDirectory())
        deleteFileSystemDirectory(file);
      else
        deleteFile(file);
    }
    deleteFile(current);
  }

  public static void deleteFile(String filename) {
    deleteFile(new File(filename));
  }

  public static void deleteFile(File file) {
    if (!file.exists())
      return;
    if (!file.delete())
      throw new RuntimeException("Could not delete '" + file.getAbsoluteFile() + "'");
    waitUntilFileDeleted(file);
  }

  private static void waitUntilFileDeleted(File file) {
    int i = 10;
    while (file.exists()) {
      if (--i <= 0) {
        System.out.println("Breaking out of delete wait");
        break;
      }
      try {
        Thread.sleep(500);
      }
      catch (InterruptedException e) {
      }
    }
  }

  public static String getFileContent(String path) throws Exception {
    File input = new File(path);
    return getFileContent(input);
  }

  public static String getFileContent(File input) throws Exception {
    return new String(getFileBytes(input));
  }

  public static byte[] getFileBytes(File input) throws Exception {
    long size = input.length();
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(input);
      byte[] bytes = new StreamReader(stream).readBytes((int) size);
      return bytes;
    } finally {
      if (stream != null)
        stream.close();
    }
  }

  public static LinkedList<String> getFileLines(String filename) throws Exception {
    return getFileLines(new File(filename));
  }

  public static LinkedList<String> getFileLines(File file) throws Exception {
    LinkedList<String> lines = new LinkedList<String>();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    while ((line = reader.readLine()) != null)
      lines.add(line);

    reader.close();
    return lines;
  }

  public static void writeLinesToFile(String filename, List<?> lines) throws Exception {
    writeLinesToFile(new File(filename), lines);
  }

  public static void writeLinesToFile(File file, List<?> lines) throws Exception {
    PrintStream output = new PrintStream(new FileOutputStream(file));
    for (Iterator<?> iterator = lines.iterator(); iterator.hasNext();) {
      String line = (String) iterator.next();
      output.println(line);
    }
    output.close();
  }

  public static void copyBytes(InputStream input, OutputStream output) throws Exception {
    StreamReader reader = new StreamReader(input);
    while (!reader.isEof())
      output.write(reader.readBytes(1000));
  }

  public static File createDir(String path) {
    makeDir(path);
    return new File(path);
  }

  public static File[] getDirectoryListing(File dir) {
    SortedSet<File> dirSet = new TreeSet<File>();
    SortedSet<File> fileSet = new TreeSet<File>();
    File[] files = dir.listFiles();
    if (files == null)
      return new File[0];
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory())
        dirSet.add(files[i]);
      else
        fileSet.add(files[i]);
    }
    List<File> fileList = new LinkedList<File>();
    fileList.addAll(dirSet);
    fileList.addAll(fileSet);
    return fileList.toArray(new File[]{});
  }

  public static String buildPath(String[] parts) {
    return StringUtil.join(Arrays.asList(parts), System.getProperty("file.separator"));
  }

  public static List<String> breakFilenameIntoParts(String fileName) {
    List<String> parts = new ArrayList<String>(Arrays.asList(fileName.split("/")));
    return parts;
  }

  public static String getPathOfFile(String fileName) {
    List<String> parts = breakFilenameIntoParts(fileName);
    parts.remove(parts.size()-1);
    return buildPath(parts.toArray(new String[parts.size()]));
  }

  public static void addItemsToClasspath(String classpathItems) throws Exception {
    final String separator = System.getProperty("path.separator");
    String currentClassPath = System.getProperty("java.class.path");
    System.setProperty("java.class.path", currentClassPath + separator + classpathItems);
    String[] items = classpathItems.split(separator);
    for (String item : items) {
      addFileToClassPath(item);
    }
  }

  private static void addFileToClassPath(String fileName) throws Exception {
    addUrlToClasspath(new File(fileName).toURI().toURL());
  }

  public static void addUrlToClasspath(URL u) throws Exception {
    URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<URLClassLoader> sysclass = URLClassLoader.class;
    Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
    method.setAccessible(true);
    method.invoke(sysloader, new Object[]{u});
  }
}
