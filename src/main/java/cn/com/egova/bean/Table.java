package cn.com.egova.bean;

import java.util.HashMap;
import java.util.Map;

public class Table {

    String tableName;
    String tableType;  // 表还是视图
    String tableSchema;

    boolean existFlag = false; //是否在文档中存在

    int pos = -1; //表格定义在文本中的位置

    Map<String, Column> columns = new HashMap<String, Column>(); // 列，map方便比较

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public Map<String, Column> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, Column> columns) {
        this.columns = columns;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public boolean isExistFlag() {
        return existFlag;
    }

    public void setExistFlag(boolean existFlag) {
        this.existFlag = existFlag;
    }
}
