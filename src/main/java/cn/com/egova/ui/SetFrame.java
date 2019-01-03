package cn.com.egova.ui;

import cn.com.egova.bean.Column;
import cn.com.egova.bean.Table;
import cn.com.egova.db.DbManager;
import cn.com.egova.db.impl.MySQLManagerImpl;
import cn.com.egova.db.impl.OracleManagerImpl;
import cn.com.egova.doc.DocUtils;
import cn.com.egova.tools.JsonUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;
import java.io.File;
import java.util.List;


/**
 * author: yindongliang@eogva.com.cn
 * 设置页面包含：
 * 1）多个mysql 数据库地址、用户名、密码、连接测试按钮
 * 2）文档目录位置
 * 3) 导出检查报告，导出数据库文档
 */
public class SetFrame extends JFrame {

    private static Logger log = Logger.getLogger(SetFrame.class);

    List<DbSetComp> dbSetList = new ArrayList<DbSetComp>();
    // 数据库连接设置面板
    private JPanel dbSetPanel = new JPanel();
    private DbSetComp dbSetComp1 = new DbSetComp();
    //private DbSetComp dbSetComp2 = new DbSetComp();
    // 数据库文档位置面板
    private JPanel dbPathPanel = new JPanel();
    // 定义一个单选按钮
    private static JRadioButton mysqlBtn = new JRadioButton("MySQL");
    private static JRadioButton oracleBtn = new JRadioButton("Oracle");


    // 状态栏
    JLabel state = new JLabel("使用提示：");
    // 状态栏消息提醒
    static JLabel StateLabel = new JLabel("欢迎使用！");
    // 添加数据库配置按钮
    private JButton addDBSetBtn = new JButton("+");
    // 文件选择
    private JTextField dbFiles = new JTextField();
    // 添加文件按钮
    private JButton addFileBtn = new JButton("添加文件");
    // 删除文件按钮
    private JButton delFileBtn = new JButton("删除文件");
    // 导出按钮面板
    private JPanel exportPanel = new JPanel();
    // 导出检查报告
    private JButton exportCheckDoc = new JButton("导出检查报告");
    // 导出数据库文档
    private JButton exportDbDoc = new JButton("导出数据库文档");

    private List<File> selectFiles = new ArrayList<File>();

    // 默认为MySQL
    private static DbManager DBManager = null;

    public static DbManager getDBManager(){
        if(SetFrame.mysqlBtn.isSelected()){
            DBManager = new MySQLManagerImpl();
        }else{
            DBManager = new OracleManagerImpl();
        }
        return DBManager;
    }

    public static void showMsg(String msg){
        SetFrame.StateLabel.setText(msg);
        SetFrame.StateLabel.setForeground(Color.red);
    }


    public static void main(String args[]){

        SetFrame setFrame = new SetFrame();
        setFrame.setVisible(true);

    }

    private SetFrame(){
        super();
        this.initUI();
        this.bindUI();
    }


