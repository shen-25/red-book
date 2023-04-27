package com.imooc.mapper;

import com.imooc.vo.FansVO;
import com.imooc.vo.VlogerVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FansMapperCuston {

    public List<VlogerVO> queryMyFollows(@Param("paramMap") Map<String, Object> map);
    public List<FansVO> queryMyFans(@Param("paramMap") Map<String, Object> map);
}