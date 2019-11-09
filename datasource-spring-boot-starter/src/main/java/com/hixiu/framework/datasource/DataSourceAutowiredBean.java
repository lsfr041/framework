/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: DataSourceAutowiredBean
 * Author:   程建乐
 * Date:     2019/9/1 0:14
 * Description: DataSourceAutowiredBean
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.hixiu.framework.common.inject.AutowiredBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;

/**
 * 〈一句话功能简述〉<br> 
 * 〈DataSourceAutowiredBean〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class DataSourceAutowiredBean extends AutowiredBean<DataSourceProperties> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DataSourceAutowiredBean.class);

    private DataSource dataSource;

    public DataSourceAutowiredBean(String namespace, DataSourceProperties properties) {
        super(namespace, properties);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public synchronized void destroy() {
        if (dataSource != null) {
            LOGGER.info("Shutting Down DataSource . . .");
            try {
                if (dataSource instanceof DruidDataSource) {
                    ((DruidDataSource) dataSource).close();
                } else {
                    LOGGER.warn("暂不支持的DataSource类型");
                }
            } catch (Exception e) {
                LOGGER.warn("Shut Down DataSource Failed", e);
            }
            dataSource = null;
        }
    }
}
