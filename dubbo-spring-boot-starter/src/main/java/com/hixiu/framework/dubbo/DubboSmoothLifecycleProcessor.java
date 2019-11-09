/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: DubboSmoothLifecycleProcessor
 * Author:   程建乐
 * Date:     2019/9/1 0:32
 * Description: DubboSmoothLifecycleProcessor
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.dubbo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultLifecycleProcessor;

/**
 * 〈一句话功能简述〉<br> 
 * 〈DubboSmoothLifecycleProcessor〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class DubboSmoothLifecycleProcessor extends DefaultLifecycleProcessor {
    private final static Logger LOGGER = LoggerFactory.getLogger(DubboSmoothLifecycleProcessor.class);

    @Value("${dubbo.smooth.timeout:30000}")
    private long smoothTimeout;

    @Override
    public void onClose() {
        LOGGER.info("容器正在关闭 . . .");
        try {
            Thread dubboShutdownHook = getDubboShutdownHook();
            if (dubboShutdownHook != null) {
                smoothTimeout = Math.max(smoothTimeout, 0);
                LOGGER.info("等待Dubbo组件注销完成(Timeout={}ms) . . .", smoothTimeout);
                dubboShutdownHook.join(smoothTimeout);
                LOGGER.info("Dubbo组件注销完成");
            }
        } catch (Throwable e) {
            LOGGER.error("等待Dubbo组件注销完成失败", e);
        }
        onDefinedClose();
        super.onClose();
        LOGGER.info("容器已关闭 . . .");
    }

    public void onDefinedClose() {

    }

    /**
     * 从当前线程组中查找dubbo shutdown hook线程
     *
     * @return dubbo shutdown hook线程,找不到时返回null
     */
    protected static Thread getDubboShutdownHook() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        Thread[] threads = new Thread[group.activeCount()];
        group.enumerate(threads);
        for (Thread thread : threads) {
            if ("DubboShutdownHook".equalsIgnoreCase(thread.getName())) {
                return thread;
            }
        }
        return null;
    }
}
