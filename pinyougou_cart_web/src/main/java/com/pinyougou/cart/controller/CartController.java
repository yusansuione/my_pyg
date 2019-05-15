package com.pinyougou.cart.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.CookieUtil;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojo.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 查询cookies中的购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        /*String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(username)) {
            System.out.println("从购物车里面取");
            String carts = CookieUtil.getCookieValue(request, "cartList", true);
            //判断cookie是否为空
            if (StringUtils.isBlank(carts)) {
                return new ArrayList<>();
            } else {

                List<Cart> cartsList = JSON.parseArray(carts, Cart.class);
                return cartsList;
            }
        } else {
            return cartService.findCartListFromRedis(username);
        }*/
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Cart> carts = new ArrayList<>();
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", true);
        //如果购物车cookies为空
        if (!StringUtils.isBlank(cartListStr)) {
            //把json串转成list
            carts = JSON.parseArray(cartListStr, Cart.class);
        }
        //如果用户未登录
        if ("anonymousUser".equals(username)) {
            System.out.println("从cookies中读取购物车");
            return carts;
        } else {
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
            //如果cookies中没有购物车数据
            if (carts.size() == 0) {
                return cartListFromRedis;
            } else {
                //合并购物车
                List<Cart> mergeCartList = cartService.mergeCartList(carts, cartListFromRedis);
                //更新redis中的购物车数据
                cartService.saveCartListToRedis(username, mergeCartList);
                //删除cookies购物车
                CookieUtil.deleteCookie(request, response, "cartList");
                //返回合并后的购物车列表
                return mergeCartList;
            }
        }


    }

    /**
     * 保存购物车列表在cookies中
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/saveCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {

        //设置可以访问的域，值设置为*时，允许所有域
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8099");
        //如果需要操作cookies，必须加上此配置，标识服务端可以写cookies，
        // 并且Access-Control-Allow-Origin不能设置为*，因为cookies操作需要域名
        response.setHeader("Access-Control-Allow-Credentials", "true");


        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Cart> cartList = findCartList();
            //注意，这里要重新接收一下，这里是调用接口查询数据
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if ("anonymousUser".equals(username)) {
                //存储一天
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, true);
            } else {
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "存储成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "存储失败");
    }

}
