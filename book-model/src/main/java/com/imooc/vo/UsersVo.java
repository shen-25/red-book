package com.imooc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersVo {
    private String id;
    private String mobile;
    private String nickname;
    private String imoocNum;

    private String face;


    private Integer sex;

    private Date birthday;

    private String country;

    private String province;


    private String city;


    private String district;


    private String description;


    private String bgImg;


    private Integer canImoocNumBeUpdated;


    private Date createdTime;


    private Date updatedTime;

    /**
     * 用户token
     */
    private String userToken;


}