package com.example.reggie.controller;

import com.example.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String path;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        log.info(originalFileName);
        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        log.info(suffix);
        String fileName = UUID.randomUUID().toString() + suffix;
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(path + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response, String name) {
        try {
            FileInputStream fileInputStream = new FileInputStream(path + name);
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jepg");
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
