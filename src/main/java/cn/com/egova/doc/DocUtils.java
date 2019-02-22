package cn.com.egova.doc;

import cn.com.egova.bean.Column;
import cn.com.egova.bean.Table;
import cn.com.egova.tools.ColorUtils;
import cn.com.egova.tools.JsonUtils;
import org.apache.log4j.Logger;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.rmi.server.ExportException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class DocUtils {

    private static Logger log = Logger.getLogger(DocUtils.class);


    /**
     * 读出word文件中的字符串
     * @param path
     * @return
     */
    public static String readWord(String path){
        FileInputStream fis = null;
        POIXMLTextExtractor extractor = null;
        try {
            fis = new FileInputStream(path);
            String ws = DocUtils.readWord(fis);
            if(ws == null){
                OPCPackage opcPackage = POIXMLDocument.openPackage(path);
                extractor = new XWPFWordExtractor(opcPackage);
                return extractor.getText();
            }else{
                return ws;
            }
        } catch (Exception ex) {
            log.error("读取word出错！", ex);
        } finally {
            if(extractor != null){
                try{
                    extractor.close();
                }catch (Exception e){}
            }
        }
        return null;
    }


    public static String readWord(File file){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            String ws = DocUtils.readWord(fis);
            if(ws == null){
                OPCPackage opcPackage = POIXMLDocument.openPackage(file.getAbsolutePath());
                POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
                return extractor.getText();
            }else{
                return ws;
            }

        } catch (Exception ex) {
            log.error("读取word出错！", ex);
        }
        return null;
    }

    public static List<Column> getColumn(){
        return null;
    }


    public static List<Table> getTableNames(File file){
        return  DocUtils.getTableNames(DocUtils.readWord(file));
    }



    public static List<Table> getTableNames(String ws){
        List<Table> list = new ArrayList<Table>();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(ws.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line;
        int begin, end;
        int pos = 0;
        String tableName = null;
        int pointPos = -1;
        try{
            while ((line = br.readLine()) != null) {
                if(!line.trim().equals("")){
                    // 匹配：开头 + 空字符（或没有）+ 表 + 空字符（或没有）+ （表名）| (表名) + 空字符（或没有）+（附加信息）| (附加信息)（或没有） + 空字符（或没有） + 结尾
                    if(Pattern.matches("^\\s*\\S*表\\s*(\\(|（)\\S+(\\)|）)\\s*((\\(|（)\\S+(\\)|）))?\\s*$", line)){
                        begin = line.indexOf("(");
                        if(begin < 0){
                            begin = line.indexOf("（");
                        }
                        end = line.indexOf(")");
                        if(end < 0){
                            end = line.indexOf("）");
                        }
                        Table t = new Table();
                        t.setPos(pos);
                        //System.out.println("文字解析表位置：" + t.getPos());
                        tableName = line.substring(begin + 1, end).toLowerCase();
                        pointPos = tableName.indexOf(".");
                        //System.out.println("pointPos:" + pointPos);
                        if(pointPos < 0){
                            t.setTableName(tableName);
                        }else{
                            //System.out.println(tableName);
                            t.setTableName(tableName.substring(pointPos + 1));
                            t.setTableSchema(tableName.substring(0, pointPos));
                        }
                        //System.out.println("文件解析到的表名：" + t.getTableName());
                        list.add(t);
                    }
                }
                pos += line.replaceAll("\\s*", "").length();
            }
        }catch (Exception ex){
            log.error("解析表名出错！" + tableName, ex);
        }
        //System.out.println(JsonUtils.toJsonString(list));
        return  list;
    }

    /**
     * 读出word文件中的字符串
     * @param fis
     * @return
     */
    public static String readWord(FileInputStream fis){
        try {
            XWPFDocument xdoc = new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
            return extractor.getText();
        } catch (Exception ex) {
            try{
                HWPFDocument doc = new HWPFDocument(fis);
                return doc.getDocumentText();
            }catch (Exception e){
                //log.error("读取word出错！", ex);
            }
        } finally {
            if(fis != null){
                try{
                    fis.close();
                }catch (Exception ex){}
            }
        }
        return null;
    }

    public static List<Table> readWordTable(FileInputStream fis){

        List<Table> list = new ArrayList<Table>();
        XWPFDocument document = null;
        String docStr = "";
        String docStrNotBlack = "";
        try {
            document = new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            docStr = extractor.getText();
            docStrNotBlack = docStr.replaceAll("\\s*","");
            //System.out.println(docStrNotBlack);
            //System.out.println("*****************************************");
        }catch (Exception ex){
            log.error("读取word出错！", ex);
            return null;
        }

        //从文字中获取的表名
        List<Table> listTable = DocUtils.getTableNames(docStr);

        // 获取所有表格
        List<XWPFTable> tables = document.getTables();
        log.debug("表格数量：" + tables.size());
        for (XWPFTable table : tables) {
            Table t = new Table();
            boolean validFlag = true;
            // 获取表格的行
            List<XWPFTableRow> rows = table.getRows();
            int i = 0;
            int j = 0;
            for (XWPFTableRow row : rows) {
                // 获取表格的每个单元格
                j = 0;
                List<XWPFTableCell> tableCells = row.getTableCells();
                Column column = new Column();
                for (XWPFTableCell cell : tableCells) {
                    // 获取单元格的内容
                    String text = cell.getText();
                    if(i == 0 && j == 0){
                        if(!text.trim().contains("键")){
                            validFlag = false;
                            break;
                        }else{
                            column = null;
                            break; //第一行为标题，不解析
                        }
                    }
                    switch(j){
                        case 0:
                            column.setKeyType(text);
                            break;
                        case 1:
                            column.setColumnDesc(text);
                            break;
                        case 2:
                            column.setColumnName(text.toLowerCase());
                            break;
                        case 3:
                            column.setDataType(text);
                            break;
                        case 4:
                            column.setPrecision(text);
                            break;
                        case 5:
                            column.setIsNullable(text);
                            break;
                        case 6:
                            column.setMemo(text);
                            break;
                    }
                    j++;
                }
                i++;
                // 如果不是需要的表格，则跳过
                if(!validFlag){
                    break;
                }
                if(column != null){
                    t.getColumns().put(column.getColumnName(), column);
                }
                //System.out.println("" + JsonUtils.toJsonString(column));
            }
            if(!validFlag){
                continue;
            }else{
                t.setPos(docStrNotBlack.indexOf(table.getText().replaceAll("\\s*", "")));//设置表格在文档位置
                //log.debug("表格位置：" + t.getPos());
                list.add(t);
            }
        }
        if(list.size() == 0){
            log.error("文档不符合规范！无法解析！");
            return null;
        }

        // System.out.println("解析出的表名：" + JsonUtils.toJsonString(listTable));

        //匹配到表名
        //这样匹配会造成错位
        for(int i = list.size() - 1; i >= 0; i--){
            for(int j = listTable.size() - 1; j >= 0; j--){
                if(listTable.get(j).getPos() < list.get(i).getPos()){
                    // System.out.println("找到表名了！：" + listTable.get(j).getTableName());
                    list.get(i).setTableName(listTable.get(j).getTableName());
                    break;
                }
            }
        }
        //System.out.println("解析表格表的个数：" + list.size());
        //System.out.println("解析出的表：" + JsonUtils.toJsonString(list));
        //for(Table tt : list){
          //  System.out.println("匹配后的表名" + tt.getTableName());
        //}
        return list;
    }

    public static boolean getWordStream(String str, String fileName){

        XWPFDocument doc = new XWPFDocument();// 创建Word文件
        XWPFParagraph p = doc.createParagraph(); //创建一个段落，用于表头
        p.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun r = p.createRun();//创建段落文本
        r = p.createRun();//创建段落文本
        r.setText(str);

        FileOutputStream out = null;
        boolean success = true;
        try {
            File newFile = new File(fileName);
            if(!newFile.exists()){
                newFile.createNewFile();
            }
            out = new FileOutputStream(newFile);
        } catch (Exception ex) {
            log.error("生成文件失败！", ex);
            success = false;
            return success;
        }

        try {
            doc.write(out);
        } catch (IOException ex) {
            log.error("生成文件失败！", ex);
            success = false;
        }
        try {
            out.close();
        } catch (IOException ex) {
            success = false;
        }

        return success;
    }

    public static List<Table> readWordTable(File file){

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (Exception ex) {
            log.error("读取word出错！", ex);
        }
        return  DocUtils.readWordTable(fis);
    }

    public static Map<String, Table> readWordTableMap(File file){
        List<Table> list = DocUtils.readWordTable(file);
        Map<String, Table> map = new HashMap<String, Table>();
        if(list != null && list.size() > 0){
            // 倒序设置保证可以覆盖错误的表解析
            for(int i = list.size();i>0; i--){
                map.put(list.get(i - 1).getTableName(), list.get(i - 1));
            }
            log.info("解析文档成功！" + file.getName());
        }else{
            log.error("word文档不符合规范：" + file.getName());
        }
        return  map;
    }

    public static void setTableGridCol(XWPFTable table, int[] colWidths) {
        CTTbl ttbl = table.getCTTbl();
        CTTblGrid tblGrid = ttbl.getTblGrid() != null ? ttbl.getTblGrid()
                : ttbl.addNewTblGrid();
        for (int j = 0, len = colWidths.length; j < len; j++) {
            CTTblGridCol gridCol = tblGrid.addNewGridCol();
            gridCol.setW(new BigInteger(String.valueOf(colWidths[j])));
        }
    }


    /**
     * 生成数据库文档
     * @param tableMap
     */
    public static XWPFDocument genDbDoc(Map<String, Table> tableMap){
        XWPFDocument doc = new XWPFDocument();// 创建Word文件
        String rgbStr = ColorUtils.Color2Str(Color.LIGHT_GRAY);
        if(tableMap != null){
            for(Map.Entry<String, Table> t : tableMap.entrySet()){
                XWPFParagraph p = doc.createParagraph(); //创建一个段落，用于表头
                p.setAlignment(ParagraphAlignment.LEFT);
                p.insertNewRun(0).addCarriageReturn();
                XWPFRun r = p.createRun();//创建段落文本
                r = p.createRun();//创建段落文本
                if(t.getValue().getTableDes() == null || t.getValue().getTableDes().equals("")){
                    r.setText("表（" + t.getValue().getTableName() + "）");
                }else if(t.getValue().getTableDes().endsWith("表")){
                    r.setText(t.getValue().getTableDes() + "（" + t.getValue().getTableName() + "）");
                }else{
                    r.setText(t.getValue().getTableDes() + "表（" + t.getValue().getTableName() + "）");
                }
                r.setFontFamily("宋体");
                r.setFontSize(14);
                r.setBold(true);

                XWPFTable table = doc.createTable(t.getValue().getColumns().size() + 1, 7);//创建一个表格
                table.getRow(0).getCell(0).setText("键");
                table.getRow(0).setHeight(30);
                table.getRow(0).getCell(1).setText("字段描述");
                table.getRow(0).getCell(2).setText("字段名");
                table.getRow(0).getCell(3).setText("数据类型");
                table.getRow(0).getCell(4).setText("字段长度");
                table.getRow(0).getCell(5).setText("是否可空");
                table.getRow(0).getCell(6).setText("备注");
                table.getRow(0).getCell(0).setColor(rgbStr);
                table.getRow(0).getCell(1).setColor(rgbStr);
                table.getRow(0).getCell(2).setColor(rgbStr);
                table.getRow(0).getCell(3).setColor(rgbStr);
                table.getRow(0).getCell(4).setColor(rgbStr);
                table.getRow(0).getCell(5).setColor(rgbStr);
                table.getRow(0).getCell(6).setColor(rgbStr);


                DocUtils.setTableGridCol(table, new int[]{ 500, 1700, 1700, 1200, 1200, 1200, 1000});
                int i = 1;
                int j = t.getValue().getColumns().size();
                for(Map.Entry<String, Column> c : t.getValue().getColumns().entrySet()){
                    table.getRow(i).setHeight(30);
                    if("P".equals(c.getValue().getKeyType())){
                        table.getRow(i).getCell(0).setText(c.getValue().getKeyType());
                        table.getRow(i).getCell(1).setText(c.getValue().getColumnDesc());
                        table.getRow(i).getCell(2).setText(c.getValue().getColumnName());
                        table.getRow(i).getCell(3).setText(c.getValue().getDataType());
                        table.getRow(i).getCell(4).setText(c.getValue().getPrecision());
                        table.getRow(i).getCell(5).setText(c.getValue().getIsNullable());
                        table.getRow(i).getCell(6).setText(c.getValue().getMemo());
                        i++;
                    }else{
                        table.getRow(j).getCell(0).setText(c.getValue().getKeyType());
                        table.getRow(j).getCell(1).setText(c.getValue().getColumnDesc());
                        table.getRow(j).getCell(2).setText(c.getValue().getColumnName());
                        table.getRow(j).getCell(3).setText(c.getValue().getDataType());
                        table.getRow(j).getCell(4).setText(c.getValue().getPrecision());
                        table.getRow(j).getCell(5).setText(c.getValue().getIsNullable());
                        table.getRow(j).getCell(6).setText(c.getValue().getMemo());
                        j--;
                    }
                }
            }
        }

        return doc;
    }

   // public static void main(String args[]) throws Exception {
        //DocUtils.readWordTable("/myfile/workspace/git/wizdom-urban-v14/doc/数据库设计/城管业务表结构设计文档.docx");
        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/户外广告系统数据库设计文档.docx");//不可以
       // String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/智云车载GPS系统表结构设计文档.docx");
        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/公园管理子系统业务表结构设计.docx");
        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/门前三包子系统数据库设计文档.docx");
        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/视频上报系统数据库设计文档.docx");
        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/信息采集员管理子系统业务表结构设计.docx");
        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/应急指挥系统数据库设计文档.doc"); //不可以
        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/园林绿化子系统业务表结构设计文档.docx");
        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/渣土系统数据库设计文档.doc"); //不可以

        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/专项排查系统数据库设计文档.docx");
        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/河长数据表V1.0.docx");

        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/环保空气质量数据库文档V1.0.docx");
        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/环保水质量数据文档V1.0.docx");
        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/环保通用文档V1.0.docx");

        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/市政产品数据库设计文档.docx");

        //String ws = DocUtils.readWord("/Users/dongliang/Downloads/db/智信后台数据库设计.doc");
        //System.out.println(ws);

    //}

    /**
    public static void main(String args[]) {


        if (Pattern.matches("^\\s*\\S*表\\s*(\\(|（)\\S+(\\)|）)\\s*((\\(|（)\\S+(\\)|）))?\\s*$", "2物理图层组表(tc_phy_layer_group)")) {
            System.out.println("匹配到！");
        }else{
            System.out.println("没匹配到");
        }

    }
     **/
}
