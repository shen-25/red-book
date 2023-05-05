package com.douyin.service.impl;

import com.github.pagehelper.PageHelper;
import com.douyin.base.BaseInfoProperties;
import com.douyin.base.RabbitMQConfig;
import com.douyin.enums.YesOrNo;
import com.douyin.mapper.FansMapper;
import com.douyin.mapper.FansMapperCuston;
import com.douyin.mo.MessageMO;
import com.douyin.pojo.Fans;
import com.douyin.service.FanService;
import com.douyin.service.MsgService;
import com.douyin.utils.JsonUtils;
import com.douyin.utils.PagedGridResult;
import com.douyin.vo.FansVO;
import com.douyin.vo.VlogerVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;

@Service
public class FanServiceImpl extends BaseInfoProperties implements FanService {
    /**
     * 关注
     */

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private FansMapperCuston fansMapperCuston;

    @Autowired
    private MsgService msgService;

    @Autowired
    private Sid sid;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doFollow(String userId, String vlogerId) {
        Fans fans = new Fans();
        String id = sid.nextShort();
        fans.setId(id);
        fans.setFanId(userId);
        fans.setVlogerId(vlogerId);
        //判断对方是否关注我
        Fans vloger = queryFansRelationShip(vlogerId, userId);
        if (vloger != null) {
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);
            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);
            fansMapper.updateByPrimaryKeySelective(vloger);
        }else{
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }
        fansMapper.insert(fans);
        MessageMO messageMO = new MessageMO();
        messageMO.setToUserId(vlogerId);
        messageMO.setFromUserId(userId);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg.follow",
                JsonUtils.objectToJson(messageMO));

    }

    public Fans queryFansRelationShip(String fanId, String vlogerId) {
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("fanId", fanId);
        criteria.andEqualTo("vlogerId", vlogerId);
        List<Fans> fansList = fansMapper.selectByExample(example);
        Fans fans = null;
        if (fansList != null && !fansList.isEmpty()) {
            fans =  fansList.get(0);
        }
        return fans;
    }

    /**
     * 取关
     *
     * @param userId
     * @param vlogerId
     */
    @Override
    @Transactional
    public void doCancel(String userId, String vlogerId) {
        //判断我们是否是朋友
        Fans fans = queryFansRelationShip(userId, vlogerId);
        if (fans != null && fans.getIsFanFriendOfMine().equals(YesOrNo.YES.type)) {
            Fans pendingFan = queryFansRelationShip(vlogerId, userId);
            pendingFan.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateByPrimaryKeySelective(pendingFan);
        }
        //删除我的fans表记录
        fansMapper.delete(fans);
    }

    /**
     * 查询用户是否关注博主
     *
     * @param userId
     * @param vlogerId
     */
    @Override
    public Boolean queryDoIFollowVloger(String userId, String vlogerId) {
        Fans fans = queryFansRelationShip(userId, vlogerId);
        return  fans != null;
    }

    @Override
    public PagedGridResult queryMyFollows(String userId, Integer page, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("myId", userId);
        PageHelper.startPage(page, pageSize);
        List<VlogerVO> vlogerVOList = fansMapperCuston.queryMyFollows(map);
        return setterPagedGrid(vlogerVOList, page);
    }

    /**
     * 我的粉丝列表
     */
    @Override
    public PagedGridResult queryMyFans(String userId, Integer page, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("myId", userId);

        PageHelper.startPage(page, pageSize);
        List<FansVO> fansVOList = fansMapperCuston.queryMyFans(map);
        /**
         * 高端做法：
         * 使用redis
         */
        for (FansVO fansVO : fansVOList) {
            //判断  他关注了我， 我有没有关注他？
            String r = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + userId + ":" + fansVO.getFanId());
            if (StringUtils.isNoneBlank(r) && r.equalsIgnoreCase("1")) {
                fansVO.setFriend(true);
            }
        }
        return setterPagedGrid(fansVOList, page);
    }
}
