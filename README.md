# guli_mall_back
电商项目

# 1 微服务架构图

![image-20220805103847332](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20220805103847332.png)

2 微服务划分图

![image-20220805104348553](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20220805104348553.png)

# 2 docker 启动Mysql

docker run --name mysql -v /mydata/mysql/data:/var/lib/mysql -v  /mydata/mysql/conf:/etc/mysql/conf -v  /mydata/mysql/log:/var/log/mysql  -e MYSQL_ROOT_PASSWORD=root  -p 3306:3306 -d mysql:5.7

在/mydata/mysql/conf目录下创建my.conf

[client]
default-character-set=utf8

[mysql]
default-character-set=utf8

[mysqld]
init_connect='SET collation_connection = utf8_unicode_ci'
init_connect='SET NAMES utf8'
character-set-server=utf8
collation-server=utf8_unicode_ci
skip-character-set-client-handshake
skip-name--resolve

# 3 docker 启动 redis

mkdir -p /mydata/redis/conf 

touch /mydata/redis/conf/redis.conf

docker run --name redis -p 6379:6379 -v /mydata/redis/data:/data -v /mydata/redis/conf/redis.conf:/etc/redis/redis.conf -d redis redis-server /etc/redis/redis.conf

docker exec -it redis  redis-cli 进入redis

redis 数据默认是存在缓存里的，要保持持久化需要在配置文件redis.conf修改

appendonly yes 然后重启redis容器 重启命令 docker restart redis

# 4 搭建项目基本框架

1 先创建一个空的项目,在主项目下，创建一个个Modules，通过renren-generator生成模块代码。

```xml
<modules>
  <module>gulimall-coupon</module>
  <module>gulimall-member</module>
  <module>gulimall-order</module>
  <module>gulimall-product</module>
  <module>gulimall-ware</module>
  <module>renren-fast</module>
  <module>renren-generator</module>
  <module>gulimall-common</module>
</modules>
```

# 5 微服务

![image-20220815101955205](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20220815101955205.png)

## 配置Spring-cloud-alibaba

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>2.2.8.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## nacos注册发现

127.0.0.1:8848/nacos

账号密码都是nacos

下载地址：https://github.com/alibaba/nacos/releases

依赖引入

```xml
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
```

下载完成之后解压进入bin 目录 启动命令 startup.cmd -m standalone 因为默认是集群启动所以直接打开会启动失败。

nacos配置

name必须得配置

```yml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
  application:
    name: gulimall-coupon
```

## 如何调用其他微服务

创建一个feign文件夹，在里面的接口加上一个注解@FeignClient，value为调用的微服务的名称

接口也是需要调用方法的名字，相同的复制过来。

在启动类上加上下面这个注解扫描feign下的接口

```java
@EnableFeignClients("com.hzk.gulimall.member.feign")
```

```java
coupon 下
@RequestMapping("/getOne")
    public R getCoupons(){
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("满100-10");
        couponEntity.setAmount(new BigDecimal("100"));
        return R.ok().put("coupons",couponEntity);
    }


member下
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/getOne")
    public R getCoupons();
}

 @Autowired
    CouponFeignService couponFeignService;
    @RequestMapping("/getMem")
    public R getMem(){
        R coupons = couponFeignService.getCoupons();
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        return R.ok().put("member",memberEntity).put("coupon",coupons.get("coupons"));
    }
```

## nacos配置管理

### 1添加依赖

```xml
<!--配置管理-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

### 2 创建bootstrap.properties文件

```properties
spring.application.name=gulimall-coupon
spring.cloud.nacos.config.server-addr=127.0.0.1:8848
```

### 3 在nacos配置中心添加一个默认数据集(Data Id) 

名称为 gulimall-coupon.proerties，默认规则。

### 4 动态获取配置值

利用@RefreshScope 以及 @Value("${配置名称}") 如果当前项目的配置和配置中心配置相同，优先获取配置中心的数据。

### 细节

#### 命名空间

作用：隔离配置，可以根据每个微服务创建对应的命名空间

![image-20220815162351682](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20220815162351682.png)

#### 组

在命名空间下，可通过组来配置不同环境的配置文件。

![image-20220815162413517](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20220815162413517.png)

#### 设置组和命名空间

```properties
spring.cloud.nacos.config.namespace=edd0be4c-3a5f-43ed-add6-7bacbf35e4fc
spring.cloud.nacos.config.group=prod
```

#### 拆分配置

![image-20220815164301809](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20220815164301809.png)

```properties
spring.cloud.nacos.config.extension-configs[0].data-id=nacos.yml
spring.cloud.nacos.config.extension-configs[0].refresh=true
spring.cloud.nacos.config.extension-configs[0].group=dev

spring.cloud.nacos.config.extension-configs[1].data-id=mybatis.yml
spring.cloud.nacos.config.extension-configs[1].refresh=true
spring.cloud.nacos.config.extension-configs[1].group=dev

spring.cloud.nacos.config.extension-configs[2].data-id=datasource.yml
spring.cloud.nacos.config.extension-configs[2].refresh=true
spring.cloud.nacos.config.extension-configs[2].group=dev
```

## 配置Spring-cloud-getway API网关

![image-20220816100600691](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20220816100600691.png)

网关的作用是根据前端发送来的请求进行处理，在将请求分发到不同的路由进行处理。

官网地址：[Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)

路由配置

```yml
spring:
  cloud:
    gateway:
      routes:
        - id: baidu_route
          uri: https://www.baidu.com
          predicates:
            - Query=params, baidu

        - id: qq_route
          uri: https://www.qq.com
          predicates:
            - Query=params, qq
