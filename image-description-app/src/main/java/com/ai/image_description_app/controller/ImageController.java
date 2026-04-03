package com.ai.image_description_app.controller;

import java.io.File;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai.image_description_app.service.AIImageDescriptionService;

@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    private AIImageDescriptionService aiImageDescriptionService;

    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file) {

        try {

            // Create uploads folder path - go to parent directory where uploads folder is located
            String uploadDir = Paths.get(System.getProperty("user.dir")).getParent().resolve("uploads").toString();

            File directory = new File(uploadDir);

            // Create folder if it doesn't exist
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new RuntimeException("Failed to create uploads directory: " + uploadDir);
                }
            }

            // Get image name
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new RuntimeException("Invalid file name");
            }

            // Destination file
            File dest = new File(directory, fileName);

            // Save image
            file.transferTo(dest);

            // Call AI service to generate description
            String description = aiImageDescriptionService.generateDescription(dest);

            // Return the generated caption (frontend shows this text)
            return description;

        } catch (Exception e) {
            // Return error response
            return "AI description generation failed";
        }
    }
}