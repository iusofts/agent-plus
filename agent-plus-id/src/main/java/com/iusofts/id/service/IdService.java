package com.iusofts.id.service;

/**
 * 统一id生成器
 */
public interface IdService {

    /**
     * 生成ID
     *
     * @return 主键id
     */
    Integer generateUid(UidTypeEnum typeEnum);

    /**
     * ID类型枚举，
     */
    enum UidTypeEnum {
        
        CHAT(1, "Chat"),
        FLOW(2, "Flow"),
        ;

        /**
         * 类型
         */
        Integer type;

        /**
         * 描述
         */
        String desc;

        UidTypeEnum(Integer type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public Integer getType() {
            return type;
        }

    }
}
