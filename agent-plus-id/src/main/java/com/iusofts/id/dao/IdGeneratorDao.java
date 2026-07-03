package com.iusofts.id.dao;

import com.iusofts.id.entity.IdGenerator;
import org.springframework.stereotype.Repository;

@Repository
public interface IdGeneratorDao {

    IdGenerator getCurrentUid(Integer type);

    Integer updateIdGenerator(IdGenerator generator);
}