```

# 商品服务

## 1 网关配置

在网关微服务下配置

```yml
spring:
  cloud:
    gateway:
      routes:
       #提高该路径匹配的优先级
        - id: product_route
          uri: lb://gulimall-product
          predicates:
            - Path=/api/product/**
          filters:
            - RewritePath=/api/?(?<segment>.*),/$\{segment}

        - id: admin_route
          uri: lb://renren-fast
          predicates:
            - Path=/api/**
          filters:
            - RewritePath=/api/?(?<segment>.*),/renren-fast/$\{segment}
```

## 2 查询树状列表

```java
@Override
public List<CategoryEntity> listWithTree() {

    List<CategoryEntity> entities = baseMapper.selectList(null);
    //1 先查出所有父级分类
    //2 通过递归为每个分类获得它的子分类
    List<CategoryEntity> list = entities.stream().filter(categoryEntity -> (
            //只有父级分类的parentCid为0
            categoryEntity.getParentCid() == 0
    )).map(categoryEntity -> {
        //为每个categoryEntity 设置它的子分类
        categoryEntity.setChildren(getChildren(categoryEntity, entities));
        return categoryEntity;
    }).sorted((categoryEntity1, categoryEntity2) -> {
        return (categoryEntity1.getSort() == null ? 0 : categoryEntity1.getSort()) - (categoryEntity2.getSort() == null ? 0 : categoryEntity2.getSort());
    }).collect(Collectors.toList());
    return list;
}

private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> entities) {
   List<CategoryEntity> list =  entities.stream().filter(categoryEntity -> {
        return root.getCatId() == categoryEntity.getParentCid();
    }).map(categoryEntity -> {
        categoryEntity.setChildren(getChildren(categoryEntity,entities));
        return categoryEntity;
   }).sorted((categoryEntity1, categoryEntity2) -> {
       return (categoryEntity1.getSort() == null ? 0 : categoryEntity1.getSort()) - (categoryEntity2.getSort() == null ? 0 : categoryEntity2.getSort());
   }).collect(Collectors.toList());
    return list;
}
```

## 3 逻辑删除字段

```java
@TableLogic(value = "1",delval = "0")  如果为1则表示逻辑上未删除 0则表示逻辑上删除
private Integer showStatus;
```

# 配置阿里云OSS

1 现在阿里云配置开启oss服务，并且创建一个bucket

![image-20220825151313046](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20220825151313046.png)

2 在个人中心选择accesskey管理

创建用于 保存accesskey 以及secretkey

![image-20220825151410005](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20220825151410005.png)

3 springboot整合aliyun-oss

引入依赖

```pom
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alicloud-oss</artifactId>
    <version>2.2.0.RELEASE</version>
</dependency>
```

配置

```yaml
spring:
 	cloud:
    	alicloud:
      	  access-key: LTAI5t6VaLmeM6FdE7mRC8xg
     	  secret-key: aBpCYYUhLDoCnDjxUryCVhvSQPM11f
     	  oss:
            endpoint: oss-cn-hangzhou.aliyuncs.com
```

demo测试

```java
 @Resource
    OSSClient ossClient;
@Test
void test() throws FileNotFoundException {

    // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
    // 填写Bucket名称，例如examplebucket。
    String bucketName = "gulimall-kee";
    // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
    String objectName = "oppo.png";
    // 创建OSSClient实例。
    

    // 本地文件。
    String content = "D:\\guli_mall\\源码\\docs\\pics\\oppo.png";
    FileInputStream fileInputStream = new FileInputStream(new File(content));
    // 创建PutObjectRequest对象。
    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, fileInputStream);

    // 上传字符串。
    ossClient.putObject(putObjectRequest);
    if (ossClient != null) {
        ossClient.shutdown();
    }
    System.out.println("上传完成");

}
```

## 配置绕过后端上传

```java
package com.example.thirdpart.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.hzk.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author kee
 * @version 1.0
 * @date 2022/8/25 16:40
 */
@RestController("thirdpart")
public class OSSController {

    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endPoint;
    @Value("${spring.cloud.alicloud.oss.bucket}")
    private String bucket;

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessId;
    @Autowired
    OSS ossClient;

