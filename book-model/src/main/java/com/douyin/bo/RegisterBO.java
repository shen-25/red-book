package com.douyin.bo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class RegisterBO {

    @NotBlank(message = "手机号不能为空")
    @Length(min = 11, max = 11, message = "手机号长度不正确")
    private String mobile;


    @NotBlank(message = "验证码不能为空")
    @Length(min = 8, max = 18, message = "密码至少8位，不能超过18位")
    private String password;
}
