/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: TraceContext
 * Author:   程建乐
 * Date:     2019/8/31 22:55
 * Description: 2
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 〈一句话功能简述〉<br> 
 * 〈TraceContext〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
public class TraceContext {
    private final static Logger LOGGER = LoggerFactory.getLogger(TraceContext.class);

    public static final String DUBBO_CONTEXT = "dubbo.context";
    public static final Long MAX_LENGTH = 2 * 1000L;//允许放入的参数的最大长度(字符数)
    public static final String FRAMEWORK_TRACE_ID = "framework_trace_id";
    private static final InheritableThreadLocal<TraceContext> CONTEXT_HOLDER = new InheritableThreadLocal<TraceContext>() {
        @Override
        protected TraceContext initialValue() {
            return new TraceContext();
        }
    };

    private AtomicLong remainingLength = new AtomicLong(MAX_LENGTH);
    private Map<String, String> attachment = new ConcurrentHashMap();

    /**
     * 设置需要传递给dubbo服务提供方的键值对
     *
     * @param key,  不能为null，若为null则不设置
     * @param value 不能为null，若为null则不设置
     * @exception Exception attachment所有键值对的字符串之和不能超过2000字符
     */
    public void put(String key, String value) throws Exception {
        if (key == null || value == null) {
            return;
        }
        long length = lengthCheck(key, value);
        attachment.put(key, value);
        remainingLength.addAndGet(-length);
        if(FRAMEWORK_TRACE_ID.equals(key)){
            MDC.put(key,value);
        }
    }

    /**
     * 合并设置需要传递给dubbo服务提供方的键值对
     *
     * @param map key和value不能为null，若有null则会将该键值对抛弃
     * @exception Exception attachment所有键值对的字符串之和不能超过2000字符
     */
    public void putAll(Map<String, String> map) throws Exception {
        if (CollectionUtils.isEmpty(map)) {
            return;
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取参数
     */
    public String get(String key) {
        return StringUtils.isEmpty(key) ? null : attachment.get(key);
    }

    public Map<String, String> copyAttachment() {
        return new HashMap<>(attachment);
    }

    /**
     * 参数长度限制检测
     * @exception Exception attachment所有键值对的字符串之和不能超过2000字符
     */
    protected Long lengthCheck(String key, String value) throws Exception {
        long length;
        if (attachment.containsKey(key)) {
            length = value.length() - attachment.get(key).length();
        } else {
            length = key.length() + value.length();
        }
        if (remainingLength.get() >= length) {
            return length;
        }
        throw new Exception(String.format("参数过长:%d>%d,key=%s,value=%s", (MAX_LENGTH - remainingLength.get() + length), MAX_LENGTH,key,value));
    }

    /**
     * 获取dubbo服务调用方设置的参数，不要修改！
     */
    public static TraceContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清空InheritableThreadLocal，避免线程复用时参数仍存在
     */
    public static void remove() {
        String traceId = getContext().get(FRAMEWORK_TRACE_ID);
        CONTEXT_HOLDER.remove();
        //避免第一次dubbo filter结束时清空traceId，导致第二次调用dubbo服务时traceId无值
        if(!StringUtils.isEmpty(traceId)){
            try {
                getContext().put(FRAMEWORK_TRACE_ID,traceId);
            } catch (Exception e) {
                LOGGER.error("FRAMEWORK_TRACE_ID过长，traceId={}",traceId,e);
            }
        }
    }

}
