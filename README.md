# hecate
## mybatis相关
### AutoConvertEnumTypeHandler
替换默认的mybatis枚举转换类, 需要注意使用该转换器, 枚举必须实现DefaultEnum接口, 否则将会使用默认的AutoConvertEnumTypeHandler.
#### 如何使用
1. mybatis配置中, default-enum-type-handler配置设定为com.annwyn.hecate.mybatis.handler.AutoConvertEnumTypeHandler
2. 枚举实现DefaultEnum接口

### MybatisLoggerAutoConfiguration
mybatis中sql输出插件. 使用log4j也可以实现, 但是一条sql会分成3条语句进行输出, 在高并发的情况下, 会出现混乱的情况. 该插件直接将sql中的参数进行替换, 输出完整的sql语句.
#### 如何使用
1. 配置com.annwyn.mybatis.logger.enable为true, 表示开启sql输出功能
2. 在需要打印sql的mapper接口上添加PrintSql注解.
3. 配置com.annwyn.mybatis.logger.enable.print-sql-if-missing为true, 表示不添加注解的情况下也打印sql

## swagger相关 
### SwaggerAutoConfiguration
对swagger进行自动配置.
#### 如何使用
1. 配置com.annwyn.swagger.enable为true表示开启swagger
2. 配置title, version等参数. 具体查看com.annwyn.hecate.swagger.SwaggerProperties