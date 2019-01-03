package cn.com.egova.db;

import cn.com.egova.bean.Table;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface DbManager {

    public static int SUCCESS = 0;
    public static int ERROR = -1;
    public static int URL_ERROR = -2;
    public static int USER_NAME_OR_PASSWORD_ERROR = -3;

    /**
     * 检查连接
     * @param jdbcUrl jdbc地址
     * @param jdbcUsername 用户名
     * @param jdbcPassword 密码
     * @return 0 成功 -1 jdbcUrl 错误  -2 jdbcUsername 或者 jdbcPassword 错误
     */
    int isConnect(String jdbcUrl, String jdbcUsername, String jdbcPassword);


    /**
     * 获取连接
     * @param jdbcUrl jdbc地址
     * @param jdbcUsername 用户名
     * @param jdbcPassword 密码
     * @return 连接
             */
    public Connection getConnection(String jdbcUrl, String jdbcUsername, String jdbcPassword);

    /**
     * 获取所有表
     * @param con 连接
     * @param schema 表集合
     * @return 表名列表
     */
    List<Map<String, Object>> getAllTable(Connection con, String schema);

    /**
     * 获取所有列
     * @param con 连接
     * @param schema 表集合
     * @return 表列列表
     */
    List<Map<String, Object>> getAllTableColumn(Connection con, String schema);

    /**
     * 获取所有表Map
     * @param con
     * @param schema
     * @return
     */
    Map<String, Table> getAllTableMap(Connection con, String schema);


 }
