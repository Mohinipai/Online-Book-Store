package com.bookstore.service.impl;

import com.bookstore.exception.ApiException;
import com.bookstore.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private final Path fileStorageLocation;

    public FileUploadServiceImpl() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create the directory where the uploaded files will be stored.");
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        String originalFileName = org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());
        
        if(originalFileName.contains("..")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Sorry! Filename contains invalid path sequence " + originalFileName);
        }

        String fileExtension = "";
        try {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } catch(Exception e) {
            fileExtension = "";
        }
        
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(newFileName)
                    .toUriString();

            return fileDownloadUri;
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file " + newFileName + ". Please try again!");
        }
    }
}
