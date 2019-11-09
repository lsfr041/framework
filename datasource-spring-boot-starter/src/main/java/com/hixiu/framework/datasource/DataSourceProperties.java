/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: DataSourceProperties
 * Author:   程建乐
 * Date:     2019/9/1 0:16
 * Description: DataSourceProperties
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 〈一句话功能简述〉<br> 
 * 〈DataSourceProperties〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties extends DruidDataSource {
    public DataSourceProperties(String name) {
        this.name = name;
    }
}
