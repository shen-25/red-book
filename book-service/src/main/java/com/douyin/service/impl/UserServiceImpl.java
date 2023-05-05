package com.douyin.service.impl;

import com.douyin.bo.LoginBo;
import com.douyin.bo.RegisterBO;
import com.douyin.bo.UpdatedUserBO;
import com.douyin.enums.Sex;
import com.douyin.enums.UserInfoModifyType;
import com.douyin.enums.YesOrNo;
import com.douyin.exceptions.GraceException;
import com.douyin.grace.result.ResponseStatusEnum;
import com.douyin.mapper.UsersMapper;
import com.douyin.pojo.Users;
import com.douyin.service.UserService;
import com.douyin.utils.DateUtil;
import com.douyin.utils.DesensitizationUtil;
import com.douyin.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    public static final String USER_FACE1 = "http://192.168.0.107/images/user-default.png";

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
    public Users createUser(String mobile, String password) {
        String userId = sid.nextShort();
        Users user = new Users();
        user.setId(userId);

          if(StringUtils.isNotBlank(password)){
              String md5 = MD5Utils.getMD5Str(password);
              user.setPassword(md5);
          }
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

    @Override
    public Users getUser(String userId) {
        Users user = usersMapper.selectByPrimaryKey(userId);
        return user;
    }

    @Transactional
    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        if (type.equals(UserInfoModifyType.NICKNAME.type)) {
            criteria.andEqualTo("nickname", updatedUserBO.getNickname());
            Users users = usersMapper.selectOneByExample(example);
            if (users != null) {
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }
        if (type.equals(UserInfoModifyType.IMOOCNUM.type)) {
            criteria.andEqualTo("imoocNum", updatedUserBO.getImoocNum());
            Users users = usersMapper.selectOneByExample(example);
            if (users != null) {
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_IMOOCNUM_EXIST_ERROR);
            }
            Users tempUser = getUser(updatedUserBO.getId());
            if(tempUser.getCanImoocNumBeUpdated().equals(YesOrNo.NO.type)){
                GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_IMOOCNUM_ERROR);
            }
            updatedUserBO.setCanImoocNumBeUpdated(YesOrNo.NO.type);
        }

        return updateUserInfo(updatedUserBO);
    }

    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO) {
        Users pendingUser = new Users();
        BeanUtils.copyProperties(updatedUserBO, pendingUser);
        int cnt = usersMapper.updateByPrimaryKeySelective(pendingUser);
        if (cnt != 1) {
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }
        return getUser(updatedUserBO.getId());
    }

    @Override
    public Users getUserByPassword(LoginBo loginBo) throws NoSuchAlgorithmException {
        String md5 = MD5Utils.getMD5Str(loginBo.getPassword());
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("mobile", loginBo.getMobile());
        criteria.andEqualTo("password", md5);
        Users user = usersMapper.selectOneByExample(example);
        if (user == null) {
            GraceException.display(ResponseStatusEnum.USER_STATUS_ERROR);
        }
        return user;
    }

    @Override
    public void register(RegisterBO registerBO) {
        this.createUser(registerBO.getMobile(), registerBO.getPassword());

    }

}
