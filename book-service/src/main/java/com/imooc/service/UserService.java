package com.imooc.service;

import com.imooc.pojo.Users;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {

    /**
     * 判断用户是否存在
     * @return
     */
    public Users queryMobileIsExit(String mobile);

    public Users createUser(String mobile);
}
