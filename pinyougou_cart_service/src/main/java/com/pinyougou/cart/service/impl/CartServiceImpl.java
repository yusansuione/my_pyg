package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.Cart;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务
 */
@Service(timeout = 10000)
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @param cartList 购物车列表
     * @param itemId   用户ID
     * @param num      商品的数量
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //根据商品的sku id查询sku信息
        TbItem item = tbItemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("没有对应的商品!");
        }
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("该商品属于无效商品!");

        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = this.searchCartBySellerId(cartList, sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {
            //4.1 创建新的购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            //新建购物车商品对象
            TbOrderItem tbOrderItem = createOrderItem(item, num);
            List<TbOrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(tbOrderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        } else {//5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem tbOrderItem = this.searchOrderItemByItemId(cart.getOrderItemList(), sellerId);
            //5.1. 如果没有，新增购物车明细
            if (tbOrderItem == null) {
                tbOrderItem = new TbOrderItem();
                TbOrderItem orderItem = this.createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {//5.2. 如果有，在原购物车明细上添加数量，更改金额
                tbOrderItem.setNum(tbOrderItem.getNum() + num);
                tbOrderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * tbOrderItem.getNum()));
                //如果数量操作后小于等于0，则移除
                if (tbOrderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(tbOrderItem);//移除购物车明细
                }
                //如果移除后cart的明细数量为0，则将cart移除
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }

        return cartList;
    }

    /**
     * 从数据里面取数据
     *
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println(" 从redis缓存里面取数据");
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            return new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 向缓存里面存数据
     *
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向缓存里面存数据");
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    /**
     * 合并购物车
     *
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                cartList1 = this.addGoodsToCartList(cartList1, tbOrderItem.getItemId(), tbOrderItem.getNum());
            }
        }

        return cartList1;
    }

    /**
     * 根据商品ID查询是否存在商家购物车里面
     *
     * @param orderItemList
     * @param sellerId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, String sellerId) {
        for (TbOrderItem tbOrderItem : orderItemList) {
            if (sellerId.equals(tbOrderItem.getSellerId())) {
                return tbOrderItem;
            }
        }
        return null;
    }

    /**
     * 创建购物车商品列表
     *
     * @param item 商品详情信息
     * @param num  商品数量
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num < 0) {
            throw new RuntimeException("商品数量错误");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }

    /**
     * 根据商家ID判断购物车列表中是否存在该商家的购物车
     *
     * @param cartList 购物车列表
     * @param sellerId 商品ID
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }
}
