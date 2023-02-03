package com.imooc.controller;

import com.imooc.MinIOConfig;
import com.imooc.base.BaseInfoProperties;
import com.imooc.bo.UpdatedUserBO;
import com.imooc.enums.FileTypeEnum;
import com.imooc.enums.UserInfoModifyType;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Users;
import com.imooc.service.UserService;
import com.imooc.utils.MinIOUtils;
import com.imooc.vo.UsersVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author word
 */
@Slf4j
@Api(tags = "UserInfoController 用户信息接口")
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    private UserService userService;

    @Autowired
    private MinIOConfig minIOConfig;

    @ApiOperation(value = "用户信息查询")
    @GetMapping("/query")
    public GraceJSONResult query(@RequestParam String userId) {
        Users user = userService.getUser(userId);
        UsersVO UsersVO = new UsersVO();
        BeanUtils.copyProperties(user, UsersVO);
        // 我的关注博主总数量
        String myFollowsCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        // 我的粉丝总数
        String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        // 用户获赞总数，视频博主（点赞/喜欢）总和
        // 这条永远不存在
        // String likedVlogCountsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + userId);
        String likedVlogerCountsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);

        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer likedVlogCounts = 0;
        Integer likedVlogerCounts = 0;
        Integer totalLikeMeCounts = 0;

        if (StringUtils.isNotBlank(myFollowsCountsStr)) {
            myFollowsCounts = Integer.valueOf(myFollowsCountsStr);
        }
        if (StringUtils.isNotBlank(myFansCountsStr)) {
            myFansCounts = Integer.valueOf(myFansCountsStr);
        }

        if (StringUtils.isNotBlank(likedVlogerCountsStr)) {
            likedVlogerCounts = Integer.valueOf(likedVlogerCountsStr);
        }
        totalLikeMeCounts = likedVlogerCounts + likedVlogCounts;

        UsersVO.setMyFansCounts(myFansCounts);
        UsersVO.setMyFollowsCounts(myFollowsCounts);
        UsersVO.setTotalLikeMeCounts(totalLikeMeCounts);

        return GraceJSONResult.ok(UsersVO);
    }

    @ApiOperation(value = "用户信息修改")
    @PostMapping("/modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO, @RequestParam Integer type) {
        UserInfoModifyType.checkUserInfoTypeIsRight(type);
        Users updateUserInfo = userService.updateUserInfo(updatedUserBO, type);
        return GraceJSONResult.ok(updateUserInfo);
    }



    @ApiOperation(value = "这是用户信息修改图片路由")
    @PostMapping("modifyImage")
    public GraceJSONResult modifyImage(@RequestParam  String userId, @RequestParam Integer type,
            MultipartFile file) throws Exception {
        if (!type.equals(FileTypeEnum.BGIMG.type) && !type.equals(FileTypeEnum.FACE.type)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        String fileName = file.getOriginalFilename();
        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                fileName, file.getInputStream());
        String imgUrl = minIOConfig.getFileHost() + "/" + minIOConfig.getBucketName() + "/" + fileName;
        UpdatedUserBO updatedUserBO = new UpdatedUserBO();
        log.info("用户图片地址 {}", imgUrl);
        updatedUserBO.setId(userId);
        if (type.equals(FileTypeEnum.BGIMG.type)) {
            updatedUserBO.setBgImg(imgUrl);
        }else{
            updatedUserBO.setFace(imgUrl);
        }
        Users user = userService.updateUserInfo(updatedUserBO);
        return GraceJSONResult.ok(user);
    }
}
