package com.amplesky.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

public class CommonUtil {
    /**
     * 分布式系统中，有一些需要使用全局唯一ID的场景，
     * 有些时候我们希望能使用一种简单一些的ID，并且希望ID能够按照时间有序生成。
     * Twitter的Snowflake 算法就是这种生成器
     * @return
     */
    public static Long getDistributedId(){
        Snowflake snowflake = IdUtil.createSnowflake(1, 1);
        long id = snowflake.nextId();
        return id;
    }
}
