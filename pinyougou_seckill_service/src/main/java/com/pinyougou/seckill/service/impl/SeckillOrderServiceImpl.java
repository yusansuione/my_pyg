package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillGoodsService;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbSeckillOrder> result = new PageResult<TbSeckillOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbSeckillOrder> list = seckillOrderMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insertSelective(seckillOrder);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKeySelective(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        seckillOrderMapper.deleteByExample(example);
    }


    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageResult<TbSeckillOrder> result = new PageResult<TbSeckillOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            //如果字段不为空
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andLike("userId", "%" + seckillOrder.getUserId() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andLike("sellerId", "%" + seckillOrder.getSellerId() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andLike("status", "%" + seckillOrder.getStatus() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andLike("receiverAddress", "%" + seckillOrder.getReceiverAddress() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andLike("receiverMobile", "%" + seckillOrder.getReceiverMobile() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andLike("receiver", "%" + seckillOrder.getReceiver() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andLike("transactionId", "%" + seckillOrder.getTransactionId() + "%");
            }

        }

        //查询数据
        List<TbSeckillOrder> list = seckillOrderMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    /**
     * 提交订单
     *
     * @param seckillId
     * @param userId
     */
    @Override


    public void submitOrder(Long seckillId, String userId) {
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
        if (seckillGoods == null) {
            throw new RuntimeException("该商品不存在");
        }
        if (seckillGoods.getStockCount() <= 0) {
            throw new RuntimeException("该商品已被抢购一空");
        }
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);

        if (seckillGoods.getStockCount() == 0) {
            //将数据库跟新到数据库
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
            //删除缓存
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
        }
        //再支付之前,保存订单到redis
        //在支付之前，保存订单到redis
        long orderId = idWorker.nextId();
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(orderId);
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
        seckillOrder.setSeckillId(seckillId);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setUserId(userId);//设置用户ID
        seckillOrder.setStatus("0");//状态
        //保存订单到redis
        redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);

    }

    /**
     * 根据用户名查询秒杀订单
     *
     * @param userId
     * @return
     */
    @Override
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        if (orderId.longValue() != seckillOrder.getId().longValue()) {
            throw new RuntimeException("订单号不匹配!");
        }
        seckillOrder.setTransactionId(transactionId);
        seckillOrder.setStatus("1");//设置状态为1 代表支付成功!
        seckillOrder.setPayTime(new Date());
        seckillOrderMapper.insertSelective(seckillOrder);//保存订单
        redisTemplate.boundHashOps("seckillOrder").delete(userId);//删除redis缓存的数据
    }

    /**
     * 从缓存中删除订单
     *
     * @param userId
     * @param orderId
     */
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder != null && orderId.longValue() == seckillOrder.getId().longValue()) {
            redisTemplate.boundHashOps("seckillOrder").delete(userId);

            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
            seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getSellerId(), seckillGoods);
        }
    }

}
