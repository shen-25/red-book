package com.douyin.repository;

import com.douyin.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author word
 */
@Repository
public interface MessageRepository extends MongoRepository<MessageMO, String> {
    List<MessageMO> findAllByToUserIdOrderByCreateTimeDesc(String toId, Pageable pageable);
}
