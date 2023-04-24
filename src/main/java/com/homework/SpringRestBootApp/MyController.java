package com.homework.SpringRestBootApp;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Paths;
import org.springframework.http.MediaType;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import java.nio.file.StandardCopyOption;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


@RestController
public class MyController {
   

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
        
    }
//http://localhost:8080/browse?subFolder=test
 // Endpoint for browsing the shared folder and its sub-folders
@GetMapping("/browse")
public ResponseEntity<List<String>> browseFolder(@RequestParam(value = "subFolder", required = false) String subFolder) {
    // Create a list to hold the names of files and folders in the shared folder
    List<String> fileNames = new ArrayList<>();
    
    // Create a File object representing the shared folder
    File folder;
    if (subFolder == null || subFolder.isEmpty()) {
        folder = new File("SharedFolder");
    } else {
        folder = new File("SharedFolder\\" + subFolder);
    }
    
    // If the folder exists and is a directory, list its contents
    if (folder.exists() && folder.isDirectory()) {
        File[] files = folder.listFiles();
        if (files != null) {
            // Loop through the files and folders in the shared folder
            for (File file : files) {
                fileNames.add(file.getName());
            }
        }
        
        // Return the list of file and folder names as a response
        return new ResponseEntity<>(fileNames, HttpStatus.OK);
    } else {
        // If the folder does not exist or is not a directory, return a 404 Not Found response
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}


    

    //http://localhost:8080/rename?oldpath=subfolder/file.txt&newpath=newfile.txt
//if u want it to stay in the same folder u have to give the name of the subfolder
// Endpoint for renaming a file or folder in the shared folder
@PutMapping("/rename")
public ResponseEntity<String> renameFile(@RequestParam("oldpath") String filePath,
                                          @RequestParam("newpath") String newName) {

    // Get the path of the shared folder
    String sharedFolderPath = "SharedFolder";

    // Construct the old file path
    String oldPath = sharedFolderPath + "/" + filePath;
    
    // Construct the new file path
    String newPath = sharedFolderPath + "/" + newName;

    // Create a File object for the old file path
    File oldFile = new File(oldPath);

    // Create a File object for the new file path
    File newFile = new File(newPath);

    // Rename the file
    if (oldFile.renameTo(newFile)) {
        return new ResponseEntity<>("File renamed successfully", HttpStatus.OK);
    } else {
        return new ResponseEntity<>("Failed to rename file", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


//http://localhost:8080/delete?path=myfile.txt
// Endpoint for deleting a file or folder from the shared folder
@DeleteMapping("/delete")
public void delete(@RequestParam("path") String itemName) {
    // Get the path of the shared folder
    String sharedFolderPath = "SharedFolder";
    
    // Delete the file/folder recursively
    boolean success = deleteRecursive(sharedFolderPath, itemName);

    // Print the result of the deleting operation
    if (success) {
        System.out.println("Deleted file/folder successfully");
    } else {
        System.out.println("Failed to delete file/folder");
    }
}

// Recursively delete a file or folder in the shared folder
private boolean deleteRecursive(String path, String itemName) {
    // Create a File object for the current item
    File item = new File(path + "\\" + itemName);

    // Check if the current item exists
    if (!item.exists()) {
        return false;
    }

    // If the current item is a file, delete it and return
    if (item.isFile()) {
        return item.delete();
    }

    // If the current item is a directory, recursively delete its contents and then delete the directory
    File[] contents = item.listFiles();
    if (contents != null) {
        for (File f : contents) {
            if (!deleteRecursive(item.getAbsolutePath(), f.getName())) {
                return false;
            }
        }
    }

    return item.delete();
}


// Endpoint for downloading a file from the shared folder
@GetMapping("/download")
public ResponseEntity<Resource> download(@RequestParam("path") String path) throws IOException {
    String downloadPath = "LocalFolder\\";
    path = "SharedFolder\\" + path;
    Path file = Paths.get(path);
    FileSystemResource resource = new FileSystemResource(file);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename());
    Path downloadFile = Paths.get(downloadPath, resource.getFilename());
    
    try (InputStream is = resource.getInputStream();
         OutputStream os = new FileOutputStream(downloadFile.toFile())) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
    }

    return ResponseEntity.ok()
            .headers(headers)
            .contentLength(resource.contentLength())
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .body(resource);
}

// Endpoint for uploading a file to the shared folder from the local folder
@PostMapping("/upload")
public ResponseEntity<String> upload(@RequestParam("localpath") String localPath, @RequestParam(value = "sharedpath", required = false) String sharedPath) throws IOException {
    Path sourceFile = Paths.get("LocalFolder\\" + localPath);

    if (sharedPath == null || sharedPath.isEmpty()) {
        sharedPath = "SharedFolder\\" + sourceFile.getFileName();
    } else {
        sharedPath = "SharedFolder\\" + sharedPath + "\\" + sourceFile.getFileName();
    }

    Path destinationFile = Paths.get(sharedPath);
    
    try (InputStream is = new FileInputStream(sourceFile.toFile());
         OutputStream os = new FileOutputStream(destinationFile.toFile())) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
    }

    return ResponseEntity.ok("File uploaded successfully!");
}




 // Endpoint for OpenAPI contract generation
 @Bean
 public GroupedOpenApi api() {
     return GroupedOpenApi.builder()
         .group("spring-boot-rest-api")
         .pathsToMatch("/browse/**", "/download/**", "/upload", "/delete/**")
         .build();
 }



 
}
