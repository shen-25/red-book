package com.imooc.service;

import com.imooc.utils.PagedGridResult;

public interface FanService {

    /**
     * 关注
     * @param userId
     * @param vlogerId
     */
    public void doFollow(String userId, String vlogerId);

    /**
     * 取关
     */
    public void doCancel(String userId, String vlogerId);

    /**
     * 查询用户是否关注博主
     */
    public Boolean queryDoIFollowVloger(String userId, String vlogerId);

    /**
     * 我的关注列表
     */
    public PagedGridResult queryMyFollows(String userId, Integer page, Integer pageSize);


    /**
     * 我的粉丝列表
     */
    public PagedGridResult queryMyFans(String userId, Integer page, Integer pageSize);


}
