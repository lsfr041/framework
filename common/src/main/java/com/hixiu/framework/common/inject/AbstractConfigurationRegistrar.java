/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: AbstractConfigurationRegistrar
 * Author:   程建乐
 * Date:     2019/8/31 23:08
 * Description: AbstractConfigurationRegistrar
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.inject;

import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈AbstractConfigurationRegistrar〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
public abstract class AbstractConfigurationRegistrar  implements ImportBeanDefinitionRegistrar {
    protected AutowiredAnnotationConfig[] parseAutowiredAnnotationConfigs(AnnotationMetadata metadata, String annotationName) {
        List<AutowiredAnnotationConfig> configs = new LinkedList<>();
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annotationName));
        String[] values = attributes.getStringArray("value");

        if (values != null && values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                if (StringUtils.isEmpty(values[i])) {
                    continue;
                }
                configs.add(new AutowiredAnnotationConfig(values[i], i == 0)); // 第一个namespace作为primary注入ioc
            }
        }

        return configs.toArray(new AutowiredAnnotationConfig[0]);
    }
}
