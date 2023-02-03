package com.imooc.service;

import com.imooc.utils.PagedGridResult;
import com.imooc.vo.IndexVlogVO;

public interface VlogService {

    /**
     * 查询首页的vlog
     */
    public PagedGridResult getIndexVlogList(String search, Integer page, Integer pageSize);

    /**
     * 根据视频主键查询vlog
     */
    public IndexVlogVO getVlogDetailById(String vlogId);

    /**
     * 用户把视频改为公开或者私密
     */
    public void changeToPrivateOrPublic(String userId , String vlogId, Integer yesOrNo);

    /**
     * 查询用户的公开或者私密视频
     */
    public PagedGridResult queryMyVlogList(String userId ,Integer page, Integer pageSize, Integer yesOrNo);

}
