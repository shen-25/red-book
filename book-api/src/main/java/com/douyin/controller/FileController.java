package com.douyin.controller;

import com.douyin.MinIOConfig;
import com.douyin.grace.result.GraceJSONResult;
import com.douyin.utils.MinIOUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author word
 */
@Slf4j
@Api(tags = "FileController视频上传接口")
@RestController("upload")
public class FileController {

    @Autowired
    private MinIOConfig minIOConfig;

    @ApiOperation(value = "用户视频上传接口")
    @PostMapping("video")
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
