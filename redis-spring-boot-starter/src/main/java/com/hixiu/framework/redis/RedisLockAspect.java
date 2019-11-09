/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisLockAspect
 * Author:   程建乐
 * Date:     2019/9/1 0:51
 * Description: RedisLockAspect
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

import com.hixiu.framework.common.utils.NameSpaceBeanNameUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;


/**
 * 〈一句话功能简述〉<br> 
 * 〈RedisLockAspect〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class RedisLockAspect implements BeanFactoryAware, Ordered {
    public static final String CLASS_NAME = "com.hixiu.framework.redis.RedisLockAspect";

    private final static Logger logger = LoggerFactory.getLogger(RedisLockAspect.class);

    private ExpressionParser parser = new SpelExpressionParser();

    private DefaultListableBeanFactory beanFactory;
    private String primaryBeanName;
    private final static String ERROR_CODE = "9010";

    public RedisLockAspect() {
    }

    public RedisLockAspect(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    /**
     * 执行分布式锁逻辑
     *
     * @param joinPoint
     * @param redisLock
     * @return
     * @throws Throwable
     */
    @Around("@annotation(redisLock)")
    public Object process(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {

        RedisService.RedisLocker locker = getLocker(joinPoint, redisLock);

        boolean lock = locker.tryLock();

        if (lock) {
            try {

                return joinPoint.proceed();
            } finally {
                locker.release();

            }
        }

        if ((RedisLock.FailAction.THROW_EXCEPTION.equals(redisLock.failAction())) || redisLock.failAction() == null) {

            throw new RedisException("获取分布式锁失败");
        }
        return null;
    }

    /**
     * 获取分布式锁对象
     *
     * @param joinPoint
     * @param redisLock
     * @return
     */
    public RedisService.RedisLocker getLocker(ProceedingJoinPoint joinPoint, RedisLock redisLock) {
        String key = getKey(joinPoint, redisLock);
        RedisService redisService = getRedisService(redisLock.value());
        return redisService.buildLock(key, redisLock.expire(), TimeUnit.MILLISECONDS);
    }

    /**
     * 获取分布式锁key
     *
     * @param joinPoint
     * @param redisLock
     * @return
     */
    private String getKey(ProceedingJoinPoint joinPoint, RedisLock redisLock) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (Exception e) {
                // ignore
            }
        }
        if (StringUtils.isEmpty(redisLock.key())) {
            return method.getDeclaringClass().getCanonicalName() + "." + method.getName();
        }
        EvaluationContext context = new MethodBasedEvaluationContext(null, method, joinPoint.getArgs(), new DefaultParameterNameDiscoverer());

        try {
            return parser.parseExpression(redisLock.key()).getValue(context).toString();
        } catch (Exception e) {
            logger.warn("redis 分布式锁 spel 表达式出错, key: {}, message: {}", redisLock.key(), e.getMessage());
            throw new RedisException("redis 分布式锁 spel 表达式出错");
        }

    }

    /**
     * 获取 RedisService 实例
     *
     * @param namespace
     * @return
     */
    private synchronized RedisService getRedisService(String namespace) {
        String beanName;
        if (!StringUtils.isEmpty(namespace)) {
            beanName = NameSpaceBeanNameUtils.build(RedisService.class, namespace);
        } else if (!StringUtils.isEmpty(primaryBeanName)) {
            beanName = primaryBeanName;
        } else {
            String[] names = beanFactory.getBeanNamesForType(RedisService.class);
            if (names.length < 1) {
                throw new RedisException("容器中无 RedisService 实例对象");
            }
            primaryBeanName = names[0];
            beanName = primaryBeanName;
        }
        try {
            logger.debug("分布式锁服务获取 RedisService 对象, namespace: {}, beanName: {}", namespace, beanName);
            return (RedisService) beanFactory.getBean(beanName);
        } catch (Exception e) {
            logger.warn("找不到redisService实例对象, beanName: {}", beanName);
            throw new RedisException("找不到redisService实例对象, beanName: " + beanName);
        }
    }


    /**
     * 优先切入
     *
     * @return
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }
}
