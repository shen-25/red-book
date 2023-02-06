package com.imooc.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.base.BaseInfoProperties;
import com.imooc.bo.CommentBO;
import com.imooc.enums.MessageEnum;
import com.imooc.mapper.CommentMapper;
import com.imooc.mapper.CommentMapperCustom;
import com.imooc.pojo.Comment;
import com.imooc.pojo.Vlog;
import com.imooc.service.CommentService;
import com.imooc.service.MsgService;
import com.imooc.service.VlogService;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentMapperCustom commentMapperCustom;

    @Autowired
    private Sid sid;

    @Autowired
    private MsgService msgService;

    @Autowired
    private VlogService vlogService;

    @Override
    @Transactional
    public CommentVO createComment(CommentBO commentBO) {
        String id = sid.nextShort();
        Comment comment = new Comment();
        comment.setId(id);
        BeanUtils.copyProperties(commentBO, comment);
        comment.setCreateTime(new Date());
        comment.setLikeCounts(0);
        commentMapper.insert(comment);
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        // 系统消息：评论/回复
        Vlog vlog = vlogService.getVlog(commentBO.getVlogId());
        Map<String, Object> msgContent = new HashMap<>();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", id);
        msgContent.put("commentContent", commentBO.getContent());
        Integer type = MessageEnum.COMMENT_VLOG.type;
        if (StringUtils.isNotBlank(commentBO.getFatherCommentId()) &&
                !commentBO.getFatherCommentId().equalsIgnoreCase("0") ) {
            type = MessageEnum.REPLY_YOU.type;
        }

        msgService.createMsg(commentBO.getCommentUserId(),
                commentBO.getVlogerId(),
                type,
                msgContent);

        msgService.createMsg(commentBO.getCommentUserId(), comment.getVlogerId(), type, msgContent );
        return commentVO;
    }

    @Override
    public PagedGridResult getCommentList(String vlogId, String userId, Integer page, Integer pageSize) {
        Map<String, Object> map =new HashMap<>();
        map.put("vlogId", vlogId);
        PageHelper.startPage(page, pageSize);
        List<CommentVO> commentList = commentMapperCustom.getCommentList(map);
        for (CommentVO cv : commentList) {
            String commentId = cv.getCommentId();
            String countStr = redis.getHashValue(REDIS_VLOG_COMMENT_COUNTS, commentId);
            Integer counts = 0;
            if (StringUtils.isNotBlank(countStr)) {
                counts = Integer.valueOf(countStr);
            }
            cv.setLikeCounts(counts);
            String isLike = redis.hget(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
            Integer like = 0;
            if (StringUtils.isNotBlank(isLike) && isLike.equalsIgnoreCase("1")) {
                like = 1;
            }
            cv.setIsLike(like);

        }
        return setterPagedGrid(commentList, page);
    }

    /**
     * 删除评论
     */
    @Override
    public void delete(String userId, String commentId, String vlogId) {
        Comment comment = new Comment();
        comment.setCommentUserId(userId);
        comment.setId(commentId);
        comment.setVlogId(vlogId);
        commentMapper.delete(comment);
        redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
    }

    @Override
    public void like(String commentId, String userId) {
        redis.incrementHash(REDIS_VLOG_COMMENT_COUNTS, commentId, 1);
        redis.setHashValue(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId, "1");
        // 系统消息：点赞评论
        Comment comment = getComment(commentId);
        Vlog vlog = vlogService.getVlog(comment.getVlogId());
        Map<String, Object> msgContent = new HashMap<>();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", commentId);
        msgService.createMsg(userId,
                comment.getCommentUserId(),
                MessageEnum.LIKE_COMMENT.type,
                msgContent);
    }

    @Override
    public Comment getComment(String id) {
        return commentMapper.selectByPrimaryKey(id);
    }

    @Override
    public void unlike( String commentId,   String userId) {
        redis.decrementHash(REDIS_VLOG_COMMENT_COUNTS, commentId, 1);
        redis.hdel(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
    }


}
