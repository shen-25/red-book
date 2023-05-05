package com.douyin.bo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class LoginBo {
    @NotBlank(message = "手机号不能为空")
    @Length(min = 11, max = 11, message = "手机号长度不正确")
    private String mobile;


    @NotBlank(message = "密码不能为空")
    private String password;

}
