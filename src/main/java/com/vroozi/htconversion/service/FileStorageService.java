package com.vroozi.htconversion.service;

import com.vroozi.htconversion.exception.FileStorageException;
import com.vroozi.htconversion.exception.MyFileNotFoundException;
import com.vroozi.htconversion.property.FileStorageProperties;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

  public static final String UTF8_BOM = "\uFEFF";
  private final Path fileStorageLocation;

  @Autowired
  public FileStorageService(FileStorageProperties fileStorageProperties) {
    this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
        .toAbsolutePath().normalize();

    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception ex) {
      throw new FileStorageException(
          "Could not create the directory where the uploaded files will be stored.", ex);
    }
  }

  private static String removeUTF8BOM(String line) {
    if (line.startsWith(UTF8_BOM)) {
      line = line.substring(1);
    }
    return line;
  }

  public String storeFile(MultipartFile file) {
    // Normalize file name
    String fileName = StringUtils.cleanPath(file.getOriginalFilename());

    try {
      // Check if the file's name contains invalid characters
      if (fileName.contains("..")) {
        throw new FileStorageException(
            "Sorry! Filename contains invalid path sequence " + fileName);
      }

      // Copy file to the target location (Replacing existing file with the same name)
      Path targetLocation = null;
      if (file.getOriginalFilename() == null) {
        targetLocation = this.fileStorageLocation.resolve(fileName);
      } else {
        targetLocation = Paths.get(file.getOriginalFilename());
      }
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      return fileName;
    } catch (IOException ex) {
      throw new FileStorageException("Could not store file " + fileName + ". Please try again!",
          ex);
    }
  }

  public String storeFile(String convertedFile, String fileName) {
    try {
      // Copy file to the target location (Replacing existing file with the same name)
      Path targetLocation = this.fileStorageLocation.resolve(fileName);
      InputStream convertedFileStream = new ByteArrayInputStream(
          convertedFile.getBytes(StandardCharsets.UTF_8));
      Files.copy(convertedFileStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
      try {
        //wait until the complete file is written //FIXME create a lock on the file to check the write process
        Thread.sleep(5000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return fileName;
    } catch (IOException ex) {
      throw new FileStorageException("Could not store file " + fileName + ". Please try again!",
          ex);
    }
  }

  public Resource loadFileAsResource(String fileName) {
    try {
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists()) {
        return resource;
      } else {
        throw new MyFileNotFoundException("File not found " + fileName);
      }
    } catch (MalformedURLException ex) {
      throw new MyFileNotFoundException("File not found " + fileName, ex);
    }
  }

  public String convertFile(MultipartFile htFile, MultipartFile mtFile) throws IOException {

    BufferedReader br;
    HashMap<String, String> htFileMap = new HashMap<>();
    StringBuilder outputFile = new StringBuilder();
    StringBuilder readLines = new StringBuilder();
    String line;
    String propertyPart[];

    InputStream is = htFile.getInputStream();
    br = new BufferedReader(new InputStreamReader(is, "UTF8"));
    while ((line = br.readLine()) != null) {
      line = removeUTF8BOM(line.trim());
      if (!StringUtils.isEmpty(line) && !line.startsWith("#")) {
        propertyPart = line.split("=");
        if (propertyPart.length > 1) {
          htFileMap.put(propertyPart[0], propertyPart[1].trim());
        } else {
          htFileMap.put(propertyPart[0], "");
        }
      }
    }
    int lineNo = 0;
    is = mtFile.getInputStream();
    br = new BufferedReader(new InputStreamReader(is));
    while ((line = br.readLine()) != null) {
      line = removeUTF8BOM(line.trim());
      if (!StringUtils.isEmpty(line) && !line.startsWith("#")) {
        propertyPart = line.split("=");
        lineNo++;
        if (htFileMap.containsKey(propertyPart[0]) && propertyPart.length > 1) {
          System.out.println("Key replaced at : " + lineNo + ": Value : " + propertyPart[0]);
          outputFile.append(line.replace(propertyPart[1], htFileMap.get(propertyPart[0])));
        } else {
          System.out.println("Key not replaced at : " + lineNo + ": Value : " + propertyPart[0]);
          outputFile.append(line);
        }
        readLines.append("\n");
        outputFile.append("\n");
      } else {
        outputFile.append(line).append("\n");
      }
    }
    System.out.println(readLines.toString());
    return outputFile.toString();
  }


}
