package com.imooc.controller;

import com.imooc.MinIOConfig;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.utils.MinIOUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author word
 */
@Slf4j
@Api(tags = "FileController 文件上次测试接口")
@RestController
public class FileController {

    @Autowired
    private MinIOConfig minIOConfig;

    @ApiOperation(value = "这是文件测试路由")
    @PostMapping("fileUpload")
    public GraceJSONResult uploadFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                fileName, file.getInputStream());
        String imgUrl = minIOConfig.getFileHost()
                + "/" + minIOConfig.getBucketName()
                + "/" + fileName;
        return GraceJSONResult.ok(imgUrl);
    }


}
