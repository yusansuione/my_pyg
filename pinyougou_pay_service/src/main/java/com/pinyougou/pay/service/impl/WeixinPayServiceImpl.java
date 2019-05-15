package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.HttpClient;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付实现
 *
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.pay.service.impl
 * @date 2018-6-3
 */
@Service(timeout = 5000)
public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notifyurl;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        Map map = new HashMap();

        try {
            Map param = new HashMap();
            param.put("appid", appid);//公众号ID
            param.put("mch_id", partner);//商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());//获取随机字符串
            param.put("body", "品优购");//商品描述
            param.put("out_trade_no", out_trade_no); //订单号
            param.put("total_fee", total_fee);  //订单总金额，单位为分
            param.put("spbill_create_ip", "127.0.0.1");  //终端IP，只要附合ip地址规范，可以随意写
            param.put("notify_url", notifyurl);  //回调地址
            param.put("trade_type", "NATIVE");  //交易类型，NATIVE 扫码支付
            //2、生成xml，通过httpClient发送请求得到数据
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求参数:" + signedXml);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();
            //3、解析结果
            String xmlResult = httpClient.getContent();
            System.out.println("微信反回结果:" + xmlResult);
            Map<String, String> resultXml = WXPayUtil.xmlToMap(xmlResult);
            map.put("code_url", resultXml.get("code_url"));//支付地址
            map.put("total_fee", total_fee);//总金额
            map.put("out_trade", out_trade_no);//订单号
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 订单查询
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            //1、包装微信接口需要的参数
            Map param = new HashMap();
            param.put("appid", appid);  //公众号ID
            param.put("mch_id", partner);  //商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
            param.put("out_trade_no", out_trade_no); //订单号

            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求参数：" + xmlParam);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //3.解析结果
            String xmlResult = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map closePay(String out_trade_no) {
        try {
            //1、包装微信接口需要的参数
            Map param = new HashMap();
            param.put("appid", appid);  //公众号ID
            param.put("mch_id", partner);  //商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
            param.put("out_trade_no", out_trade_no); //订单号
            //2、生成xml，通过httpClient发送请求得到数据
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求参数:" + xmlParam);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //3、解析结果
            String xmlResult = httpClient.getContent();
            System.out.println("微信返回结果：" + xmlResult);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }
}
