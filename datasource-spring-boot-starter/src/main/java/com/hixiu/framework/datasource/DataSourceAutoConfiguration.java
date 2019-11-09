/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: DataSourceAutoConfiguration
 * Author:   程建乐
 * Date:     2019/9/1 0:13
 * Description: DataSourceAutoConfiguration
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.datasource;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 〈一句话功能简述〉<br> 
 * 〈DataSourceAutoConfiguration〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class DataSourceAutoConfiguration {
    @Bean(name = DataSourceAutowiredAnnotationProcessor.CLASS_NAME)
    @ConditionalOnMissingBean(name = DataSourceAutowiredAnnotationProcessor.CLASS_NAME)
    public DataSourceAutowiredAnnotationProcessor dataSourceAutowiredAnnotationBeanPostProcessor() {
        return new DataSourceAutowiredAnnotationProcessor();
    }
}
