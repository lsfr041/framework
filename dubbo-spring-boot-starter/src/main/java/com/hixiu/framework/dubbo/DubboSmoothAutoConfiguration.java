/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: DubboSmoothAutoConfiguration
 * Author:   程建乐
 * Date:     2019/9/1 0:32
 * Description: DubboSmoothAutoConfiguration
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.dubbo;

import com.alibaba.boot.dubbo.autoconfigure.DubboAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.LifecycleProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * 〈一句话功能简述〉<br> 
 * 〈DubboSmoothAutoConfiguration〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
@Configuration
@AutoConfigureAfter(DubboAutoConfiguration.class)
public class DubboSmoothAutoConfiguration {
    @Bean(AbstractApplicationContext.LIFECYCLE_PROCESSOR_BEAN_NAME)
    @ConditionalOnMissingBean(name = AbstractApplicationContext.LIFECYCLE_PROCESSOR_BEAN_NAME)
    public LifecycleProcessor smoothLifecycleProcessor() {
        return new DubboSmoothLifecycleProcessor();
    }
}