    // 初始化UI组件
    private void initUI(){
        // 标题
        this.setTitle("数据库文档检查与编写工具");
        // 大小
        this.setSize(520, 450);//330

        // 窗口居中
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int left = (d.getSize().width - this.getSize().width) / 2;
        int top = (d.getSize().height - this.getSize().height) / 2;
        this.setLocation(left, top);

        // 不能改变大小
        //this.setResizable(false);

        //流式布局
        this.setLayout(new FlowLayout(FlowLayout.CENTER));


        //加入数据库连接设置面板
        this.add(this.dbSetPanel);
        //this.dbSetPanel.setAutoscrolls(true);
        this.dbSetPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.dbSetPanel.setPreferredSize(new Dimension(490, 250));//150
        this.dbSetPanel.setBorder(BorderFactory.createTitledBorder("数据库连接设置"));
        this.dbSetPanel.add(this.mysqlBtn);
        this.dbSetPanel.add(this.oracleBtn);
        //暂时只支持MySQL
        this.mysqlBtn.setSelected(true);
        //this.mysqlBtn.setEnabled(false);
        this.oracleBtn.setSelected(false);
        this.oracleBtn.setEnabled(false);


        this.dbSetPanel.add(this.dbSetComp1.getPanel());
        //this.dbSetPanel.add(this.dbSetComp2.getPanel());
        this.dbSetComp1.getPanel().add(this.addDBSetBtn);
        this.addDBSetBtn.setPreferredSize(new Dimension(25, 20));
        this.addDBSetBtn.setMargin(new Insets(0,0,0,0));

        //加入数据库文档选择面板
        this.add(this.dbPathPanel);
        this.dbPathPanel.setPreferredSize(new Dimension(490, 65));
        this.dbPathPanel.setBorder(BorderFactory.createTitledBorder("数据库文档选择"));
        this.dbPathPanel.add(this.dbFiles);
        this.dbPathPanel.add(this.addFileBtn);
        this.dbPathPanel.add(this.delFileBtn);
        this.delFileBtn.setPreferredSize(new Dimension(90, 20));
        this.delFileBtn.setMargin(new Insets(0,0,0,0));
        this.dbPathPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.dbFiles.setPreferredSize(new Dimension(280, 20));
        //不可编辑
        this.dbFiles.setEditable(false);
        this.addFileBtn.setPreferredSize(new Dimension(90, 20));
        this.addFileBtn.setMargin(new Insets(0,0,0,0));

        //导出按钮面板
        this.add(this.exportPanel);
        this.exportPanel.setPreferredSize(new Dimension(487, 45));
        this.exportPanel.setBorder(BorderFactory.createTitledBorder(""));
        this.exportPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.exportPanel.add(this.exportCheckDoc);
        this.exportPanel.add(this.exportDbDoc);

        // 加入提示信息
        this.add(this.state);
        this.add(this.StateLabel);

        this.dbSetList.add(dbSetComp1);
        //删除 "-" 按钮
        dbSetComp1.getPanel().remove(dbSetComp1.getDelDBSetBtn());
        dbSetComp1.getJdbcUrl().setText("jdbc:mysql://localhost:3306/cgdb");
        //this.DbSetList.add(dbSetComp2);

    }