    @RequestMapping("/oss/policy")
    public R policy() {

        // 填写Host地址，格式为https://bucketname.endpoint。
        String host = "https://" + bucket + "." + endPoint;
        // 设置上传回调URL，即回调服务器地址，用于处理应用服务器与OSS之间的通信。OSS会在文件上传完成后，把文件上传信息通过此回调URL发送给应用服务器。
        //String callbackUrl = "https://192.168.0.0:8888";
        // 设置上传到OSS文件的前缀，可置空此项。置空后，文件将上传至Bucket的根目录下。
        String format = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dir = format + "/";

        // 创建OSSClient实例。
        Map<String, String> respMap = null;
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));


        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        }
        return R.ok().put("data",respMap);
    }

}
```

# 前后端校验

## 前端校验

通过callback回调函数显示错误信息

```js
firstLetter: [
  {
    validator: (rule, value, callback) => {
      if (value === '') {
        callback(new Error('首字母必须填写'))
      } else if (!/^[a-zA-Z]$/.test(value)) {
        callback(new Error('首字母必须在a-z,A-Z之间'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
],
```

## 后端校验

## JSR303校验

依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
    <version>2.3.7.RELEASE</version>
</dependency>
```

在字段上添加注解

```java
package com.hzk.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
/**
 * 品牌
 * 
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-11 11:13:31
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * 品牌id
    */
   @TableId
   private Long brandId;
   /**
    * 品牌名
    */
   @NotBlank(message = "品牌名不能为空")
   private String name;
   /**
    * 品牌logo地址
    */
   @NotBlank
   @URL(message = "必须是一个合法的Url地址")
   private String logo;
   /**
    * 介绍
    */
   private String descript;
   /**
    * 显示状态[0-不显示；1-显示]
    */
   private Integer showStatus;
   /**
    * 检索首字母
    */
   @NotBlank
   @Pattern(regexp = "^[a-zA-Z]$")
   private String firstLetter;
   /**
    * 排序
    */
   @NotNull
   @Min(value = 0)
   private Integer sort;

}
```

在controller接收层接收错误

```java
@RequestMapping("/save")
public R save(@Valid @RequestBody BrandEntity brand, BindingResult bindingResult) {

    Map<String, String> map = new HashMap<>();
    if (bindingResult.hasErrors()) {
        bindingResult.getFieldErrors().forEach((item) -> {
            map.put(item.getField(), item.getDefaultMessage());
        });
        return R.error(400, "提交的数据不合法").put("data", map);
    } else {
        brandService.save(brand);

        return R.ok();
    }

}
```

## 集中校验

通过@RestControllerAdvice(basePackages = "com.hzk.gulimall.product.controller") 将controller层抛出的异常统一接收，在对异常做出不同的处理

```java
package com.hzk.gulimall.product.exception;

import com.hzk.common.exception.BizCodeEnum;
import com.hzk.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kee
 * @version 1.0
 * @date 2022/8/29 16:22
 */
//@RestControllerAdvice = @ControllerAdvice + @ResponseBody
@Slf4j
@RestControllerAdvice(basePackages = "com.hzk.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handlerValidationException(MethodArgumentNotValidException e) {
        log.error("异常信息：{}，异常类型:{}", e.getMessage(), e.getClass());
        Map<String, String> map = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach((fieldError) -> {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handlerException(Throwable e){
        return R.error(BizCodeEnum.UNKONW_EXCEPTION.getCode(), BizCodeEnum.UNKONW_EXCEPTION.getMsg());
    }
}
```

```java
@RequestMapping("/save")
public R save(@Valid @RequestBody BrandEntity brand) {

    brandService.save(brand);

    return R.ok();
}
```

## 分组校验

编写两个接口AddGroup,UpdateGroup 并在controller接口层通过@Validated（接口名.class）

来指定选用什么组进行校验

```java
package com.hzk.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hzk.common.valid.AddGroup;
import com.hzk.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;
/**
 * 品牌
 * 
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-11 11:13:31
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * 品牌id
    */
   @NotNull(message = "修改必须提交品牌Id",groups = {UpdateGroup.class})
   @Null(message = "新增不能指定Id",groups = {AddGroup.class})
   @TableId
   private Long brandId;
   /**
    * 品牌名
    */

   @NotBlank(message = "品牌名必须提交",groups = {UpdateGroup.class,AddGroup.class})
   private String name;
   /**
    * 品牌logo地址
    */
   @NotBlank(groups = {AddGroup.class})
   @URL(message = "必须是一个合法的Url地址",groups = {UpdateGroup.class,AddGroup.class})
   private String logo;
   /**
    * 介绍
    */
   private String descript;
   /**
    * 显示状态[0-不显示；1-显示]
    */
   private Integer showStatus;
   /**
    * 检索首字母
    */
   @NotEmpty(groups = {AddGroup.class,UpdateGroup.class})
   @Pattern(message = "首字母必须在a-zA-Z之间",regexp = "^[a-zA-Z]$",groups = {AddGroup.class,UpdateGroup.class})
   private String firstLetter;
   /**
    * 排序
    */
   @NotNull(groups = {AddGroup.class,UpdateGroup.class})
   @Min(message = "排序值不能小于0",value = 0,groups = {AddGroup.class,UpdateGroup.class})
   private Integer sort;

}
```

```java
@RequestMapping("/save")
public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand) {
    brandService.save(brand);

    return R.ok();
}
```

```java
@RequestMapping("/update")
public R update(@Validated(UpdateGroup.class)@RequestBody BrandEntity brand) {
    brandService.updateById(brand);

    return R.ok();
}
```

## 自定义校验

先编写一个注解，然后在编写自己的校验器,校验器要实现ConstraintValidator接口并重写两个方法

```java
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface ListValue {

    String message() default "{com.hzk.common.valid.ListValue.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    int[] vals() default { };
}
```

校验器

```java
package com.hzk.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @author kee
 * @version 1.0
 * @date 2022/8/29 17:19
 */
// 校验器
public class ListConstraintValidator implements ConstraintValidator<ListValue,Integer> {
    Set<Integer> set = new HashSet<>();
    @Override
    public void initialize(ListValue constraintAnnotation) {
        //ConstraintValidator.super.initialize(constraintAnnotation);
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
                set.add(val); 
        }
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        
        return set.contains(value);
    }
}
```

# Elasticsearch

https://www.elastic.co/guide/cn/elasticsearch/guide/current/index.html



[Elasticsearch Guide [7.4\] | Elastic](https://www.elastic.co/guide/en/elasticsearch/reference/7.4/index.html) 7.4版本

官方中文文档

## 索引

类似mysql 数据库

## 类型

类似数据表

## 文档

类似mysql一条记录

## 安装

### Elasticsearch

docker pull elasticsearch:7.4.2

docker pull kibana:7.4.2

mkdir -p /mydata/elasticsearch/config

mkdir -p /mydata/elasticsearch/data

echo "http:host:0.0.0.0" >> /mydata/elasticsearch/config/elasticsearch.yml

docker run --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e ES_JAVA_OPTS="-Xms64m -Xmx512m" -v /mydata/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml -v /mydata/elasticsearch/data:/usr/share/elasticsearch/data -v /mydata/elasticsearch/plugins:/usr/share/elasticsearch/plugins -d elasticsearch:7.4.2                                   

elasticsearch容器卷挂在的时候会有权限问题需要吧外面的文件权限设置rwx 既 chmod -R 777 /mydata/elasticsearch (-R 表示递归将该目录下的所有文件的权限设置为可读可写可执行)

### Kibana

docker run --name kibana -e ELASTICSEARCH_HOSTS=http://192.168.56.10:9200 -p 5601:5601 -d kibana:7.4.2

docker update 容器id --restart=always

## 初步检索(入门)

### _cat

GET/_cat/nodes 查看素有节点

GET/_cat/health 查看es健康状况

GET/_cat/master 查看主节点

GET/_cat/indices 查看所有索引  有点类似mysql的show dabases；

### 索引一个文档(保存)

PUT http://192.168.56.10:9200/costmer/externale/1

当发送put请求必须携带Id，如果该Id不存在则创建，存在则为更新操作

POST http://192.168.56.10:9200/costmer/externale/1

post请求可以不携带Id，如果不带id则为创建，带id则为更新操作

### 查询一个文档

GET http://192.168.56.10:9200/costmer/externale/1

```json
{
    "_index": "costmer",
    "_type": "externale",
    "_id": "1",
    "_version": 3,
    "_seq_no": 3,
    "_primary_term": 1,
    "found": true,
    "_source": {
        "name": "jack"
    }
}
```

如果并发情况下有2至多个人想修改某一条文档，那么可以根据_seq_no(版本号)来进行修改

乐观锁修改 http://192.168.56.10:9200/costmer/externale/1?if_seq_no=3&if_primary_term=1

如果冲突则返回409confilict

```json
{
    "error": {
        "root_cause": [
            {
                "type": "version_conflict_engine_exception",
                "reason": "[1]: version conflict, required seqNo [4], primary term [1]. current document has seqNo [5] and primary term [1]",
                "index_uuid": "39pt7iGVT1qqsZ6W3XjE3w",
                "shard": "0",
                "index": "costmer"
            }
        ],
        "type": "version_conflict_engine_exception",
        "reason": "[1]: version conflict, required seqNo [4], primary term [1]. current document has seqNo [5] and primary term [1]",
        "index_uuid": "39pt7iGVT1qqsZ6W3XjE3w",
        "shard": "0",
        "index": "costmer"
    },
    "status": 409
}
```



### 更新文档

post请求方式的更新文档 会对比元数据，如果没有变化则不会进行操作

put请求方式的更新文档 则不会对比，有没有变化都会进行更新操作。

方式 1 POST http://192.168.56.10:9200/costmer/externale/1/_update

```json
{
	"doc":{
		"name": "john"
	}
}
```

方式2 POST  http://192.168.56.10:9200/costmer/externale/1

```json
{
   "name": "john"
}
```

方式3 PUT http://192.168.56.10:9200/costmer/externale/1

```json
{
   "name": "john",
    "age": "age"
}
```

### 删除文档&索引

在es中没有删除类型这一操作，只能删除文档或者索引

DELETE  http://192.168.56.10:9200/costmer/externale/1 删除文档

DELETE  http://192.168.56.10:9200/costmer 删除索引

```json
//删除成功返回的数据
{
    "acknowledged": true
}
```



### **bulk** **批量** **API** 

```json
POST customer/external/_bulk
{"index":{"_id":"1"}}
{"name": "John Doe" }
{"index":{"_id":"2"}}
{"name": "Jane Doe" }
语法格式：
{ action: { metadata }}\n
{ request body }\n
{ action: { metadata }}\n
{ request body }\n
复杂实例：
POST /_bulk
{ "delete": { "_index": "website", "_type": "blog", "_id": "123" }}
{ "create": { "_index": "website", "_type": "blog", "_id": "123" }}
{ "title": "My first blog post" }
{ "index": { "_index": "website", "_type": "blog" }}
{ "title": "My second blog post" }
{ "update": { "_index": "website", "_type": "blog", "_id": "123" }
{ "doc" : {"title" : "My updated blog post"} }
```

## 进阶

### 查询的两种方式

1 

```json
GET /bank/_search?pretty
{
	
  "query": { "match_all": {} },

  "sort": [
	// 排序规则
    { "account_number": "asc" },
    { "balance": "desc"}

  ]

}
```

2

```
GET /bank/_search?q=*&sort=account_number:asc
```

### QueryDSL

#### matchAll

```json
GET /bank/_search
{
	
  "query": { "match_all": {} },

  "sort": [
    { "account_number": "asc" },
    { "balance": "desc"}

  ],
    //分页查询
  "from": 0,
  "size": 5,
    //想要查询的字段
  "_source": ["age","gender"]

}
```

#### match

```json
GET /bank/_search
{
  "query": {
    "match": {
        //可以精确匹配，也可以模糊匹配（倒排索引）会对检索条件进行分词匹配
      "address": "Columbus"
    }
  }
}
```

#### match_phrase（短语）

```json
GET /bank/_search
{
  "query": {
    "match_phrase": {
        //不会再而分词匹配，而是当成一句短语
      "address": "Holmes Lane"
    }
  }
}
```

#### multi_match(多字段匹配)

```json
GET /bank/_search
{
  "query": {
    "multi_match": {
        //匹配信息 会进行分词匹配
      "query": "Holmes",
        //匹配字段
      "fields": ["address","gender"]
    }
  }
}
```

#### bool复合查询

```json
GET /bank/_search
{
  "query": {
    "bool": {
        //必须匹配
      "must": [
        {
          "match": {
            "gender": "M"
          }
        }
      ],
        //必须不能匹配
      "must_not": [
        {"match": {
          "age": "20"
        }}
      ],
        //应该匹配,有最好，没有也行
      "should": [
        {
          "match": {
            "state": "TEXT"
          }
        }
      ]
    }
  }
}


```

#### filter

filter不参与得分

```json
GET /bank/_search
{
  "query": {
    "bool": {
      "filter": {
        "range": {
          "age": {
              //大于
            "gte": 10,
              //小于
            "lte": 20
          }
        }
      }
    }
  }
}

```

#### term

和match一样。匹配某个属性的值。全文检索字段用match，其他非text字段匹配用term

```json
GET /bank/_search
{
  "query": {
    "match": {
        //keyworkd 需要完全匹配
     "address.keyword": "880 Holmes"
    }
  }
}

GET /bank/_search
{
  "query": {
    "term": {
     "address": "880 Holmes"
    }
  }
}

```

#### aggregation(执行聚合)

例1

```json
##统计地址中包含mill的年龄分布以及平均年龄
GET bank/_search
{
  "query": {
    "match": {
      "address": "mill"
    }
  },
  "aggs": {
    "addrAgg": {
      "terms": {
        "field": "age",
        "size": 10
      }
    },
    "ageAvg":{
      "avg": {
        "field": "age"
      }
    }
  }
}
```

例2

```json
##按照年龄聚合，并且请求这写年龄段的人的平均薪资
GET bank/_search
{
  "query": {
    "match_all": {}
  }, 
 
  "aggs": {
    "ageAgg": {
      "terms": {
        "field": "age",
        "size": 100
      },
      "aggs": {
        "balanceAgee": {
          "avg": {
            "field": "balance"
          }
        }
      }
    }
  }
}
```

例3

```json
##查出所有年龄分布，并且这些年龄中M的平均薪资和F的平均薪资以及这个年龄段的平均薪资

GET bank/_search
{
  "query": {"match_all": {}},
  "aggs": {
    "ageAgg": {
      "terms": {
        "field": "age",
        "size": 100
      },
      "aggs": {
        "genderAgg": {
          "terms": {
            "field": "gender.keyword",
            "size": 10
          },
          "aggs": {
            "balanceAgg": {
              "avg": {
                "field": "balance"
              }
            }
          }
        },
            "agebalanceAgg":{
        "avg": {
          "field": "balance"
          }
        }
      }
    }
  }
}
```

### Mapping(映射)

#### 字段类型

**string**

[`text`](https://www.elastic.co/guide/en/elasticsearch/reference/7.4/text.html) and [`keyword`](https://www.elastic.co/guide/en/elasticsearch/reference/7.4/keyword.html)

**[Numeric](https://www.elastic.co/guide/en/elasticsearch/reference/7.4/number.html)**

```
long`, `integer`, `short`, `byte`, `double`, `float`, `half_float`, `scaled_float
```

**[Date](https://www.elastic.co/guide/en/elasticsearch/reference/7.4/date.html)**

```
date
```

**[Date nanoseconds](https://www.elastic.co/guide/en/elasticsearch/reference/7.4/date_nanos.html)**

```
date_nanos
```

**[Boolean](https://www.elastic.co/guide/en/elasticsearch/reference/7.4/boolean.html)**

```
boolean
```

**[Binary](https://www.elastic.co/guide/en/elasticsearch/reference/7.4/binary.html)**

```
binary
```

**[Range](https://www.elastic.co/guide/en/elasticsearch/reference/7.4/range.html)**

```
integer_range`, `float_range`, `long_range`, `double_range`, `date_range
```

#### 添加一个映射

```JSON
PUT /my-index
{
  "mappings": {
    "properties": {
      "age": {
        "type": "integer"
      },
      "email": {
        "type": "keyword"
      },
      "name": {
        "type": "text"
      }
    }
  }
}
```

#### 给映射添加新字段

```JSON
PUT /my_index/_mapping
{
  "properties": {
    "employee-id": {
      "type": "keyword",
      "index": false
    }
  }
}
```

#### 更新映射字段

如果该字段已存在想修改类型，只能创建新的索引进行数据迁移。不能直接更新

### 分词

分词器

https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.4.2/elasticsearch-analysis-ik-7.4.2.zip 

安装的时候kibana出现了一个小问题ip地址对应不上

首先找到es内网地址 docker inspect elasticsearch  |grep IPAddress

把之前的Kibana容器删除 docker重新运行一个新的容器ip地址设置为内网地址

docker run --name kibana -e ELASTICSEARCH_HOSTS=http://上一步查出来的ip:9200 -p 5601:5601 -d kibana:7.4.2 

```json
POST _analyze
{
  "analyzer": "ik_smart",
  "text": "我是中国人"
}

```

#### 自定义词库

修改/usr/share/elasticsearch/plugins/ik/config/中的 IKAnalyzer.cfg.xml 

/usr/share/elasticsearch/plugins/ik/config 

这个自定义分词的文件在nginx/html/es/fenci.txt 需要提前创建。

```properties
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
        <comment>IK Analyzer 扩展配置</comment>
        <!--用户可以在这里配置自己的扩展字典 -->
        <entry key="ext_dict"></entry>
         <!--用户可以在这里配置自己的扩展停止词字典-->
        <entry key="ext_stopwords"></entry>
        <!--用户可以在这里配置远程扩展字典 -->
         
         <entry key="remote_ext_dict">http://192.168.56.10/es/fenci.txt</entry>
        <!--用户可以在这里配置远程扩展停止词字典-->
        <!-- <entry key="remote_ext_stopwords">words_location</entry> -->
</properties>
```



## 配置nginx

随便启动一个 nginx 实例，只是为了复制出配置 

 docker run -p 80:80 --name nginx -d nginx:1.10 

将容器内的配置文件拷贝到当前目录：docker container cp nginx:/etc/nginx . 

别忘了后面的点 

修改文件名称：mv nginx conf 把这个 conf 移动到/mydata/nginx 下 

终止原容器：docker stop nginx 

执行命令删除原容器：docker rm $ContainerId 

创建新的 nginx；执行以下命令 

docker run -p 80:80 --name nginx \ 

-v /mydata/nginx/html:/usr/share/nginx/html \ 

-v /mydata/nginx/logs:/var/log/nginx \ 

-v /mydata/nginx/conf:/etc/nginx \ 

-d nginx:1.10

## springboot整合ElasticSearch

```xml
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>7.4.2</version>
</dependency>
```

配置ES的地址

```java
@Configuration
public class GulimallElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        //builder.addHeader("Authorization", "Bearer " + TOKEN);
        //builder.setHttpAsyncResponseConsumerFactory(
        //        new HttpAsyncResponseConsumerFactory
        //                .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.56.10", 9200, "http")));
        return client;
    }

}
```

向ES中写入数据

```java
@Override
public boolean productStatusUp(List<SkuEsModel> models) throws IOException {
    BulkRequest bulkRequest = new BulkRequest();
    for (SkuEsModel model : models) {
        IndexRequest request = new IndexRequest(EsConstant.PRODUCT_INDEX);
        request.id(model.getSkuId().toString());
        String str = JSONObject.toJSONString(model);
        request.source(str, XContentType.JSON);
        bulkRequest.add(request);
    }

    BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
    //TODO 批量处理错误
    boolean b = bulk.hasFailures();
    List<String> list = Arrays.stream(bulk.getItems()).map(item -> {
        return item.getId();
    }).collect(Collectors.toList());
    log.info("商品上架成功: {}", list);
    return b;
}
```

# SpringBoot整合Redis

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yml
spring:
	redis:
   	 	host: 192.168.56.10 #配置的ip地址
    	port: 6379 #暴露端口
```

## 分布式锁

```java
String uuid = UUID.randomUUID().toString();
// 加锁要原子性操作
Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 50, TimeUnit.SECONDS);
```

```java
//原子性解锁
String script =  "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
```

# Spring-Cache不足

## 读模式

缓存穿透 ： 查询一个Null值的数据，解决办法：缓存空数据 spring.cache.redis.cache-null-values=true

缓存击穿：大量并发进来同时查询一个刚好过期的数据，解决办法：加分布式锁 (redisson)

缓存雪崩：大量的key同时过期 解决：加过期时间 spring.cache.redis.time-to-live=3600000

## 写模式（缓存与数据库一致）

1. 加读写锁
2. 引入canal,感知到Mysql的更新去更新数据库
3. 读多写多，直接去数据库查询就行

# Redisson分布式锁

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version>3.17.7</version>
</dependency>
```

配置

```yml
redis:
  host: 192.168.56.10
  port: 6379
```



```java
/**
 * Redisson 分布式锁
 *
 * @return
 */
public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
    RLock lock = redisson.getLock("CatelogJson-lock");
    // 加锁
    lock.lock();
    Map<String, List<Catelog2Vo>> catelogJson = null;
    try {
        catelogJson = getDataFromDb();
    } finally {
        lock.unlock();
    }

    return catelogJson;

}
```

# 并发（异步）

## 4种线程开启方法

```java
public class ThreadTest {

    public static void main(String[] args) {
        //1  实现Runnable接口
        new Thread(new Runnable01()).start();
        //2  继承 Thread类
        new Thread01().start();
        //3  实现Callable接口 这个接口可以接收一个泛型可以获得返回值
        // 但是要获取这个返回值 程序就会变成同步了 会等待线程执行完获得返回值 整个程序才会结束。
        
         FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
        new Thread(futureTask).start();
        Integer integer = futureTask.get();
    }
    


    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("runnable01"  + Thread.currentThread());
            int i = 10 / 2;
            System.out.println("i=" + i);
        }
    }
    public static class Thread01 extends Thread{
        @Override
        public void run() {
            System.out.println("Thread01" + Thread.currentThread());
            int i = 10 / 2;
            System.out.println("i=" + i);
        }
    }
    public static class Callable01 implements Callable<Integer>{

        @Override
        public Integer call() throws Exception {
            System.out.println("Callable01"  + Thread.currentThread());
            int i = 10 / 2;
            System.out.println("i=" + i);
            return i;
        }
    }
}

```

```java
//第四种 线程池
public class ThreadTest {
    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(10);
        service.submit(new Runnable01());
        service.submit(new Thread01());
        service.submit(new Callable01());
        
               /**
         * 七大参数
         corePoolSize – the number of threads to keep in the pool, even if they are idle, unless allowCoreThreadTimeOut is set
         maximumPoolSize – the maximum number of threads to allow in the pool
         keepAliveTime – when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for new tasks before terminating.
         unit – the time unit for the keepAliveTime argument
         workQueue – the queue to use for holding tasks before they are executed. This queue will hold only the Runnable tasks submitted by the execute method.
         threadFactory – the factory to use when the executor creates a new thread
         handler – the handler to use when execution is blocked because the thread bounds and queue capacities are reached

         corePoolSize 核心线程数 用不用一直在那
         maximumPoolSize 线程池最大数    线程池大小 是  min(核心线程数 corePoolSize) - max(maximumPoolSize 最大线程数)
         keepAliveTime  当线程的数量超过核心线程数 这些空闲的线程会在指定时间内释放
         unit  指定释放时间单位
         workQueue 任务队列
         threadFactory 线程工厂 用于创建新的线程
         handler 任务拒绝策略处理器
         */
        /**
         * 1.1 创建核心线程数
         * 1.2 当核心线程数满了 进入队列
         * 1.3 队列满了 再开线程任务
         * 1.4 都满了有任务拒绝策略
         * 1.5 当任务都执行结束后 指定等待时长释放任务
         * 1.6
         *
         *  7个核心数 最大20个线程数 50个队列 100并发进来 7个立即执行 50个进入队列 开13个线程 剩下的30个根据策略进行执行
         *  四个策略
         *  CallerRunsPolicy 同步执行
         *  DiscardPolicy 丢弃任务 但是没有通知
         *  AbortPolicy 会抛出 RejectedExecutionException异常
         *  DiscardOldestPolicy 这种策略，会把队列中存在时间最久的那个任务给丢弃掉，以便给新来的任务腾位置；
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                5,
                200,
                2,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }    
  }
}
```

## 常见的四种线程池

```java
        /**
         * 创建一个线程池，该线程池根据需要创建新线程，但会在可用时重用以前构造的线程。
         * 这些池通常会提高执行许多短期异步任务的程序的性能。如果可用，对执行的调用将重用以前构造的线程。
         * 如果没有可用的现有线程，将创建一个新线程并将其添加到池中。 60 秒内未使用的线程将被终止并从缓存中删除。
         * 因此，保持空闲时间足够长的池不会消耗任何资源。
         * 请注意，可以使用 ThreadPoolExecutor 构造函数创建具有相似属性但不同细节（例如，超时参数）的池。
         */
        Executors.newCachedThreadPool();
```

```java
        /**
         * 创建一个线程池，该线程池重用固定数量的线程，这些线程在共享的无界队列中运行。
         * 在任何时候，最多 nThreads 个线程将是活动的处理任务。
         * 如果在所有线程都处于活动状态时提交了额外的任务，它们将在队列中等待，直到有线程可用。
         * 如果任何线程在关闭之前的执行过程中因故障而终止，则如果需要执行后续任务，将有一个新线程取代它。
         * 池中的线程将一直存在，直到它被显式关闭。
         */
        Executors.newFixedThreadPool(10);
```

```java
      /**
         * 创建一个线程池，可以安排命令在给定的延迟后运行，或定期执行。
         */
        Executors.newScheduledThreadPool(10);
```

```java
        /**
         *
         创建一个 Executor，它使用单个工作线程在无界队列中运行。
         （但是请注意，如果此单个线程在关闭之前的执行过程中因故障而终止，
         则如果需要执行后续任务，一个新线程将取代它。）
         保证任务按顺序执行，并且不会有多个任务处于活动状态在任何给定时间。与
         其他等效的 newFixedThreadPool(1) 不同，返回的执行程序保证不能重新配置以使用其他线程
         */
        Executors.newSingleThreadExecutor();
```

## 异步编排CompletableFuture

```java
package com.hzk.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @author kee
 * @version 1.0
 * @date 2022/11/17 16:29
 */
public class ThreadTest {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main...start");
        //CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        //    System.out.println("当前线程" + Thread.currentThread());
        //    int i = 10 / 2;
        //    System.out.println("i=" + i);
        //}, executor);
        //CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
        //    System.out.println("当前线程" + Thread.currentThread());
        //    int i = 10 / 0;
        //    System.out.println("i=" + i);
        //    return i;
        //}, executor)
        //        .whenComplete((result,exception)->{
        //            System.out.println("结果是：" + result + " 异常：" + exception);
        //        })
        //        .exceptionally(throwable -> {
        //          return 10;
        //        });
        //System.out.println(future1.get());
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("task1..." + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("task1结束" );
            return i;
        },executor);
        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("task2..." + Thread.currentThread().getId());
            System.out.println("task2结束" );
            return "Hello";
        },executor);
        /**
         * 这个函数不能接受两个任务的返回参数
         */
        //future01.runAfterBothAsync(future02,()->{
        //    System.out.println("任务3.." + Thread.currentThread().getId());
        //},executor);
        /**
         * 能接收两个任务参数但不能改变返回结果
         */
        //future01.thenAcceptBothAsync(future02,(f1,f2) ->{
        //    System.out.println(f1 + f2);
        //},executor);
        /**
         * 能接收任务也能返回新结果
         *
         */
        //CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
        //    return f1 + f2 + "->hello";
        //},executor);
        /**
         * 由于GET这个操作需要获得前两个线程返回结果并且计算才能得到新的结果，所以会变成同步操作
         */
        //System.out.println(future.get());

        System.out.println("main...end");
    }

    public static void testmain(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main... start");
        //ExecutorService service = Executors.newFixedThreadPool(10);
        //service.submit(new Runnable01());
        //service.submit(new Thread01());
        //service.submit(new Callable01());


        /**
         * 七大参数
         corePoolSize – the number of threads to keep in the pool, even if they are idle, unless allowCoreThreadTimeOut is set
         maximumPoolSize – the maximum number of threads to allow in the pool
         keepAliveTime – when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for new tasks before terminating.
         unit – the time unit for the keepAliveTime argument
         workQueue – the queue to use for holding tasks before they are executed. This queue will hold only the Runnable tasks submitted by the execute method.
         threadFactory – the factory to use when the executor creates a new thread
         handler – the handler to use when execution is blocked because the thread bounds and queue capacities are reached

         corePoolSize 核心线程数 用不用一直在那
         maximumPoolSize 线程池最大数    线程池大小 是  min(核心线程数 corePoolSize) - max(maximumPoolSize 最大线程数)
         keepAliveTime  当线程的数量超过核心线程数 这些空闲的线程会在指定时间内释放
         unit  指定释放时间单位
         workQueue 任务队列
         threadFactory 线程工厂 用于创建新的线程
         handler 任务拒绝策略处理器
         */
        /**
         * 1.1 创建核心线程数
         * 1.2 当核心线程数满了 进入队列
         * 1.3 队列满了 再开线程任务
         * 1.4 都满了有任务拒绝策略
         * 1.5 当任务都执行结束后 指定等待时长释放任务
         * 1.6
         *
         *  7个核心数 最大20个线程数 50个队列 100并发进来 7个立即执行 50个进入队列 开13个线程 剩下的30个根据策略进行执行
         *  CallerRunsPolicy 同步执行
         *  DiscardPolicy 丢弃任务 但是没有通知
         *  AbortPolicy 会抛出 RejectedExecutionException异常
         *  DiscardOldestPolicy 这种策略，会把队列中存在时间最久的那个任务给丢弃掉，以便给新来的任务腾位置；
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                5,
                200,
                2,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        /**
         * 创建一个线程池，该线程池根据需要创建新线程，但会在可用时重用以前构造的线程。
         * 这些池通常会提高执行许多短期异步任务的程序的性能。如果可用，对执行的调用将重用以前构造的线程。
         * 如果没有可用的现有线程，将创建一个新线程并将其添加到池中。 60 秒内未使用的线程将被终止并从缓存中删除。
         * 因此，保持空闲时间足够长的池不会消耗任何资源。
         * 请注意，可以使用 ThreadPoolExecutor 构造函数创建具有相似属性但不同细节（例如，超时参数）的池。
         */
        //Executors.newCachedThreadPool();
        /**
         * 创建一个线程池，该线程池重用固定数量的线程，这些线程在共享的无界队列中运行。
         * 在任何时候，最多 nThreads 个线程将是活动的处理任务。
         * 如果在所有线程都处于活动状态时提交了额外的任务，它们将在队列中等待，直到有线程可用。
         * 如果任何线程在关闭之前的执行过程中因故障而终止，则如果需要执行后续任务，将有一个新线程取代它。
         * 池中的线程将一直存在，直到它被显式关闭。
         */
        //Executors.newFixedThreadPool(10);
        /**
         * 创建一个线程池，可以安排命令在给定的延迟后运行，或定期执行。
         */
        //Executors.newScheduledThreadPool(10);
        /**
         *
         创建一个 Executor，它使用单个工作线程在无界队列中运行。
         （但是请注意，如果此单个线程在关闭之前的执行过程中因故障而终止，
         则如果需要执行后续任务，一个新线程将取代它。）
         保证任务按顺序执行，并且不会有多个任务处于活动状态在任何给定时间。与
         其他等效的 newFixedThreadPool(1) 不同，返回的执行程序保证不能重新配置以使用其他线程
         */
        //Executors.newSingleThreadExecutor();


        System.out.println("main... end");
    }


    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("runnable01"  + Thread.currentThread());
            int i = 10 / 2;
            System.out.println("i=" + i);
        }
    }
    public static class Thread01 extends Thread{
        @Override
        public void run() {
            System.out.println("Thread01" + Thread.currentThread());
            int i = 10 / 2;
            System.out.println("i=" + i);
        }
    }
    public static class Callable01 implements Callable<Integer>{

        @Override
        public Integer call() throws Exception {
            System.out.println("Callable01"  + Thread.currentThread());
            int i = 10 / 2;
            System.out.println("i=" + i);
            return i;
        }
    }


}
```

# 验证码

首先需要在阿里云上购买短信服务，再根据供应商提供的API接口实现发送短信

```java
package com.example.thirdpart.component;

import com.example.thirdpart.utils.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/7 17:27
 */

@ConfigurationProperties("spring.cloud.alicloud.sms")
@Component
@Data
public class SmsComponent {
    private String host;
    private String path;
    private String method;
    private String appcode;
    private String tplId;

    public void sendSmsCode(String code,String phoneNumber){

        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phoneNumber);
        querys.put("param", "code:" + code);
        querys.put("tpl_id", tplId);
        Map<String, String> bodys = new HashMap<String, String>();


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## 验证码接口防刷机制以及六十秒锁定

```java
@ResponseBody
@GetMapping("/sms/sendCode")
/**
 * 一个思路
 *  phone为 key,设置一个值为2 当作发送次数 设置这个key的过期时间
 *  这个号码没发一次减去1 当这个值为0时不能发送 10分钟内可以发送3此 如果已经为0在发送则 抛出频率过高异常
 */
public R sendCode(@RequestParam("phone") String phone) {
    //int i = (int) (Math.random() * 9000 + 1000);//随机生成一个四位整数
    ValueOperations<String, String> ops = redisTemplate.opsForValue();

    //接口频率
    String redisCode = ops.get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
    if (!StringUtils.isEmpty(redisCode)) {
        Long currentTime = Long.parseLong(redisCode.split("_")[1]);
        if (System.currentTimeMillis() - currentTime < 60000) {
            return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
        }
    }


    //进来先判断是否有key 有key再判断是否为0
    //接口防刷
    String count = ops.get(AuthServerConstant.SMS_COUNT_CACHE_PREFIX + phone);
    if (!StringUtils.isEmpty(count)) {
        if (count.equals("0")) {
            return R.error(BizCodeEnum.SMS_COUNT_EXCEPTION.getCode(), BizCodeEnum.SMS_COUNT_EXCEPTION.getMsg());
        } else {
            String tempCount = ops.get(AuthServerConstant.SMS_COUNT_CACHE_PREFIX + phone);
            int i = Integer.parseInt(tempCount) - 1;
            ops.set(AuthServerConstant.SMS_COUNT_CACHE_PREFIX + phone, i + "");
        }
    } else {
        ops.set(AuthServerConstant.SMS_COUNT_CACHE_PREFIX + phone, "2", 10, TimeUnit.MINUTES);
    }

    int code = (int) ((Math.random() * 9 + 1) * 100000);
    String codeNum = String.valueOf(code);
    String redisStorage = codeNum + "_" + System.currentTimeMillis();

    ops.set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, redisStorage, 10, TimeUnit.MINUTES);
    thirdPartFeignService.sendCode(codeNum, phone);


    return R.ok();
}
```

# 分布式Session-Spirng-session

引入依赖

```xml
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-data-redis</artifactId>
        </dependency>
```

修改配置

```properties
spring.session.store-type=redis
```

## 核心配置修改域名

```java
@Configuration
public class GulimallSessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainName("gulimall.com");
        cookieSerializer.setCookieName("GULISESSION");
        return cookieSerializer;
    }

    @Bean
    public RedisSerializer<Object> redisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }
}

```



# 第三方weibo登录

先到微博开放平台创建一个应用

![image-20221219153456624](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20221219153456624.png)

![image-20221219153557560](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20221219153557560.png)

```
<a href="https://api.weibo.com/oauth2/authorize?client_id=(APPKEY)&response_type=code&redirect_uri=http://auth.gulimall.com/oauth2.0/weibo/success">
```

## 登录

```java
package com.hzk.gulimall.auth.controller;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hzk.common.utils.R;
import com.hzk.gulimall.auth.feign.MemberFeignService;
import com.hzk.common.vo.MemberResponseVo;
import com.hzk.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/14 16:23
 */
@Slf4j
@Controller
public class OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "77292125");
        map.put("client_secret", "3cbb9862304760bf8c7a441aeceefff8");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        //String url = "https://api.weibo.com/oauth2/access_token";
        //HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map, new HashMap<>());
        //HttpResponse response = HttpUtils.post(url, null, map);

        String url = "https://api.weibo.com/oauth2/access_token?client_id=77292125&client_secret=3cbb9862304760bf8c7a441aeceefff8&grant_type=authorization_code&redirect_uri=http://auth.gulimall.com/oauth2.0/weibo/success&code=" + code;
        HttpPost post = new HttpPost(url);
        CloseableHttpResponse response = null;

        response = HttpClients.createDefault().execute(post);
        if (response.getStatusLine().getStatusCode() == 200) {
            String s = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSONObject.parseObject(s, SocialUser.class);
            R oauthLogin = memberFeignService.oauthLogin(socialUser);
            if (oauthLogin.getCode() == 0) {
                MemberResponseVo responseVo = oauthLogin.getData("data", new TypeReference<MemberResponseVo>() {
                });
                log.info("登陆成功，用户信息:{}", responseVo);
                session.setAttribute("loginUser",responseVo);
                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }
}
```

 R oauthLogin = memberFeignService.oauthLogin(socialUser);

```java
    @Override
    public MemberEntity login(SocialUser vo) {
        String uid = vo.getUid();
        MemberEntity member = baseMapper.selectOne(new QueryWrapper<MemberEntity>().lambda().eq(MemberEntity::getSocialUid, uid));
        if (member != null) {
            //说明已经注册
            MemberEntity updateMember = new MemberEntity();
            updateMember.setAccessToken(vo.getAccess_token());
            updateMember.setId(member.getId());
            updateMember.setExpiresIn(vo.getExpires_in() + "");
            baseMapper.updateById(updateMember);

            member.setAccessToken(vo.getAccess_token());
            member.setExpiresIn(vo.getExpires_in() + "");
            return member;
        } else {
            MemberEntity register = new MemberEntity();
            try {
                Map<String, String> map = new HashMap<>();
                map.put("access_token", vo.getAccess_token());
                map.put("uid", vo.getUid());
                HttpResponse response = null;
                response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), map);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String result = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");

                    register.setNickname(name);
                    register.setGender("m".equals(gender) ? 1 : 0);
                }

            } catch (Exception e) {
            }
            register.setSocialUid(vo.getUid());
            register.setAccessToken(vo.getAccess_token());
            register.setExpiresIn(vo.getExpires_in() + "");
            baseMapper.insert(register);
            return register;
        }
    }
}
```

# 盐值加密和解密

```java
BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
String encode = passwordEncoder.encode(vo.getPassword());
```



```java
//为true密码就是正确的
BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
boolean matches = passwordEncoder.matches(vo.getPassword(), encodePassword);
```

# RabbitMQ

异步处理，应用解耦，流量控制

![image-20230221164543134](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230221164543134.png)

![image-20230221172328631](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230221172328631.png)

![image-20230221172553725](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230221172553725.png)

![image-20230222153655504](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230222153655504.png)

## 安装

![image-20230222153853905](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230222153853905.png)

docker run -d --name rabbitmq -p 5671:5671 -p 5672:5672 -p 4369:4369 -p 25672:25672 -p 15671:15671 -p 15672:15672 rabbitmq:management

## springboot整合rabbitmq

```xMl
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
```

## exchange（交换机 )  queue(队列) binding(绑定)

```java
package com.hzk.gulimall.order;

import com.hzk.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void sendTextMessage() {
        OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
        for (int i = 0; i < 10; i++) {
            entity.setId(1L);
            entity.setCreateTime(new Date());
            entity.setName("哈哈哈" + i);
            rabbitTemplate.convertAndSend("hello-java-exchange", "hello-java", entity);
            log.info("信息发送成功:{}", entity);
        }

    }

    @Test
    void createExchange() {
        /**
         * DirectExchange FanoutExchange CustomExchange TopicExchange  HeadersExchange
         *
         */
        DirectExchange directExchange = new DirectExchange("hello-java-exchange");
        amqpAdmin.declareExchange(directExchange);
        log.info("交换机hello-java-exchange 创建成功");
    }

    @Test
    void createQueue() {
        /**
         *
         */
        Queue queue = new Queue("hello-java-queue");
        amqpAdmin.declareQueue(queue);
        log.info("队列-hello-java-queue 创建成功");
    }

    @Test
    void createBinding() {
        /**
         * String destination,
         * Binding.DestinationType destinationType,
         * String exchange,
         * String routingKey,
         * @Nullable Map<String, Object> arguments
         */
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello-java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("绑定-hello-java  创建成功");
    }
}

```

## publisher可靠抵达

![image-20230224163429610](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230224163429610.png)

![image-20230227155507197](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230227155507197.png)

```properties
#开启发送端消息抵达队列确认
spring.rabbitmq.publisher-returns=true
#开启发送端确认 会调用confirmCallback方法  correlated  simple none
spring.rabbitmq.publisher-confirm-type=correlated
#只要抵达队列，以异步发送优先回调 returnConfirm
spring.rabbitmq.template.mandatory=true
```

```java
    @PostConstruct
    public void initRabbitTemplate() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData  当前消息的唯一关联数据
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println(("correlationData:" + correlationData + "ack:" + ack + "cause:" + cause));

            }
        });
    }
