package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pinyougou.IdWorker;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.Cart;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TbPayLogMapper tbPayLogMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbOrder> result = new PageResult<TbOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbOrder> list = orderMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(TbOrder order) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

        List<String> orderList = new ArrayList();//订单ID列表
        double total_money = 0;//总金额
        for (Cart cart : cartList) {
            long orderId = idWorker.nextId();
            TbOrder tbOrder = new TbOrder();//新创建订单对象
            tbOrder.setOrderId(orderId);//订单ID
            tbOrder.setUserId(order.getUserId());//用户名
            tbOrder.setPaymentType(order.getPaymentType());//支付类型
            tbOrder.setStatus("1");//状态：未付款
            tbOrder.setCreateTime(new Date());//订单创建日期
            tbOrder.setUpdateTime(tbOrder.getCreateTime());//订单更新日期
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());//地址
            tbOrder.setReceiverMobile(order.getReceiverMobile());//手机号
            tbOrder.setReceiver(order.getReceiver());//收货人
            tbOrder.setSourceType(order.getSourceType());//订单来源
            tbOrder.setSellerId(cart.getSellerId());//商家ID
            double money = 0;
            for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                tbOrderItem.setId(idWorker.nextId());
                tbOrderItem.setOrderId(orderId);//订单id
                tbOrderItem.setSellerId(cart.getSellerId());
                money += tbOrderItem.getPrice().doubleValue() + money;
                tbOrderItemMapper.insertSelective(tbOrderItem);
            }
            tbOrder.setPayment(new BigDecimal(money));
            orderMapper.insertSelective(tbOrder);
            orderList.add(orderId + "");
            total_money += money;
        }
        if ("1".equals(order.getPaymentType())) {
            TbPayLog tbPayLog = new TbPayLog();
            String outTradeNo = idWorker.nextId() + "";
            tbPayLog.setCreateTime(new Date());
            tbPayLog.setOutTradeNo(outTradeNo);
            String ids = orderList.toString().replace("[", "").replace("]", "").replace(" ", "");
            tbPayLog.setOrderList(ids);//设置订单列表,以 , 隔开
            tbPayLog.setPayType("1");//设置支付类型
            tbPayLog.setTotalFee((long) total_money);//设置总金额
            tbPayLog.setTradeState("0");//设置支付状态
            tbPayLog.setUserId(order.getUserId());//设置用户id
            tbPayLogMapper.insert(tbPayLog);//插入数据库
            redisTemplate.boundHashOps("paylog").put(tbPayLog.getUserId(), tbPayLog);//放入缓存
        }
        //3.清除redis购物车的缓存数据
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }


    /**
     * 修改
     */
    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbOrder findOne(Long id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        orderMapper.deleteByExample(example);
    }


    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        PageResult<TbOrder> result = new PageResult<TbOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if (order != null) {
            //如果字段不为空
            if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                criteria.andLike("paymentType", "%" + order.getPaymentType() + "%");
            }
            //如果字段不为空
            if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                criteria.andLike("postFee", "%" + order.getPostFee() + "%");
            }
            //如果字段不为空
            if (order.getStatus() != null && order.getStatus().length() > 0) {
                criteria.andLike("status", "%" + order.getStatus() + "%");
            }
            //如果字段不为空
            if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                criteria.andLike("shippingName", "%" + order.getShippingName() + "%");
            }
            //如果字段不为空
            if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                criteria.andLike("shippingCode", "%" + order.getShippingCode() + "%");
            }
            //如果字段不为空
            if (order.getUserId() != null && order.getUserId().length() > 0) {
                criteria.andLike("userId", "%" + order.getUserId() + "%");
            }
            //如果字段不为空
            if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                criteria.andLike("buyerMessage", "%" + order.getBuyerMessage() + "%");
            }
            //如果字段不为空
            if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                criteria.andLike("buyerNick", "%" + order.getBuyerNick() + "%");
            }
            //如果字段不为空
            if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                criteria.andLike("buyerRate", "%" + order.getBuyerRate() + "%");
            }
            //如果字段不为空
            if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                criteria.andLike("receiverAreaName", "%" + order.getReceiverAreaName() + "%");
            }
            //如果字段不为空
            if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                criteria.andLike("receiverMobile", "%" + order.getReceiverMobile() + "%");
            }
            //如果字段不为空
            if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                criteria.andLike("receiverZipCode", "%" + order.getReceiverZipCode() + "%");
            }
            //如果字段不为空
            if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                criteria.andLike("receiver", "%" + order.getReceiver() + "%");
            }
            //如果字段不为空
            if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                criteria.andLike("invoiceType", "%" + order.getInvoiceType() + "%");
            }
            //如果字段不为空
            if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                criteria.andLike("sourceType", "%" + order.getSourceType() + "%");
            }
            //如果字段不为空
            if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                criteria.andLike("sellerId", "%" + order.getSellerId() + "%");
            }

        }

        //查询数据
        List<TbOrder> list = orderMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    /**
     * 支付日志查询
     *
     * @param userId
     * @return
     */
    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("paylog").get(userId);
    }

    /**
     * 修改订单状态
     *
     * @param out_trade_no   支付订单号
     * @param transaction_id 微信返回的交易流水号
     */
    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {

        //1.修改日志状态
        TbPayLog tbPayLog = tbPayLogMapper.selectByPrimaryKey(out_trade_no);
        tbPayLog.setPayTime(new Date());
        tbPayLog.setTradeState("1");
        tbPayLog.setTransactionId(transaction_id);//设置交易订单号
        tbPayLogMapper.updateByPrimaryKeySelective(tbPayLog);

        //2.修改订单状态
        String orderList = tbPayLog.getOrderList();
        String[] orderIds = orderList.split(",");// 根据 , 分割出订单列表
        for (String orderId : orderIds) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(new Long(orderId));
            if (tbOrder != null) {
                tbOrder.setStatus("2");//设置2.已付款状态
                orderMapper.updateByPrimaryKeySelective(tbOrder);
            }
        }
        redisTemplate.boundHashOps("paylog").delete(tbPayLog.getUserId());//删除缓存
    }

}
