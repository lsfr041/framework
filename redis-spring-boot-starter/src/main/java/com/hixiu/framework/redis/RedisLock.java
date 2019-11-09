/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisLock
 * Author:   程建乐
 * Date:     2019/9/1 0:51
 * Description: RedisLock
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

/**
 * 〈一句话功能简述〉<br> 
 * 〈RedisLock〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public @interface RedisLock {
    /**
     * EnableRedisConfiguration 注解中的 value, apollo 中的 namespace
     */
    String value() default "";

    /**
     * 分布式锁 key, 支持spl表达式, 默认 package.class.method
     */
    String key() default "";

    /**
     * 默认超时时间，单位：毫秒
     */
    int expire() default 60000;

    /**
     * 获取锁失败处理逻辑
     */
    @Deprecated
    FailAction failAction() default FailAction.THROW_EXCEPTION;

    enum FailAction {
        RETURN_NULL,
        THROW_EXCEPTION
    }
}
