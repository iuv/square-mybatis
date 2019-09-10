package com.jisuye;

import com.jisuye.core.BeanObject;
import com.jisuye.core.BeansMap;
import com.jisuye.util.DbUtil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 集成Mybatis配置类
 * @author ixx
 * @date 2019-09-08
 */
public class MybatisConfig {
    private static Logger logger = LoggerFactory.getLogger(MybatisConfig.class);
    public static void init(){
        // 获取DataSource
        DataSource dataSource = DbUtil.getDataSource();
        // 使用代码构造Mybatis配置
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        URL mapperUrl = ClassLoader.getSystemResource("mapper/");
        String path = mapperUrl.getPath();
        logger.info("mapperUrl path :{}", path);
        // 判断当前启动是在否jar包中
        if(path.indexOf("!/")>0){
            getXmlByJar(path, configuration);
        } else {
            getXmlByFile(path, configuration);
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        try {
            SqlSession session = sqlSessionFactory.openSession();
            // 循环所有Bean处理Config
            for(Class clzz : BeansMap.getClassList()) {
                Annotation[] annotations = clzz.getAnnotations();
                for (Annotation annotaion : annotations) {
                    // 如果是Mybatis的Mapper则注入到容器里
                    if(annotaion instanceof Mapper){
                        Object mapper = session.getMapper(clzz);
                        BeanObject tmpBeanObject = new BeanObject(clzz, mapper);
                        tmpBeanObject.setObject(mapper);
                        BeansMap.put(clzz.getName(), tmpBeanObject);
                    }
                }
            }
        } catch (Exception e){
            logger.error("init mybatis config error!!", e);
        }
    }

    /**
     * 本地调试获取xml配置文件
     * @param path
     * @param configuration
     */
    private static void getXmlByFile(String path, Configuration configuration){
        File mapperDir = null;
        try {
            mapperDir = new File(path);
            if(mapperDir.exists()){
                File[] files = mapperDir.listFiles();
                logger.info("files size :{}", files.length);
                for (File file : files) {
                    logger.info("file path:{}", file.getPath());
                    logger.info("file abspath:{}", file.getAbsolutePath());
                    try {
                        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(
                                new FileInputStream(file), configuration,
                                file.getPath(),
                                configuration.getSqlFragments());
                        xmlMapperBuilder.parse();
                    } catch (Exception e) {
                        e.printStackTrace(); // 出现错误抛出异常
                    }
                }
            }
        } catch (Exception e) {
            logger.error("load mapper xml file error!", e);
        }
    }

    /**
     * 处理jar包中的xml配置文件（打包后启动）
     */
    private static void getXmlByJar(String path, Configuration configuration){
        logger.info("xml get by path:{}", path);
        path = path.substring(0, path.indexOf("!/")).replace("file:", "");
        logger.info("xml get by jar path:{}", path);
        try {
            JarFile jarFile = new JarFile(path);
            Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
            while (jarEntryEnumeration.hasMoreElements()){
                JarEntry jarEntry = jarEntryEnumeration.nextElement();
                String name = jarEntry.getName();
                // logger.info("file name ====={}", name);
                if(name.startsWith("mapper/") && name.endsWith(".xml")){
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(
                            ClassLoader.getSystemResourceAsStream(name), configuration,
                            name,
                            configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
