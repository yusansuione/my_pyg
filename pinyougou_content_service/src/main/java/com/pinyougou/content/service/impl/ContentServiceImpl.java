package com.pinyougou.content.service.impl;

import java.util.Arrays;
import java.util.List;

import com.pinyougou.content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbContent> result = new PageResult<TbContent>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbContent> list = contentMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbContent> info = new PageInfo<TbContent>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        content.setStatus("1");
        contentMapper.insertSelective(content);
        //删除缓存
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
    }


    /**
     * 修改
     */
    @Override
    public void update(TbContent content) {
        TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        contentMapper.updateByPrimaryKeySelective(content);
        //判断是否更改了广告类型
        if (tbContent.getCategoryId().longValue() != content.getCategoryId().longValue()) {
            //删除修改前的缓存
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //在清空数据前,先将缓存清除
        List<TbContent> tbContentList = contentMapper.selectByExample(example);
        for (TbContent tbContent : tbContentList) {
            redisTemplate.boundHashOps("content").delete(tbContent.getCategoryId());
        }
        //跟据查询条件删除数据
        contentMapper.deleteByExample(example);
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageResult<TbContent> result = new PageResult<TbContent>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();

        if (content != null) {
            //如果字段不为空
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andLike("title", "%" + content.getTitle() + "%");
            }
            //如果字段不为空
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andLike("url", "%" + content.getUrl() + "%");
            }
            //如果字段不为空
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andLike("pic", "%" + content.getPic() + "%");
            }
            //如果字段不为空
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andLike("status", "%" + content.getStatus() + "%");
            }

        }

        //查询数据
        List<TbContent> list = contentMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbContent> info = new PageInfo<TbContent>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    /**
     * 根据categoryId查询
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
        Example example = new Example(TbContent.class);
        //从缓存里面读取
        List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
        if (contentList == null) {
            //条件
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("categoryId", categoryId);
            criteria.andEqualTo("status", "1");
            //实现排序
            example.setOrderByClause("sortOrder asc");
            contentList = contentMapper.selectByExample(example);

            //把数据放入缓存
            redisTemplate.boundHashOps("content").put(categoryId, contentList);

        } else {
            //
            System.out.println("从缓存取得");
        }
        return contentList;
    }

}
