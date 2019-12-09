package com.leyou.upload.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UploadService {
    String upload(MultipartFile file);

    Map getSignature();
}
