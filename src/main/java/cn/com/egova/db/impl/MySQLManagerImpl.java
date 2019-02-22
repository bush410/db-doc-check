package cn.com.egova.db.impl;

import cn.com.egova.bean.Column;
import cn.com.egova.bean.Table;
import cn.com.egova.db.DbManager;
import cn.com.egova.doc.DocUtils;
import cn.com.egova.tools.JsonUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQLManagerImpl implements DbManager {

    private static Logger log = Logger.getLogger(MySQLManagerImpl.class);

    /**
     * 是否可以连接
     * @param jdbcUrl
     * @param jdbcUsername
     * @param jdbcPassword
     * @return
     */
    public int isConnect(String jdbcUrl, String jdbcUsername, String jdbcPassword) {

        Connection con = null;
        try{
            con = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
            if(con != null ){
                return DbManager.SUCCESS;
            }
        }catch(Exception ex){
            log.error("获取连接失败！", ex);
            String message = ex.getLocalizedMessage();
            if(message.indexOf("No suitable driver found") > -1){
                return DbManager.URL_ERROR;
            }else if(message.indexOf("Access denied") > -1) {
                return DbManager.USER_NAME_OR_PASSWORD_ERROR;
            }
        }finally {
            //关闭连接
            if(con != null){
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }

        return DbManager.ERROR;
    }

    /**
     *
     * @param jdbcUrl
     * @param jdbcUsername
     * @param jdbcPassword
     * @return
     */
    public Connection getConnection(String jdbcUrl, String jdbcUsername, String jdbcPassword){
        Connection con = null;
        try{
            con = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
        }catch(Exception ex){
            log.error("获取连接失败！", ex);
        }
        return con;
    }

    public List<Map<String, Object>> getAllTable(Connection con, String schema) {

        String sql = " select * from information_schema.tables where table_schema = ? order by table_type,table_name ";
        PreparedStatement ps = null;
        try{
            ps = con.prepareStatement(sql);
            ps.setString(1, schema);
            ResultSet resultSet = ps.executeQuery();
            return MySQLManagerImpl.convertList(resultSet);
        } catch(Exception ex){
            log.error("获取表名失败！", ex);
        } finally {
            try {
                if(ps != null){
                    ps.close();
                }
            } catch (Exception ex) {
                log.error("关闭数据库连接失败！", ex);
            }
        }
        return null;
    }


    public List<Map<String, Object>> getAllTableColumn(Connection con, String schema) {
        String sql = " select * from information_schema.columns where table_schema = ? ";
        PreparedStatement ps = null;
        try{
            ps = con.prepareStatement(sql);
            ps.setString(1, schema);
            ResultSet resultSet = ps.executeQuery();
            return MySQLManagerImpl.convertList(resultSet);
        } catch(Exception ex){
            log.error("获取所有列失败！", ex);
        } finally {
            try {
                if(ps != null){
                    ps.close();
                }
            } catch (Exception ex) {
                log.error("关闭数据库连接失败！", ex);
            }
        }
        return null;
    }


    public Map<String, Table> getAllTableMap(Connection con, String schema) {

        List<Map<String, Object>> tables = this.getAllTable(con, schema);
        List<Map<String, Object>> columns = this.getAllTableColumn(con, schema);

        Map<String, Table> tMap = new HashMap<String, Table>();

        for(Map<String, Object> tableMap : tables){
            Table table = new Table();
            table.setTableName(tableMap.get("TABLE_NAME") == null ? "" : tableMap.get("TABLE_NAME").toString().toLowerCase());
            table.setTableType(tableMap.get("TABLE_TYPE") == null ? "" : tableMap.get("TABLE_TYPE").toString());
            table.setTableSchema(tableMap.get("TABLE_SCHEMA") == null ? "" : tableMap.get("TABLE_SCHEMA").toString());
            table.setTableDes(tableMap.get("TABLE_COMMENT") == null ? "" : tableMap.get("TABLE_COMMENT").toString());
            tMap.put(table.getTableName(), table);
        }

        for(Map<String, Object> columnMap : columns){
            Column column = new Column();
            column.setTableName(columnMap.get("TABLE_NAME") == null ? "" : columnMap.get("TABLE_NAME").toString().toLowerCase());
            column.setColumnName(columnMap.get("COLUMN_NAME") == null ? "" : columnMap.get("COLUMN_NAME").toString().toLowerCase());
            column.setDataType(columnMap.get("DATA_TYPE") == null ? "" : columnMap.get("DATA_TYPE").toString());
            column.setColumnDesc(columnMap.get("COLUMN_COMMENT") == null ? "" : columnMap.get("COLUMN_COMMENT").toString());
            column.setIsNullable(columnMap.get("IS_NULLABLE").equals("YES")? "NULL" : "NOTNULL");
            if(column.getDataType().equals("varchar")){
                column.setPrecision(columnMap.get("CHARACTER_MAXIMUM_LENGTH") == null ? "" : columnMap.get("CHARACTER_MAXIMUM_LENGTH").toString());
            }
            if(columnMap.get("COLUMN_KEY") != null && columnMap.get("COLUMN_KEY").equals("PRI")){
                column.setKeyType("P");
            }

            if(column.getDataType().equals("float") || column.getDataType().equals("double")){
                column.setPrecision((columnMap.get("NUMERIC_PRECISION") == null ? "10" : columnMap.get("NUMERIC_PRECISION").toString())
                        + "," + (columnMap.get("NUMERIC_SCALE") == null ? "0" : columnMap.get("NUMERIC_SCALE").toString()));
            }
            tMap.get(column.getTableName()).getColumns().put(column.getColumnName(), column);
        }

        try {
            if(con != null){
                con.close();
            }
        } catch (Exception ex) {
            log.error("关闭数据库连接失败！", ex);
        }
        return tMap;
    }


    public static List<Map<String, Object>> convertList(ResultSet rs) throws SQLException{
        List<Map<String, Object>> list = new ArrayList();
        ResultSetMetaData md = rs.getMetaData();//获取键名
        int columnCount = md.getColumnCount();//获取行的数量
        while (rs.next()) {
            Map<String, Object> rowData = new HashMap<String, Object>();//声明Map
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));//获取键名及值
            }
            list.add(rowData);
        }
        return list;
    }


    /**
    public static void main(String args[]){
        DbManager mysql = new MySQLManagerImpl();
        Connection con = mysql.getConnection("jdbc:mysql://localhost:3305/shantou_biz", "root", "888888");
        Map map = mysql.getAllTableMap(con, "shantou_biz");
        DocUtils.genDbDoc(map);
    }
     **/
}
