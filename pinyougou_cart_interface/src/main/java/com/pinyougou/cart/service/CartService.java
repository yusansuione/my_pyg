package com.pinyougou.cart.service;

import com.pinyougou.pojo.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 添加购物车
     *
     * @param cartList 购物车列表
     * @param itemId   用户ID
     * @param num      商品的数量
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 查询购物车
     *
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 存储购物车列表
     *
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username, List<Cart> cartList);


    /**
     * 合并购物车
     *
     * @param cartList1
     * @param cartList2
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);


}
