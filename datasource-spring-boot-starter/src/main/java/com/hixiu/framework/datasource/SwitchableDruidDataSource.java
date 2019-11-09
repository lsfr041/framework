/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: SwitchableDruidDataSource
 * Author:   程建乐
 * Date:     2019/9/1 0:17
 * Description: SwitchableDruidDataSource
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.datasource;

import com.alibaba.druid.pool.DruidAbstractDataSource;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.util.StringUtils;
import com.hixiu.framework.common.utils.NameSpaceBeanNameUtils;
import org.springframework.beans.BeanUtils;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈SwitchableDruidDataSource〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class SwitchableDruidDataSource extends DruidDataSource {
    public SwitchableDruidDataSource(DataSourceProperties properties) {
        try {
            //不再设置DruidDataSource的deprecated，validConnectionCheckerClass为已存在的默认值，避免错误日志
            List<String> excludeList = new ArrayList<>();
            if(properties.getMaxIdle() == DruidAbstractDataSource.DEFAULT_MAX_IDLE){
                excludeList.add("maxIdle");
            }
            if(StringUtils.isEmpty(properties.getValidConnectionCheckerClassName())){
                excludeList.add("validConnectionCheckerClassName");
            }
            BeanUtils.copyProperties(properties, this, excludeList.toArray(new String[excludeList.size()]));
        } catch (Exception e) {
            throw new RuntimeException("拷贝数据源连接配置失败", e);
        }
    }

    @Override
    @PostConstruct
    public void init() throws SQLException {
        super.init();
    }

    @Override
    public DruidPooledConnection getConnection(long maxWaitMillis) throws SQLException {
        // TODO 监听数据源配置变化,动态切换
        return super.getConnection(maxWaitMillis);
    }


    @Override
    public String toString() {
        return NameSpaceBeanNameUtils.build(this.getClass(), name);
    }
}
