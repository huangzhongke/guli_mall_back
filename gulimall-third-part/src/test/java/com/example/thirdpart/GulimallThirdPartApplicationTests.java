package com.example.thirdpart;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
class GulimallThirdPartApplicationTests {

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
}
