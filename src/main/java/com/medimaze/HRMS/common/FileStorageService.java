package com.medimaze.HRMS.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public ApiResponse<String> storeFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.error("Cannot store empty file");
        }

        try {
            String targetFolderPath = uploadDir + File.separator + (subDir != null ? subDir : "misc");
            Path uploadPath = Paths.get(targetFolderPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String cleanFilename = System.currentTimeMillis() + "_" + (originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "file");
            Path filePath = uploadPath.resolve(cleanFilename);
            Files.copy(file.getInputStream(), filePath);

            String relativeUrl = "/" + targetFolderPath.replace(File.separator, "/") + "/" + cleanFilename;
            return ApiResponse.success("File uploaded successfully", relativeUrl);
        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to store file: " + e.getMessage());
        }
    }
}