```

## ack确认机制

```properties
#auto  client dups_ok 默认是自动确认，cline为手动确认
# client一旦消息侦听器实现调用了javax.jms.Message.acknowledge（），消息就会被确认。此模式使应用程序（而不是 JMS 提供程序）能够完全控制消息确认
#dups_ok  与自动确认类似，只是所述确认是惰性的。因此，消息可能会传递多次。此模式启用至少一次消息传递保证。
spring.jms.listener.acknowledge-mode=client
```

![image-20230227154003637](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230227154003637.png)

# Feign远程调用丢失请求头

当feign远程调用其他应用时会重新封装一个请求，这样会导致原先的登录信息丢失。

解决办法，在封装请求先添加一个拦截器，将请求头放入其中

```java
package com.hzk.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/2 14:09
 */
@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = requestAttributes.getRequest();
                if (request != null){
                    String cookie = request.getHeader("Cookie");
                    template.header("Cookie",cookie);
                }

            }
        };
    }
}

```

# 分布式事务

## CAP定理

![image-20230310154030979](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230310154030979.png)

一致性和可用性只能2选1 所以系统只能是CP或者AP

## RAFT算法

选举机制类似于redis,当节点中没有leader时，每个结点有自旋时间，当某个节点自旋结束，首先会给自己投票，然后给其他所有节点投票，如果有两个节点相同票数，则继续等待自旋时间结束开始下轮投票，最终选出一个leader。leader如何同步子节点数据？当客户端消息发送到leader他首先发送给子节点消息，当子节点相应数据，则leader节点commit数据，在同步给子节点消息，让子节点commit。leader发送消息的间隔根据心跳算法计算。

当网络中断，节点被分成两部分，若有一部分虽然当上了leader节点，但是不是大多数结点投票的结果，则客户端的消息发送过来依然时保存失败的。当大多数节点选举投票出来的leader消息才能同步成功。

当网络恢复了，如果有两个Leader结点则选举轮次多的当选leader结点然后继续同步信息。

## Seata

官方文档：https://seata.io/zh-cn/ 不适合应用再高并发的方法上，下单就不适合

seata版本为1.5.1

第一步引入依赖

```xml
<dependency>
  <groupId>com.alibaba.cloud</groupId>
  <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

