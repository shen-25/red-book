package com.douyin.service;

import com.douyin.bo.LoginBo;
import com.douyin.bo.RegisterBO;
import com.douyin.bo.UpdatedUserBO;
import com.douyin.pojo.Users;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;

public interface UserService {

    /**
     * 判断用户是否存在
     * @return
     */
    public Users queryMobileIsExit(String mobile);


    @Transactional
    Users createUser(String mobile, String password);

    public Users getUser(String userId);


    public Users updateUserInfo(UpdatedUserBO updatedUserBO);

    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type);

    Users getUserByPassword(LoginBo loginBo) throws NoSuchAlgorithmException;

    void register(RegisterBO registerBO);
}
