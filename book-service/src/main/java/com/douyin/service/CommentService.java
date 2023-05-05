package com.douyin.service;

import com.douyin.bo.CommentBO;
import com.douyin.pojo.Comment;
import com.douyin.utils.PagedGridResult;
import com.douyin.vo.CommentVO;

public interface CommentService {
    /**
     * 创建评论
     */
    public CommentVO createComment(CommentBO commentBO);


    /**
     * 视频的评论列表
     */
    public PagedGridResult getCommentList(String vlogId, String userId, Integer page, Integer pageSize);

    /**
     * 删除评论
     */
    public void delete(String userId, String commentId, String vlogId );

    /**
     * 点赞评论
     */
    public void like(String commentId, String userId);

    public Comment getComment(String id);

    /**
     * 取消点赞评论
     */
    public void unlike( String commentId, String userId);
}
