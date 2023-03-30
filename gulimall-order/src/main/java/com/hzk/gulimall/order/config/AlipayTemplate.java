package com.hzk.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.hzk.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000122669322";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key="MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCscQfReAbdLmJshICoWB6xB30nh9UlsGrVH5GHOgCEOMrU64b6AoGL+z00ndVqj3c0mWQ+/8aH5CpMggsWEKXjps1wNgT96oK0XHdTk/YVmatjzoKVdHrKmyQZPIcHKVHpgsRYGRUaugX46qkhaADUR373dE7zPvc7GE/lgxOz3UIy8Rrlz1WtvxK0mA9wN7y8VC5mFIlm36GR3BEqsJhYhQB+JvAWXzgJKmsiJLwHcgU7WHUE9d2OvGmb2jruFFXeoYcV8rkYrW8rNTcFJeYxRS+C8QlFf69zyHZk47KdWBEFXxpZ1afPLg85Ew0MPyUOEOY0lecjWBejHNb2UQyNAgMBAAECggEAf7lgzxgWDXGz2Sjzmr1aTkl7u/HyQG5BUwaE5iJiC419PuQNH9UHW0WF+hkF4F3PL5iGFduhkzm+0thnmbpPBSWheQGqBODUWv86Kx+m4CzoM47cThLueG8c98xLP1StpUYEZDR/20H59nrpNyMvqgwi0OrHJ4h12sGRPDAwQcHgr5IijyogL+r1UYxCAuWX+2QG4lhUa0Cvf5VpOSb1JbS2eeByH+nIJfzVOxdfXjnaqQJgqOQLVbGdTMF/ovuLo2IaU01yyCuGTkvNT5HsM/OFxWKWncIcto5Gc6MZccTFluXGHdwdzqvu3f0GB7vPqtBb659sXyvU7mfua1EfwQKBgQDv4/hWOO4yrTQxtXG+GveOCRLeBpAkLkfpfVh6x2Z4/x5LY+XuDrsKujo3xwwUQKhxKDj5df0Os4N1HoOHlRGy3uqXlXuHCOZE39fok/3WIZWrULf5HdPizzSWC/NPyi8qBVbOdUxcgUHYr/t1jAiYywtdZnSom8kMUl9mlRrMSQKBgQC4BYZTk9NKi3Kqa7/p6HRjZALe9AKNAjmDyKwV+pKE9gMge/Y0XxosG23pH1oJjfjGK8x/Aurnt+THu/yszkA1y4bO/J9JUi8RGOIc50Wudx3TBh8jHh8n2BubkTO8sjKOh2SHkqIakgpOw4FSDTluOBK3CQfraVwphbn26ElWJQKBgHaqe7cTNGlnGpGYYuqaZmkhfe7MqlLrffE3ayRZLQyfjrnWcfAf41B8LL5YvVXrYiePsRdd0U+LZ3TxIjlYeUfVU5CmhlOXCGl7nU9UndVmCaaUKF2X7q+ctjOfN735kvkjaPx6M8ryucLZ0vcjzUDf+AA85WpencZ1nPGewoHZAoGBALPvowMCHq5bYEnHhiZfx76PwkbjbcMXk8mKZMow8yfTrgcLbxiSjXLndzdmEpIZIlIT7aeb4LmnFKtA47bNDfgMBMtMNSxqkd8z55S/h2Eq1BzNdZDPGUPKimQ5PM+bpte7B8+Qz7UPa8jMXh0vFSLy7UnQKNBFe3qWadpGE7+JAoGBAOjnEGf4brqs+Eal9CVz56/2d2UwGL/JdV3kIKwxYKvxy0nXRiGVfML/yM2pA6aZSwBUCDoJRSfz2EmyxZfyUxTuTjS5F5clGZBn20PHA5fXw9IcedzunRmEMcqI75vg+kKkfASl6/hMBI+CunHnYTVtCvFUIMlVxROvJLHfuyl1";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArp0TJJxhfNByNNVjvl0FUnuHe/+mYuLrMwcnU7iIB3d94ZThtAdgpawjHkfxF6eL6gLShpSpeGE5PCgcwpSW/OaOUbRw2k/Sd/1k0PYjh6GFEo2yUSz1jdMf1n2V6SoRVAzys9k3a8BZOzpw65P3DD/jYj0jx303JLdJvKyH9vEYJiM0+hXwpCgVVmqgxV1D0SmVpgtdhJMX6B5u8GDLbG5LfDrHK/r1tBlBbkK3xqaadY/DMktR7o/yf5QAhEm4SFmyIdU8N1JwuZyXK5lcUyzp1voW/lg8lsRomFZ1TmdWlHjenDLgkEnqgV9fQZx1Qx9bF34Irfx3QIXZaBh9/QIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url=" https://7e1476e263.zicp.fun/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";
    private String timeout="30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);
        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
