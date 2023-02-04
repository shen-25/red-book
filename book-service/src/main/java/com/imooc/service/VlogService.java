package com.imooc.service;

import com.imooc.pojo.MyLikedVlog;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.IndexVlogVO;

public interface VlogService {

    /**
     * 查询首页的vlog
     */
    public PagedGridResult getIndexVlogList(String userId,String search, Integer page, Integer pageSize);

    Integer getVlogBeLikeCounts(String vlogId);

    /**
     * 根据视频主键查询vlog
     */
    public IndexVlogVO getVlogDetailById(String vlogId, String userId);

    /**
     * 用户把视频改为公开或者私密
     */
    public void changeToPrivateOrPublic(String userId , String vlogId, Integer yesOrNo);

    /**
     * 查询用户的公开或者私密视频
     */
    public PagedGridResult queryMyVlogList(String userId ,Integer page, Integer pageSize, Integer yesOrNo);

    /**
     * 用户点赞视频
     */
    public MyLikedVlog userlikeVlog(String userId, String vlogId);

    /**
     * 用户取消点赞视频
     */
    public void userUnlikeVlog(String userId, String vlogId);

    /**
     * 查询用户点赞过的短视频
     */
    public PagedGridResult getMyLikeVlogList(String userId ,Integer page, Integer pageSize);

    /**
     * 查询用户关注的博主视频
     */
    public PagedGridResult getMyFollowVlogList(String userId ,Integer page, Integer pageSize);

    /**
     * 查询用户的朋友视频
     */
    public PagedGridResult getMyFriendVlogList(String userId ,Integer page, Integer pageSize);

}
