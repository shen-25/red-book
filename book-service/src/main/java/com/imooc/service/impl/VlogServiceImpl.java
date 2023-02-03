package com.imooc.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.base.BaseInfoProperties;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.mapper.VlogMapper;
import com.imooc.mapper.VlogMapperCustom;
import com.imooc.pojo.Vlog;
import com.imooc.service.VlogService;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.IndexVlogVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {

    public static final String USER_FACE1 = "http://192.168.0.107/images/user-default.png";

    @Autowired
    public Sid sid;

    @Autowired
    private VlogMapperCustom vlogMapperCustom;

    @Autowired
    private VlogMapper vlogMapper;

    /**
     * 查询首页的vlog
     */
    @Override
    public PagedGridResult getIndexVlogList(String search, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(search)) {
            map.put("search", search);
        }
        List<IndexVlogVO> indexVlogList = vlogMapperCustom.getIndexVlogList(map);
        return setterPagedGrid(indexVlogList, page);
    }

    /**
     * 根据视频主键查询vlog
     *
     * @param vlogId
     */
    @Override
    public IndexVlogVO getVlogDetailById(String vlogId) {
        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);
        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
         return null;
    }

    /**
     * 用户把视频改为公开或者私密
     */
    @Override
    public void changeToPrivateOrPublic(String userId , String vlogId, Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", vlogId);
        criteria.andEqualTo("vlogerId", userId);
        Vlog vlog = new Vlog();
        vlog.setIsPrivate(yesOrNo);
        vlogMapper.updateByExampleSelective(vlog, example);
    }

    /**
     * 查询用户的公开或者私密视频
     *
     * @param userId
     * @param page
     * @param pageSize
     * @param yesOrNo
     */
    @Override
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {

        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("isPrivate", yesOrNo);
        PageHelper.startPage(page, pageSize);
        List<Vlog> vlogList = vlogMapper.selectByExample(example);
        return setterPagedGrid(vlogList, page);

    }
}