    // 绑定UI组件事件
    private void bindUI(){
        //关闭退出
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // 添加数据库文件
        this.addFileBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

                // 只识别doc和docx文档
                FileFilter filefilter = new FileFilter() {
                    public boolean accept(File file) {
                        if (file.getName().toLowerCase().endsWith(".doc")
                                || file.getName().toLowerCase().endsWith(".docx")) {
                            return true;
                        }
                        return false;
                    }
                    public String getDescription() {
                        return null;
                    }
                };
                jfc.setFileFilter(filefilter);
                jfc.setAcceptAllFileFilterUsed(true);

                //多选
                jfc.setMultiSelectionEnabled(true);
                jfc.showDialog(new JLabel(), "选择");
                File[] files = jfc.getSelectedFiles();
                if(files != null && files.length > 0){
                    for(File file : files){
                        selectFiles.add(file);
                    }
                }
                setDbFilesText();
            }
        });

        // 删除文件
        this.delFileBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                if(selectFiles.size() > 0){
                    selectFiles.remove(selectFiles.size() - 1);
                    setDbFilesText();
                }
            }
        });

        //添加数据库配置行
        this.addDBSetBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                DbSetComp dbSetComp = new DbSetComp();
                dbSetList.add(dbSetComp);
                dbSetPanel.add(dbSetComp.getPanel());
                dbSetPanel.updateUI();
                DbSetComp.COUNT++;
                if(DbSetComp.COUNT == 5){
                    addDBSetBtn.setEnabled(false);
                }

                //删除配置按钮
                dbSetComp.getDelDBSetBtn().addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent arg0) {
                        dbSetPanel.remove(dbSetComp.getPanel());
                        dbSetList.remove(dbSetComp);
                        DbSetComp.COUNT--;
                        if(DbSetComp.COUNT < 5){
                            addDBSetBtn.setEnabled(true);
                        }
                        dbSetPanel.updateUI();
                    }
                });
            }
        });

        this.exportDbDoc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setDialogTitle("保存数据库文档");
                File file = new File("数据库文档" + new Date().getTime()+ ".doc");
                jfc.setSelectedFile(file);//设置默认文件名
                int resule = jfc.showSaveDialog(null);
                File f = null;
                if(resule == JFileChooser.APPROVE_OPTION) {
                    f = jfc.getSelectedFile();
                }else{
                    return;
                }
                Map<String, Table> dbTableMap = getDbTableMap();
                XWPFDocument doc = DocUtils.genDbDoc(dbTableMap);
                FileOutputStream out = null;
                boolean success = true;
                try {
                    File newFile = new File(f.getParentFile().getAbsolutePath() + "/数据库文档" + new Date().getTime() + ".doc");
                    if(!newFile.exists()){
                        newFile.createNewFile();
                    }
                    out = new FileOutputStream(newFile);
                } catch (Exception ex) {
                    log.error("生成文件失败！", ex);
                    success = false;
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
                if(success){
                    SetFrame.showMsg("生成数据库文档成功！");
                }else{
                    SetFrame.showMsg("生成数据库文档失败！");
                }

            }
        });

        this.exportCheckDoc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {

                Map<String, Table> dbTableMap = getDbTableMap();
                Map<String, Table> map = new HashMap<String, Table>();
                StringBuffer sb = new StringBuffer();
                for(File file : selectFiles){
                    sb.append("******************************************************************************************");
                    sb.append("\r\n");
                    sb.append(file.getName() + "  检查情况：");
                    sb.append("\r\n");
                    map = DocUtils.readWordTableMap(file);
                    if(map == null || map.size() == 0){
                        sb.append(file.getName() + "文档不符合规范！");
                        sb.append("\r\n");
                        continue;
                    }

                    for(Map.Entry<String, Table> mapEntry : map.entrySet()){
                        if(mapEntry == null || mapEntry.getValue() == null || mapEntry.getValue().getTableName() == null){
                            continue;
                        }
                        if(!mapEntry.getValue().getTableName().contains("_")){
                            sb.append("表名：" + mapEntry.getValue().getTableName() + "不符合规范");
                            sb.append("\r\n");
                        }

                        if(!dbTableMap.containsKey(mapEntry.getKey())){
                            sb.append("数据库中没有该表存在：" + mapEntry.getValue().getTableName());
                            sb.append("\r\n");
                        }else{
                            dbTableMap.get(mapEntry.getKey()).setExistFlag(true);
                            for(Map.Entry<String, Column> cEntry : dbTableMap.get(mapEntry.getKey()).getColumns().entrySet()){
                                if(!map.get(mapEntry.getKey()).getColumns().containsKey(cEntry.getKey())){
                                    sb.append("表" + mapEntry.getKey() + "缺乏列描述：" + cEntry.getValue().getColumnName());
                                    sb.append("\r\n");
                                }
                            }
                        }
                    }
                    //System.out.println(sb.toString());

                    /**
                    for(Map.Entry<String, Table> mapEntry : dbTableMap.entrySet()){
                        if(!mapEntry.getValue().getTableName().contains("_")){
                            sb.append("表名：" + mapEntry.getValue().getTableName() + "不符合规范");
                            sb.append("\r\n");
                        }
                        if(!map.containsKey(mapEntry.getKey())){
                            if(!mapEntry.getValue().getTableName().endsWith("_bak") && !mapEntry.getValue().getTableName().startsWith("test") ){ //过滤掉备份表和测试表
                                sb.append("缺乏表描述：" + mapEntry.getValue().getTableName());
                                sb.append("\r\n");
                            }
                        }else{
                            for(Map.Entry<String, Column> cEntry : mapEntry.getValue().getColumns().entrySet()){
                                if(!map.get(mapEntry.getKey()).getColumns().containsKey(cEntry.getKey())){
                                    sb.append("表" + mapEntry.getKey() + "缺乏列描述：" + cEntry.getValue().getColumnName());
                                    sb.append("\r\n");
                                }
                            }
                        }
                    }**/

                }

                sb.append("******************************************************************************************");
                sb.append("\r\n");
                sb.append("所有文档中没有描述的表：");
                sb.append("\r\n");
                for(Map.Entry<String, Table> mapEntry : dbTableMap.entrySet()){
                    if(!mapEntry.getValue().isExistFlag()){
                        sb.append(mapEntry.getValue().getTableName());
                        sb.append("  ");
                    }
                }


                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setDialogTitle("保存数据库文档");
                File file = new File("数据库检查" + new Date().getTime()+ ".doc");
                jfc.setSelectedFile(file);//设置默认文件名
                int resule = jfc.showSaveDialog(null);
                File f = null;
                if(resule == JFileChooser.APPROVE_OPTION) {
                    f = jfc.getSelectedFile();
                }else{
                    return;
                }

                //System.out.println(stb.toString());

                boolean success = DocUtils.getWordStream(sb.toString(),
                        f.getParentFile().getAbsolutePath() + "/" + file.getName());

                if(success){
                    SetFrame.showMsg("生成数据库检查文档成功！");
                }else{
                    SetFrame.showMsg("生成数据库检查文档失败！");
                }

            }
        });

    }

    /**
     * 从库中获取所有的表
     * @return
     */
    private Map<String, Table> getDbTableMap() {
        Map<String, Table> dbTableMap = new HashMap<String, Table>();
        for(DbSetComp dc : dbSetList){
            Connection con = dc.getCon();
            if(con != null){
                Map<String, Table> map = SetFrame.getDBManager().getAllTableMap(con, dc.getSchema());
                dbTableMap.putAll(map);
            }
        }
        return dbTableMap;
    }

    //设置文件名，便于知道已经选择了哪些文件
    private void setDbFilesText(){
        if(selectFiles != null && selectFiles.size() > 0){
            String fileNames = "";
            for(File file : selectFiles){
                fileNames = fileNames + file.getName() + ";";
            }
            dbFiles.setText(fileNames);
        }else{
            dbFiles.setText("");
        }
    }

}

