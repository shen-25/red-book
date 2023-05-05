package com.douyin.service.impl;

import com.douyin.base.BaseInfoProperties;
import com.douyin.base.RabbitMQConfig;
import com.douyin.bo.VlogBO;
import com.douyin.enums.MessageEnum;
import com.douyin.enums.YesOrNo;
import com.douyin.mapper.MyLikedVlogMapper;
import com.douyin.mapper.VlogMapper;
import com.douyin.mapper.VlogMapperCustom;
import com.douyin.mo.MessageMO;
import com.douyin.pojo.MyLikedVlog;
import com.douyin.pojo.Vlog;
import com.douyin.service.FanService;
import com.douyin.service.MsgService;
import com.douyin.service.VlogService;
import com.douyin.utils.JsonUtils;
import com.douyin.utils.PagedGridResult;
import com.douyin.vo.IndexVlogVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
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


    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 新增vlog视频
     */

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createVlog(VlogBO vlogBO) {

        String vid = sid.nextShort();

        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBO, vlog);

        vlog.setId(vid);

        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type);

        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());

        vlogMapper.insert(vlog);
    }


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
        Vlog vlog = getVlog(vlogId);
        Map<String, Object> msgContent = new HashMap<>();
        msgContent.put("vlogId", vlogId);
        msgContent.put("vlogCover", vlog.getCover());

        // MQ异步解耦
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(vlog.getVlogerId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.LIKE_VLOG.enValue,
                JsonUtils.objectToJson(messageMO));
        return myLikedVlog;
    }

    @Override
    public Vlog getVlog(String vlogId) {
        return vlogMapper.selectByPrimaryKey(vlogId);
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

