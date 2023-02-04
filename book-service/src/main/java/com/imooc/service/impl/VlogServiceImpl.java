package com.imooc.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.base.BaseInfoProperties;
import com.imooc.mapper.MyLikedVlogMapper;
import com.imooc.mapper.VlogMapper;
import com.imooc.mapper.VlogMapperCustom;
import com.imooc.pojo.MyLikedVlog;
import com.imooc.pojo.Vlog;
import com.imooc.service.FanService;
import com.imooc.service.VlogService;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.IndexVlogVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {

    public static final String USER_FACE1 = "http://192.168.0.107/images/user-default.png";

    @Autowired
    public Sid sid;

    @Autowired
    private VlogMapperCustom vlogMapperCustom;

    @Autowired
    private VlogMapper vlogMapper;

    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;

    @Autowired
    private FanService fansService;

    /**
     * 查询首页的vlog
     */
    @Override
    public PagedGridResult getIndexVlogList(String userId, String search, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(search)) {
            map.put("search", search);
        }
        List<IndexVlogVO> indexVlogList = vlogMapperCustom.getIndexVlogList(map);
        for(IndexVlogVO indexVlogVO : indexVlogList){
            String vlogId = indexVlogVO.getVlogId();
            String vlogerId = indexVlogVO.getVlogerId();

            if (StringUtils.isNotBlank(userId)) {
                // 判断用户是否点赞过此视频
                indexVlogVO.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
                // 用户是否关注博主
                Boolean isLike = fansService.queryDoIFollowVloger(userId, vlogerId);
                indexVlogVO.setDoIFollowVloger(isLike);
            }
            //  获取视频点赞数
            indexVlogVO.setLikeCounts(getVlogBeLikeCounts(vlogId));
        }
        return setterPagedGrid(indexVlogList, page);
    }

    @Override
    public Integer getVlogBeLikeCounts(String vlogId) {
        Integer res = 1;
        String cnt = redis.get( REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        if (StringUtils.isNotBlank(cnt)) {
            res = Integer.valueOf(cnt);
        }
        return  res;
    }

    /**
     * 根据视频主键查询vlog
     *
     * @param vlogId
     */
    @Override
    public IndexVlogVO getVlogDetailById(String vlogId, String userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);
        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);
        if (list != null && !list.isEmpty()) {
            return setIndexVlogVO(list.get(0), userId);
        }
         return null;
    }

    private IndexVlogVO setIndexVlogVO(IndexVlogVO indexVlogVO, String userId){
        String vlogId = indexVlogVO.getVlogId();
        String vlogerId = indexVlogVO.getVlogerId();
        if (StringUtils.isNotBlank(userId)) {
            indexVlogVO.setDoIFollowVloger(true);
            // 判断用户是否点赞过此视频
            indexVlogVO.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
            // 用户是否关注博主
            Boolean isLike = fansService.queryDoIFollowVloger(userId, vlogerId);
            indexVlogVO.setDoIFollowVloger(isLike);
        }
        //  获取视频点赞数
        indexVlogVO.setLikeCounts(getVlogBeLikeCounts(vlogId));
        return indexVlogVO;
    }

    /**
     * 用户把视频改为公开或者私密
     */
    @Override
    public void changeToPrivateOrPublic(String userId , String vlogId, Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", vlogId);
        criteria.andEqualTo("vlogerId", userId);
        Vlog vlog = new Vlog();
        vlog.setIsPrivate(yesOrNo);
        vlogMapper.updateByExampleSelective(vlog, example);
    }



    /**
     * 查询用户的公开或者私密视频
     *
     * @param userId
     * @param page
     * @param pageSize
     * @param yesOrNo
     */
    @Override
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {

        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("isPrivate", yesOrNo);
        PageHelper.startPage(page, pageSize);
        List<Vlog> vlogList = vlogMapper.selectByExample(example);
        return setterPagedGrid(vlogList, page);
    }

    /**
     * 用户点赞视频
     *
     * @param userId
     * @param vlogId
     */
    @Override
    @Transactional
    public MyLikedVlog userlikeVlog(String userId, String vlogId) {
        String id = sid.nextShort();
        MyLikedVlog myLikedVlog = new MyLikedVlog();
        myLikedVlog.setId(id);
        myLikedVlog.setVlogId(vlogId);
        myLikedVlog.setUserId(userId);
        myLikedVlogMapper.insert(myLikedVlog);
        return myLikedVlog;
    }

    @Override
    @Transactional
    public void userUnlikeVlog(String userId, String vlogId) {
        MyLikedVlog myLikedVlog = new MyLikedVlog();
        myLikedVlog.setVlogId(vlogId);
        myLikedVlog.setUserId(userId);
        myLikedVlogMapper.delete(myLikedVlog);
    }

    public boolean doILikeVlog(String userId, String vlogId) {
        String isLike = redis.get(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + vlogId);
        return StringUtils.isNotBlank(isLike)
                && isLike.equalsIgnoreCase("1");
    }

    /**
     * 查询用户点赞过的短视频
     */
    @Override
    public PagedGridResult getMyLikeVlogList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(userId)) {
            map.put("userId", userId);
        }
        List<IndexVlogVO> myLikedVlogList = vlogMapperCustom.getMyLikedVlogList(map);
        return setterPagedGrid(myLikedVlogList, page);
    }

    /**
     * 查询用户关注的博主视频
     */
    @Override
    public PagedGridResult getMyFollowVlogList(String userId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(userId)) {
            map.put("myId", userId);
        }
        PageHelper.startPage(page, pageSize);
        List<IndexVlogVO> myFollowVlogList = vlogMapperCustom.getMyFollowVlogList(map);
        for(IndexVlogVO indexVlogVO : myFollowVlogList){
            String vlogId = indexVlogVO.getVlogId();
            if (StringUtils.isNotBlank(userId)) {
                indexVlogVO.setDoIFollowVloger(true);
                // 判断用户是否点赞过此视频
                indexVlogVO.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
            }
            //  获取视频点赞数
            indexVlogVO.setLikeCounts(getVlogBeLikeCounts(vlogId));
        }
        return setterPagedGrid(myFollowVlogList, page);
    }

    /**
     * 查询用户的朋友视频
     */
    @Override
    public PagedGridResult getMyFriendVlogList(String userId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(userId)) {
            map.put("myId", userId);
        }
        PageHelper.startPage(page, pageSize);
        List<IndexVlogVO> myFriendVlogList = vlogMapperCustom.getMyFriendVlogList(map);
        for(IndexVlogVO indexVlogVO : myFriendVlogList){
            String vlogId = indexVlogVO.getVlogId();
            if (StringUtils.isNotBlank(userId)) {
                indexVlogVO.setDoIFollowVloger(true);
                // 判断用户是否点赞过此视频
                indexVlogVO.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
            }
            //  获取视频点赞数
            indexVlogVO.setLikeCounts(getVlogBeLikeCounts(vlogId));
        }
        return setterPagedGrid(myFriendVlogList, page);
    }
}

