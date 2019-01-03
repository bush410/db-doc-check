package cn.com.egova.db.impl;

import cn.com.egova.bean.Table;
import cn.com.egova.db.DbManager;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class OracleManagerImpl implements DbManager {

    /**
     * 是否可以连接
     * @param jdbcUrl
     * @param jdbcUsername
     * @param jdbcPassword
     * @return
     */
    public int isConnect(String jdbcUrl, String jdbcUsername, String jdbcPassword) {

        return DbManager.ERROR;
    }

    @Override
    public Connection getConnection(String jdbcUrl, String jdbcUsername, String jdbcPassword) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getAllTable(Connection con, String schema) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getAllTableColumn(Connection con, String schema) {
        return null;
    }

    @Override
    public Map<String, Table> getAllTableMap(Connection con, String schema) {
        return null;
    }


}
