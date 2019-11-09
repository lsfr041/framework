/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: DataSourceException
 * Author:   程建乐
 * Date:     2019/9/1 0:16
 * Description: DataSourceException
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.datasource;

/**
 * 〈一句话功能简述〉<br> 
 * 〈DataSourceException〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class DataSourceException extends RuntimeException{
    public DataSourceException() {
        this("DataSource异常");
    }

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
