package com.pinyougou.sellergoods.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.ItemCatService;
import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbItemCat> findAll() {
        return itemCatMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbItemCat> result = new PageResult<TbItemCat>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbItemCat> list = itemCatMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbItemCat> info = new PageInfo<TbItemCat>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(TbItemCat itemCat) {
        itemCatMapper.insertSelective(itemCat);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbItemCat itemCat) {
        itemCatMapper.updateByPrimaryKeySelective(itemCat);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbItemCat findOne(Long id) {
        return itemCatMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        List longs = new ArrayList();
        for (Long id : ids) {
            longs.add(id);
            selectAllid(longs, id);
        }
        //构建查询条件
        Example example = new Example(TbItemCat.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        itemCatMapper.deleteByExample(example);
    }

    public void selectAllid(List longs, Long id) {
        List<TbItemCat> tbItemCats = this.findByparentId(id);
        if (tbItemCats != null && tbItemCats.size() > 0) {
            for (TbItemCat tbItemCat : tbItemCats) {
                longs.add(tbItemCat.getId());
                selectAllid(longs, tbItemCat.getId());
            }
        }
    }

    @Override
    public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
        PageResult<TbItemCat> result = new PageResult<TbItemCat>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbItemCat.class);
        Example.Criteria criteria = example.createCriteria();

        if (itemCat != null) {
            //如果字段不为空
            if (itemCat.getName() != null && itemCat.getName().length() > 0) {
                criteria.andLike("name", "%" + itemCat.getName() + "%");
            }

        }

        //查询数据
        List<TbItemCat> list = itemCatMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbItemCat> info = new PageInfo<TbItemCat>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    /**
     * 跟据父级parentId查询商品列表
     *
     * @param parentId
     * @return
     */
    @Override
    public List<TbItemCat> findByparentId(Long parentId) {

        TbItemCat where = new TbItemCat();
        where.setParentId(parentId);

        List<TbItemCat> tbItemCatList = this.findAll();
        //将商品分类数据放入缓存（Hash）。以分类名称作为key ,以模板ID作为值
        //在这里写的原因是商品分类增删改都会经过这个方法
        for (TbItemCat tbItemCat : tbItemCatList) {
            redisTemplate.boundHashOps("itemCat").put(tbItemCat.getName(), tbItemCat.getTypeId());
        }
        return itemCatMapper.select(where);
    }

}
