# 数据库文档检查与编写工具

## 开发目标
* 方便数据库文档的检查
* 方便数据库文档的编写

## 遗留问题
* 暂时只支持MySQL

## 使用说明
* 前提：在使用的电脑上安装jkd1.7+ （或jre1.7+）
* 双击jar包即可
* 界面简单明了，无需说明
* 日志位置一般需要修改,找到jar包中BOOT-INF/classes/log4j.properties

## 文档表格规范
* 推荐使用word2007/2010 *.docx

XXX表（表名）(可选信息)<br>

键 | 字段描述 | 字段名 | 数据类型 | 字段长度 | 是否可空 | 备注
--- | --- | --- | --- | --- | --- | ---
F/P | 中文描述 | 列物理名称 | varchar/int/bigint/longtext/datetime/float/double/long/等 | 长度/精度 | NULL/NOTNULL | 