package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.IdWorker;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class payController {
    @Reference(timeout = 5000)
    private WeixinPayService weixinPayService;
    @Reference(timeout = 5000)
    private OrderService orderService;

    /**
     * 生成二维码
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        IdWorker idWorker = new IdWorker();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog tbPayLog = orderService.searchPayLogFromRedis(userId);
        if (tbPayLog != null) {
            //测试一分钱
            return weixinPayService.createNative(tbPayLog.getOutTradeNo() + "", tbPayLog.getTotalFee() + "");
        } else {
            return new HashMap();
        }
    }

    /**
     * 查询支付状态
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        Integer x = 0;
        while (true) {
            Map<String, String> resunltMap = weixinPayService.queryPayStatus(out_trade_no);
            if (resunltMap == null) {
                result = new Result(false, "支付异常");
                break;
            }
            if ("success".equalsIgnoreCase(resunltMap.get("trade_state"))) {
                result = new Result(true, "支付成功");

                orderService.updateOrderStatus(out_trade_no, resunltMap.get("transaction_id"));//修改订单状态
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x >= 100) {
                result = new Result(false, "支付超时");
                break;
            }
        }
        return result;
    }
}
