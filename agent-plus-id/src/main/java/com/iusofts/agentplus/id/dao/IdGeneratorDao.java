package com.iusofts.agentplus.id.dao;

import com.iusofts.agentplus.id.entity.IdGenerator;
import org.springframework.stereotype.Repository;

@Repository
public interface IdGeneratorDao {

    IdGenerator getCurrentUid(Integer type);

    Integer updateIdGenerator(IdGenerator generator);
}