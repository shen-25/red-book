package com.douyin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
//全构造函数
@AllArgsConstructor
@NoArgsConstructor
public class Student {

    private String name;
    private Integer age;


}


