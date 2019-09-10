# square-mybatis
该项目为square框架集成Mybatis的插件项目，使用前置依赖 square项目

## 一、使用说明
### 1. 获取代码
本地`git clone `或下载代码
### 2. 打包编译
在项目目录下执行`mvn clean install`
### 3. 添加依赖
在需要引用Mybatis的项目里添加Mybatis及square-mybatis依赖
```xml
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.5.2</version>
        </dependency>
        <dependency>
            <groupId>com.jisuye</groupId>
            <artifactId>square-mybatis</artifactId>
            <version>0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.12</version>
        </dependency>
```
### 4. 添加配置
添加配置文件InitConfig.java
```java
package com.jisuye.config;
import com.jisuye.MybatisConfig;
import com.jisuye.annotations.Config;
import com.jisuye.core.SquareConfig;

@Config
public class InitConfig implements SquareConfig {
    @Override
    public void config() {
        MybatisConfig.init();
    }

}

```
### 5. 启动测试
记得添加application.yml中的mysql配置：
```yaml
square:
  datasource:
    url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&useSSL=false&autoReconnect=true
    username: root
    password: 123456
```
自行添加Mybatis 接口及mapper测试即可
    password: 123456
