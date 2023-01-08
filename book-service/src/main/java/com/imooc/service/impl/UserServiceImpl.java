package com.imooc.service.impl;

import com.imooc.enums.Sex;
import com.imooc.enums.YesOrNo;
import com.imooc.mapper.UsersMapper;
import com.imooc.pojo.Users;
import com.imooc.service.UserService;
import com.imooc.utils.DateUtil;
import com.imooc.utils.DesensitizationUtil;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    public static final String USER_FACE1 = "http://192.168.0.100/images/user-default.png";

    @Autowired
    public Sid sid;

    @Autowired
    private UsersMapper usersMapper;
    /**
     * 判断用户是否存在
     *
     * @return
     */
    @Override
    public Users queryMobileIsExit(String mobile) {
        Users users = new Users();
        System.out.println(users.toString());
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("mobile", mobile);
        //这里添加的时example
        Users user = usersMapper.selectOneByExample(userExample);
        return user;
    }

    /**
     * 创建用户
     * @param mobile
     * @return
     */
    @Transactional
    @Override
    public Users createUser(String mobile) {
        UUID uuid = UUID.randomUUID();
        System.out.println(uuid.toString());
        String userId = sid.nextShort();
        Users user = new Users();
        user.setId(userId);

        user.setMobile(mobile);
        user.setNickname("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setImoocNum("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE1);

        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("这家伙很懒，什么都没留下~");
        user.setCanImoocNumBeUpdated(YesOrNo.YES.type);

        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());
        usersMapper.insert(user);
        return user;
    }
}
