package com.imooc.controller;

import com.imooc.base.BaseInfoProperties;
import com.imooc.bo.CommentBO;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.service.CommentService;
import com.imooc.service.VlogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Api(tags = "评论业务接口")
@RestController
@RequestMapping("comment")
public class CommentController extends BaseInfoProperties {

    @Autowired
    private CommentService commentService;


    @ApiOperation("创建评论")
    @PostMapping("create")
    public GraceJSONResult create(@Valid @RequestBody CommentBO commentBO) {
        return GraceJSONResult.ok(commentService.createComment(commentBO));
    }

    @ApiOperation("视频评论总数")
    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId) {
        String counts = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);
        Long res = 0L;
        if (StringUtils.isNotBlank(counts)) {
            res = Long.valueOf(counts);
        }
        return GraceJSONResult.ok(res);
    }

    @ApiOperation("视频评论列表")
    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String vlogId,
                                @RequestParam(defaultValue = "") String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        return GraceJSONResult.ok(commentService.getCommentList(vlogId, userId, page, pageSize));
    }


    @ApiOperation("博主删除自己视频的评论")
    @DeleteMapping("delete")
    public GraceJSONResult delete(@RequestParam String commentUserId,
                                  @RequestParam String commentId,
                                  @RequestParam String vlogId){
        commentService.delete(commentUserId, commentId, vlogId);

        return GraceJSONResult.ok();
    }

    @ApiOperation("点赞评论")
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String commentId,
                                @RequestParam String userId){

        commentService.like(commentId, userId);
        return GraceJSONResult.ok();
    }

    @ApiOperation("取消点赞评论")
    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String commentId,
                                @RequestParam String userId){
        commentService.unlike(commentId, userId);
        return GraceJSONResult.ok();
    }


}
