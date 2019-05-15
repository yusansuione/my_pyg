package com.pinyougou.sellergoods.impl;

import java.util.Arrays;
import java.util.List;

import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.Specification;
import com.pinyougou.pojo.TbSpecificationOption;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.sellergoods.service.SpecificationService;
import entity.PageResult;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;
    @Autowired
    private TbSpecificationOptionMapper tbSpecificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbSpecification> result = new PageResult<TbSpecification>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbSpecification> list = specificationMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSpecification> info = new PageInfo<TbSpecification>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(Specification specification) {
        //规格插入
        specificationMapper.insertSelective(specification.getSpecification());
        //规格列表

        List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
        for (TbSpecificationOption tbSpecificationOption : specificationOptionList) {
            tbSpecificationOption.setSpecId(specification.getSpecification().getId());
            tbSpecificationOptionMapper.insertSelective(tbSpecificationOption);
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Specification specification) {
        specificationMapper.updateByPrimaryKey(specification.getSpecification());
        TbSpecificationOption where = new TbSpecificationOption();
        where.setSpecId(specification.getSpecification().getId());
        tbSpecificationOptionMapper.delete(where);

        //规格列表

        List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
        for (TbSpecificationOption tbSpecificationOption : specificationOptionList) {
            tbSpecificationOption.setSpecId(specification.getSpecification().getId());
            tbSpecificationOptionMapper.insertSelective(tbSpecificationOption);
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Specification findOne(Long id) {
        Specification specification = new Specification();
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
        specification.setSpecification(tbSpecification);
        TbSpecificationOption where = new TbSpecificationOption();
        where.setSpecId(id);
        List<TbSpecificationOption> select = tbSpecificationOptionMapper.select(where);
        specification.setSpecificationOptionList(select);
        return specification;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        specificationMapper.deleteByExample(example);

        for (Long id : ids) {
            TbSpecificationOption where = new TbSpecificationOption();
            where.setSpecId(id);
            tbSpecificationOptionMapper.delete(where);
        }
    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageResult<TbSpecification> result = new PageResult<TbSpecification>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();

        if (specification != null) {
            //如果字段不为空
            if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
                criteria.andLike("specName", "%" + specification.getSpecName() + "%");
            }

        }

        //查询数据
        List<TbSpecification> list = specificationMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSpecification> info = new PageInfo<TbSpecification>(list);
        result.setTotal(info.getTotal());

        return result;
    }

}
