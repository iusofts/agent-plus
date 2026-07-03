package com.iusofts.agentplus.id.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.iusofts.agentplus.id.dao.IdGeneratorDao;
import com.iusofts.agentplus.id.entity.IdGenerator;
import com.iusofts.agentplus.id.exception.IdGenerationException;
import com.iusofts.agentplus.id.service.IdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * 自增加id生成
 *
 * @author Ivan
 */
@DS("boss")
@Service
public class IdServiceImpl implements IdService {

    @Autowired
    private IdGeneratorDao idDao;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Integer generateUid(UidTypeEnum typeEnum) {

        if (null == typeEnum) {
            throw new IdGenerationException(null, "类型为空");
        }

        //最终返回的ID值
        Integer uid;

        /**
         * 查询当前类型ID当前值
         */
        IdGenerator currentUid = idDao.getCurrentUid(typeEnum.getType());

        // 如果没有数据,提示对应类型数据不存在
        if (null == currentUid) {
            throw new IdGenerationException(null, "对应类型的id数据不存在");
        }

        // 随机追加
        int incrNum = getRandomNumberInRange(currentUid.getStepMin(), currentUid.getStepMax());

        uid = currentUid.getUid() + incrNum;
        currentUid.setUid(uid);
        idDao.updateIdGenerator(currentUid);

        return uid;
    }


    public int getRandomNumberInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("最小值不能大于最大值");
        }
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

}