/**
 * 数据库设置组件
 */
class DbSetComp {

    public static int COUNT = 1;

    private String urlStr = "jdbc地址";
    private String userNameStr = "用户名";
    private String passwordStr = "密码";
    Font initFont = new Font("宋体", Font.ITALIC,12);
    Font font = new Font("宋体", Font.CENTER_BASELINE,12);


    DbSetComp(){
        this.initUI();
        this.bindUI();
    }

    private void initUI(){
        this.panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.panel.setBorder(BorderFactory.createEtchedBorder());
        jdbcUrl.setPreferredSize(new Dimension(199, 20));
        jdbcUsername.setPreferredSize(new Dimension(60, 20));
        jdbcPassword.setPreferredSize(new Dimension(60, 20));
        testConnect.setPreferredSize(new Dimension(90, 20));
        delDBSetBtn.setPreferredSize(new Dimension(25, 20));
        delDBSetBtn.setMargin(new Insets(0,0,0,0));

        jdbcUrl.setFont(initFont);
        jdbcUrl.setForeground(Color.LIGHT_GRAY);

        jdbcUsername.setFont(initFont);
        jdbcUsername.setForeground(Color.LIGHT_GRAY);

        jdbcPassword.setFont(initFont);
        jdbcPassword.setForeground(Color.LIGHT_GRAY);

        this.panel.add(jdbcUrl);
        this.panel.add(jdbcUsername);
        this.panel.add(jdbcPassword);
        this.panel.add(testConnect);
        this.panel.add(delDBSetBtn);
    }