第二步 在需要全局事务控制的项目的资源文件目录下创建registry.conf和file.conf文件

file.conf 需要注意修改的地方 vgroupMapping.gulimall-order = "default"  以及再yml或Properties文件中添加

两者要对应

```properties
seata.tx-service-group=gulimall-order
```

```
transport {
  # tcp udt unix-domain-socket
  type = "TCP"
  #NIO NATIVE
  server = "NIO"
  #enable heartbeat
  heartbeat = true
  #thread factory for netty
  thread-factory {
    boss-thread-prefix = "NettyBoss"
    worker-thread-prefix = "NettyServerNIOWorker"
    server-executor-thread-prefix = "NettyServerBizHandler"
    share-boss-worker = false
    client-selector-thread-prefix = "NettyClientSelector"
    client-selector-thread-size = 1
    client-worker-thread-prefix = "NettyClientWorkerThread"
    # netty boss thread size,will not be used for UDT
    boss-thread-size = 1
    #auto default pin or 8
    worker-thread-size = 8
  }
  shutdown {
    # when destroy server, wait seconds
    wait = 3
  }
  serialization = "seata"
  compressor = "none"
}
service {
  #vgroup->rgroup
  vgroupMapping.gulimall-order = "default"
  #only support single node
  default.grouplist = "127.0.0.1:8091"
  #degrade current not support
  enableDegrade = false
  #disable
  disable = false
  #unit ms,s,m,h,d represents milliseconds, seconds, minutes, hours, days, default permanent
  max.commit.retry.timeout = "-1"
  max.rollback.retry.timeout = "-1"
}

client {
  async.commit.buffer.limit = 10000
  lock {
    retry.internal = 10
    retry.times = 30
  }
  report.retry.count = 5
}

## transaction log store
store {
  ## store mode: file、db
  mode = "file"

  ## file store
  file {
    dir = "sessionStore"

    # branch session size , if exceeded first try compress lockkey, still exceeded throws exceptions
    max-branch-session-size = 16384
    # globe session size , if exceeded throws exceptions
    max-global-session-size = 512
    # file buffer size , if exceeded allocate new buffer
    file-write-buffer-cache-size = 16384
    # when recover batch read size
    session.reload.read_size = 100
    # async, sync
    flush-disk-mode = async
  }

  ## database store
  db {
    ## the implement of javax.sql.DataSource, such as DruidDataSource(druid)/BasicDataSource(dbcp) etc.
    datasource = "dbcp"
    ## mysql/oracle/h2/oceanbase etc.
    db-type = "mysql"
    url = "jdbc:mysql://127.0.0.1:3306/seata"
    user = "mysql"
    password = "mysql"
    min-conn = 1
    max-conn = 3
    global.table = "global_table"
    branch.table = "branch_table"
    lock-table = "lock_table"
    query-limit = 100
  }
}
lock {
  ## the lock store mode: local、remote
  mode = "remote"

  local {
    ## store locks in user's database
  }

  remote {
    ## store locks in the seata's server
  }
}
recovery {
  committing-retry-delay = 30
  asyn-committing-retry-delay = 30
  rollbacking-retry-delay = 30
  timeout-retry-delay = 30
}

transaction {
  undo.data.validation = true
  undo.log.serialization = "jackson"
}

## metrics settings
metrics {
  enabled = false
  registry-type = "compact"
  # multi exporters use comma divided
  exporter-list = "prometheus"
  exporter-prometheus-port = 9898
}
```

第四步 在需要控制事务的总方法上标注  @GlobalTransactional注解 即可完成远程事务的回滚。

## RabbitM延时队列

![image-20230317093325977](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230317093325977.png)

![image-20230317093502149](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230317093502149.png)

## 保证消息可靠性

![image-20230322161135051](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230322161135051.png)

![image-20230322161325106](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230322161325106.png)

![image-20230322162534211](C:\Users\hzk\AppData\Roaming\Typora\typora-user-images\image-20230322162534211.png)
