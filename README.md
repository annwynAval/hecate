# hecate
## mybatis相关
### AutoConvertEnumTypeHandler
替换默认的mybatis枚举转换类, 需要注意使用该转换器, 枚举必须实现DefaultEnum接口, 否则将会使用默认的AutoConvertEnumTypeHandler.

### MybatisLoggerAutoConfiguration
mybatis中sql输出插件. 使用log4j也可以实现, 但是一条sql会分成3条语句进行输出, 在高并发的情况下, 会出现混乱的情况. 该插件直接将sql中的参数进行替换, 输出完整的sql语句.

## swagger相关 
### SwaggerAutoConfiguration
对swagger进行自动配置.