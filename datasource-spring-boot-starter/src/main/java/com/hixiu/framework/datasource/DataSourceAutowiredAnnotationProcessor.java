/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: DataSourceAutowiredAnnotationProcessor
 * Author:   程建乐
 * Date:     2019/9/1 0:13
 * Description: DataSourceAutowiredAnnotationProcessor
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.hixiu.framework.common.inject.AutowiredAnnotationConfig;
import com.hixiu.framework.common.inject.AutowiredAnnotationProcessor;
import org.apache.ibatis.datasource.DataSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈DataSourceAutowiredAnnotationProcessor〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class DataSourceAutowiredAnnotationProcessor extends AutowiredAnnotationProcessor<DataSourceAutowiredBean, DataSourceAutowired> {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataSourceAutowiredAnnotationProcessor.class);

    public static final String CLASS_NAME = "com.hixiu.framework.datasource.DataSourceAutowiredAnnotationProcessor";

    public DataSourceAutowiredAnnotationProcessor() {
        super(DataSourceAutowired.class);
    }

    public DataSourceAutowiredAnnotationProcessor(DefaultListableBeanFactory beanFactory, AutowiredAnnotationConfig... configs) {
        super(DataSourceAutowired.class, beanFactory, configs);
    }

    @Override
    protected AutowiredAnnotationConfig parseConfig(DataSourceAutowired autowired) {
        return new AutowiredAnnotationConfig(autowired.value());
    }

    @Override
    protected DataSourceAutowiredBean create(AutowiredAnnotationConfig config) {
        DataSourceAutowiredBean bean = DataSourceAutowiredBeanRegistrar.register(config, beanFactory);
        Connection c = null;
        try {
            c = bean.getDataSource().getConnection();
        } catch (Throwable e) {
            LOGGER.error("DataSource初始化时，获取连接失败...",e);
            throw new DataSourceException("DataSource初始化时，获取连接失败...",e);
        } finally {
            if(c != null){
                try {
                    c.close();
                } catch (SQLException e) {
                    LOGGER.error("DataSource初始化时，关闭连接失败...",e);
                }
            }
        }
        return bean;
    }

    @Override
    protected Object getBean(DataSourceAutowiredBean autowiredBean, Class<?> autowiredType) {
        if (autowiredType == DataSource.class || autowiredType == DruidDataSource.class) {
            return autowiredBean.getDataSource();
        } else if (autowiredType == DataSourceAutowiredBean.class) {
            return autowiredBean;
        }
        return null;
    }

}
