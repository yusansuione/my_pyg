package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制层
 *
 * @author Steven
 */
@RestController
@RequestMapping("pay")
public class PayController {
    @Reference(timeout = 5000)
    private WeixinPayService weixinPayService;
    @Reference(timeout = 5000)
    private SeckillOrderService seckillOrderService;


    /**
     * 生成二维码
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        //获取当前用户
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis查询用户抢购订单
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        //找到订单
        if (seckillOrder != null) {
            //金额（分）
            String fen = (long) (seckillOrder.getMoney().doubleValue() * 100) + "";
            return weixinPayService.createNative(seckillOrder.getId() + "", fen);
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
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        while (true) {
            Map<String, String> resunltMap = weixinPayService.queryPayStatus(out_trade_no);
            if (resunltMap == null) {
                result = new Result(false, "支付异常");
                break;
            }
            if ("success".equalsIgnoreCase(resunltMap.get("trade_state"))) {
                result = new Result(true, "支付成功");

                /*            orderService.updateOrderStatus(out_trade_no, resunltMap.get("transaction_id"));//修改订单状态*/
                seckillOrderService.saveOrderFromRedisToDb(userId, new Long(out_trade_no), resunltMap.get("transaction_id"));//修改秒杀订单状态
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
                Map<String, String> map = weixinPayService.closePay(out_trade_no);
                if (!"SUCCESS".equals(map.get("result_code"))) {
                    if ("ORDERPAID".equals(map.get("rr_code"))) {
                        result = new Result(true, "支付成功");
                        seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
                    }
                }
                if (result.isSuccess() == false) {
                    System.out.println("超时，取消订单");
                    //2.调用删除
                    seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
                }
                seckillOrderService.deleteOrderFromRedis(userId, new Long(out_trade_no));

                break;
            }
        }
        return result;
    }
}