    private void bindUI(){
        this.jdbcUrl.addFocusListener(new FocusListener() {
            //失去焦点时
            public void focusLost(FocusEvent e) {
                if("".equals(jdbcUrl.getText())){
                    jdbcUrl.setText(urlStr);
                    jdbcUrl.setFont(initFont);
                    jdbcUrl.setForeground(Color.LIGHT_GRAY);
                }
            }
            //获得焦点时
            public void focusGained(FocusEvent e) {
                if(urlStr.equals(jdbcUrl.getText())){
                    jdbcUrl.setText("");
                }
                jdbcUrl.setFont(font);
                jdbcUrl.setForeground(Color.black);
            }
        });

        this.jdbcUsername.addFocusListener(new FocusListener() {
            //失去焦点时
            public void focusLost(FocusEvent e) {
                if("".equals(jdbcUsername.getText())){
                    jdbcUsername.setText(userNameStr);
                    jdbcUsername.setFont(initFont);
                    jdbcUsername.setForeground(Color.LIGHT_GRAY);
                }

            }
            //获得焦点时
            public void focusGained(FocusEvent e) {
                if(userNameStr.equals(jdbcUsername.getText())){
                    jdbcUsername.setText("");
                }
                jdbcUsername.setFont(font);
                jdbcUsername.setForeground(Color.black);
            }
        });


        this.jdbcPassword.addFocusListener(new FocusListener() {
            //失去焦点时
            public void focusLost(FocusEvent e) {
                if("".equals(jdbcPassword.getText())){
                    jdbcPassword.setText(passwordStr);
                    jdbcPassword.setFont(initFont);
                    jdbcPassword.setForeground(Color.LIGHT_GRAY);
                }
            }
            //获得焦点时
            public void focusGained(FocusEvent e) {
                if(passwordStr.equals(jdbcPassword.getText())){
                    jdbcPassword.setText("");
                }
                jdbcPassword.setFont(font);
                jdbcPassword.setForeground(Color.black);
            }
        });

        //测试连接
        this.testConnect.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                String msg = "";
                int isConnect = isConnect();
                if(isConnect == DbManager.SUCCESS){
                    SetFrame.showMsg("连接成功！");
                }else if(isConnect == DbManager.ERROR){
                    SetFrame.showMsg("连接失败！");
                }else if(isConnect == DbManager.URL_ERROR){
                    SetFrame.showMsg("jdbc地址有误！格式jdbc:mysql://192.168.1.1:3306/cgdb");
                }else if(isConnect == DbManager.USER_NAME_OR_PASSWORD_ERROR){
                    SetFrame.showMsg("用户名或者密码有误！");
                }
            }
        });

    }

    public int isConnect(){
        return SetFrame.getDBManager().isConnect(jdbcUrl.getText(), jdbcUsername.getText(), jdbcPassword.getText());
    }

    public Connection getCon(){
        return SetFrame.getDBManager().getConnection(jdbcUrl.getText(), jdbcUsername.getText(), jdbcPassword.getText());
    }

    public String getSchema(){
        return jdbcUrl.getText().substring(jdbcUrl.getText().lastIndexOf("/") + 1);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
    }

    private JPanel panel = new JPanel();
    private JTextField jdbcUrl = new JTextField(this.urlStr);
    private JTextField jdbcUsername = new JTextField(this.userNameStr);
    private JTextField jdbcPassword = new JTextField(this.passwordStr);
    private JButton testConnect = new JButton("测试连接");

    public JButton getDelDBSetBtn() {
        return delDBSetBtn;
    }

    public void setDelDBSetBtn(JButton delDBSetBtn) {
        this.delDBSetBtn = delDBSetBtn;
    }

    // 删除数据库配置按钮
    private JButton delDBSetBtn = new JButton("-");

    public JButton getTestConnect() {
        return testConnect;
    }

    public void setTestConnect(JButton testConnect) {
        this.testConnect = testConnect;
    }


    public JTextField getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(JTextField jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public JTextField getJdbcUsername() {
        return jdbcUsername;
    }

    public void setJdbcUsername(JTextField jdbcUsername) {
        this.jdbcUsername = jdbcUsername;
    }

    public JTextField getJdbcPassword() {
        return jdbcPassword;
    }

    public void setJdbcPassword(JPasswordField jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }
}
