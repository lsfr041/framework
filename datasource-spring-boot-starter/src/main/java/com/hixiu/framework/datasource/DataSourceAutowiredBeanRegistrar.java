/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: DataSourceAutowiredBeanRegistrar
 * Author:   程建乐
 * Date:     2019/9/1 0:15
 * Description: DataSourceAutowiredBeanRegistrar
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.datasource;

import com.hixiu.framework.common.inject.AbstractAutowiredBeanRegistrar;
import com.hixiu.framework.common.inject.AutowiredAnnotationConfig;
import com.hixiu.framework.common.properties.ApolloConfigurationPropertiesBinderUtils;
import com.hixiu.framework.common.utils.NameSpaceBeanNameUtils;
import org.apache.ibatis.datasource.DataSourceException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import javax.sql.DataSource;

/**
 * 〈一句话功能简述〉<br> 
 * 〈DataSourceAutowiredBeanRegistrar〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class DataSourceAutowiredBeanRegistrar extends AbstractAutowiredBeanRegistrar<DataSourceAutowiredBean> {

    public DataSourceAutowiredBeanRegistrar(AutowiredAnnotationConfig config, DefaultListableBeanFactory beanFactory) {
        super(config, beanFactory);
        this.autowiredBean = new DataSourceAutowiredBean(config.getValue(), bindProperties(beanFactory, config.getValue()));
    }

    /**
     * 从配置中心绑定配置属性到{@link DataSourceProperties}
     *
     * @param beanFactory
     * @param namespace
     * @return
     */
    protected static DataSourceProperties bindProperties(DefaultListableBeanFactory beanFactory, String namespace) {
        return ApolloConfigurationPropertiesBinderUtils.bindFromNamespace(beanFactory, namespace, new DataSourceProperties(namespace));
    }

    protected void buildAndRegisterDataSource() {
        String beanName = NameSpaceBeanNameUtils.build(DataSource.class, this.autowiredBean.getNamespace());
        if (beanFactory.containsBean(beanName)) {
            logger.info("Bean[{}]已存在", beanName);
            autowiredBean.setDataSource(beanFactory.getBean(beanName, DataSource.class));
        } else {
            logger.debug("创建并注册Bean:{}", beanName);
            try {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SwitchableDruidDataSource.class);
                builder.addConstructorArgValue(autowiredBean.getProperties());
                registerBeanDefinition(beanName, builder.getBeanDefinition());
                autowiredBean.setDataSource(beanFactory.getBean(beanName, DataSource.class));
            } catch (Throwable e) {
                throw new DataSourceException("初始化DataSource失败", e);
            }
        }
    }

    @Override
    protected DataSourceAutowiredBean build() {
        this.buildAndRegisterDataSource();
        return autowiredBean;
    }

    public static DataSourceAutowiredBean register(AutowiredAnnotationConfig config, DefaultListableBeanFactory beanFactory) {
        return new DataSourceAutowiredBeanRegistrar(config, beanFactory).register();
    }
}
