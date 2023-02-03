package com.imooc.controller;

import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.YesOrNo;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.MyLikedVlog;
import com.imooc.service.VlogService;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.IndexVlogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "VlogController  短视频相关业务功能接口")
@RestController
@RequestMapping("vlog")
public class VlogController extends BaseInfoProperties {

    @Autowired
    private VlogService vlogService;

    @ApiOperation(value = "获取首页推荐路由")
    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "") String userId,
                                     @RequestParam(defaultValue = "") String search,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult PagedGridResult = vlogService.getIndexVlogList(userId, search, page, pageSize);
        return GraceJSONResult.ok(PagedGridResult);
    }

    @ApiOperation(value = "根据主键查询vlog")
    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId,
                                  @RequestParam String vlogId) {
        IndexVlogVO vlogDetailById = vlogService.getVlogDetailById(vlogId);
        return GraceJSONResult.ok(vlogDetailById);

    }

    @ApiOperation(value = "视频改为私密")
    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId,
                                  @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId, vlogId, YesOrNo.YES.type);
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "视频改为公开")
    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId,
                                           @RequestParam String vlogId) {

        vlogService.changeToPrivateOrPublic(userId, vlogId, YesOrNo.NO.type);
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "查询用户公开视频列表")
    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize
                                          ) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.NO.type);
        return GraceJSONResult.ok(pagedGridResult);
    }

    @ApiOperation(value = "查询用户私密视频列表")
    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.YES.type);
        return GraceJSONResult.ok(pagedGridResult);
    }

    @ApiOperation(value = "用户点赞视频")
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId,
                                         @RequestParam String vlogerId,
                                         @RequestParam String vlogId) {

       vlogService.userlikeVlog(userId, vlogId);

        // 点赞后，视频和博主的获赞都加 1
        redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);

        redis.set(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + vlogId, "1");
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "用户取消点赞视频")
    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId,
                                @RequestParam String vlogerId,
                                @RequestParam String vlogId) {

        vlogService.userUnlikeVlog(userId, vlogId);

        // 取消点赞后，视频和博主的获赞都减 1
        redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);

        redis.del(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + vlogId);
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "获取视频最新的点赞数")
    @PostMapping("totalLikedCounts")
    public GraceJSONResult totalLikedCounts(
                                  @RequestParam String vlogId) {

        Integer vlogBeLikeCounts = vlogService.getVlogBeLikeCounts(vlogId);
        return GraceJSONResult.ok(vlogBeLikeCounts);
    }


    @ApiOperation(value = "获取用户点赞的视频")
    @GetMapping("myLikedList")
    public GraceJSONResult getMyLikeVlogList(@RequestParam String userId,
                                             @RequestParam Integer page,
                                             @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult myLikeVlogList = vlogService.getMyLikeVlogList(userId, page, pageSize);
        return GraceJSONResult.ok(myLikeVlogList);
    }

}
