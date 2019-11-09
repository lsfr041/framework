/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisException
 * Author:   程建乐
 * Date:     2019/9/1 0:50
 * Description: RedisException
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;


/**
 * 〈一句话功能简述〉<br> 
 * 〈RedisException〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class RedisException extends RuntimeException {

    public RedisException() {
        this("Redis服务异常");
    }

    public RedisException(String message) {
        super(message);
    }

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }

}
