package com.imooc.service.impl;

import com.imooc.enums.YesOrNo;
import com.imooc.mapper.FansMapper;
import com.imooc.pojo.Fans;
import com.imooc.service.FanService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class FanServiceImpl implements FanService {
    /**
     * 关注
     */

    @Autowired
    private FansMapper fansMapper;


    @Autowired
    private Sid sid;

    @Override
    @Transactional
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
            fansMapper.insert(fans);
        }


    }

    public Fans queryFansRelationShip(String fanId, String vlogerId) {
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", fanId);
        criteria.andEqualTo("fanId", vlogerId);
        List<Fans> fansList = fansMapper.selectByExample(example);
        Fans fans = null;
        if (fansList != null && !fansList.isEmpty()) {
            fans =  fansList.get(0);
        }
        return fans;
    }
}
