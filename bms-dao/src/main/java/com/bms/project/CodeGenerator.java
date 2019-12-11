package com.bms.project;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liqiang
 */
public class CodeGenerator {

    private static DataSourceConfig getDataSourceConfig() {
        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setDbType(DbType.MYSQL);
        // 自定义数据库表字段类型转换【可选】
        dsc.setTypeConvert(new MySqlTypeConvert() {
            @Override
            public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
                return super.processTypeConvert(globalConfig, fieldType);
            }
        });
        dsc.setUrl("jdbc:mysql://localhost:3306/bms?autoReconnect=true&useCompression=true&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&useSSL=true");
        dsc.setDriverName("com.mysql.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("laij082699");
        return dsc;
    }

    public static void main(String[] args) {
        String projectPath = new File("").getAbsolutePath() + "/bms-dao";
        String moduleName = "";
        final String mapperPath = projectPath + "/src/main/resources/com/bms/project/" + moduleName + "/mapper/autoSql/";
        //多个以英文逗号分隔
        String tables = "bms_login_log";
        //表的前缀，生成类名，会去掉这个前缀
        String tablePrefix = "";

        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();
        mpg.setTemplateEngine(new VelocityTemplateEngine());
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setOutputDir(projectPath + "/src/main/java");
        gc.setAuthor("autoGenerator");
        gc.setOpen(false);
        gc.setIdType(IdType.ID_WORKER);
        gc.setFileOverride(true);
        gc.setBaseResultMap(true);
        gc.setBaseColumnList(false);
        mpg.setGlobalConfig(gc);
        mpg.setDataSource(getDataSourceConfig());

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.bms.project.generator");
        pc.setModuleName(moduleName);
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        // 如果模板引擎是 velocity
        String templatePath = "/templates/mapper.xml.vm";
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名
                return mapperPath + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setXml(null);
        templateConfig.setController(null);
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setRestControllerStyle(true);
        String[] includes = tables.split(",");
        strategy.setInclude(includes);
        strategy.setTablePrefix(tablePrefix);
        strategy.setEntityLombokModel(true);
        mpg.setStrategy(strategy);
        mpg.setConfig(new ConfigBuilder(mpg.getPackageInfo(), mpg.getDataSource(), mpg.getStrategy(), mpg.getTemplate(), mpg.getGlobalConfig()));
        List<TableInfo> tableList = mpg.getConfig().getTableInfoList();
        for (TableInfo table : tableList) {
            table.setEntityName(table.getEntityName() + "Mp");
            table.setMapperName(table.getEntityName() + "Mapper");
            table.setXmlName(table.getEntityName() + "Mapper");
            table.setServiceName("I" + table.getEntityName() + "Service");
            table.setServiceImplName(table.getEntityName() + "ServiceImpl");
        }
        mpg.execute();
    }

}
