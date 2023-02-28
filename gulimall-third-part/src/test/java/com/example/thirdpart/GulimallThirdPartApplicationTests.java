package com.example.thirdpart;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectRequest;
import com.example.thirdpart.component.SmsComponent;
import com.example.thirdpart.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@SpringBootTest
class GulimallThirdPartApplicationTests {

    @Autowired
    SmsComponent smsComponent;
    @Test
    void contextLoads() {
    }

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


        // 本地文件路径。
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

    @Test
    void sendEmail(){
        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "fc9a11c89fee4b3cb12b217e6c461ed0";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", "15988767510");
        querys.put("param", "code:1234");
        querys.put("tpl_id", "TP1711063");
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

    @Test
    void t2(){
        String code = UUID.randomUUID().toString().substring(0,4);
        System.out.println(code);
    }
}
