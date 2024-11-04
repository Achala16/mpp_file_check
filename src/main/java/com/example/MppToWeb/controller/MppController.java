package com.example.MppToWeb.controller;

import com.example.MppToWeb.service.MppReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mpp")
public class MppController {

    @Autowired
    private MppReaderService mppReaderService;

    @PostMapping("/process")
    public ResponseEntity<String> processMppFile(@RequestParam("file") MultipartFile file) {
        try {
            // You can get the file path or content as needed
            String filePath = file.getOriginalFilename(); // This is just the name, adjust as necessary

            // You may want to save the file temporarily or directly process it
            mppReaderService.processMppFile(file); // Modify your service to accept MultipartFile

            return ResponseEntity.ok("MPP file processed successfully: " + filePath);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error processing MPP file: " + e.getMessage());
        }
    }
}
