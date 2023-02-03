package com.imooc.service;

public interface FanService {

    /**
     * 关注
     * @param userId
     * @param vlogerId
     */
    public void doFollow(String userId, String vlogerId);

}
