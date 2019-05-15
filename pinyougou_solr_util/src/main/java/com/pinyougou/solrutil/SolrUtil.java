package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import entity.SolrItem;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 导入商品数据
     */
    public void importItemData() {
        TbItem where = new TbItem();
        where.setStatus("1");
        List<TbItem> tbItemList = tbItemMapper.select(where);
        List<SolrItem> solrItems = new ArrayList<>();
        SolrItem solrItem = null;
        System.out.println("--------商品列表开始-----------");
        for (TbItem tbItem : tbItemList) {
            solrItem = new SolrItem();
            BeanUtils.copyProperties(tbItem, solrItem);
            solrItems.add(solrItem);
            Map map = (Map) JSON.parse(tbItem.getSpec());
            solrItem.setSpecMap(map);
        }
        System.out.println("--------商品列表结束-----------");
        solrTemplate.saveBeans(solrItems);
        solrTemplate.commit();
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");

        SolrUtil solrUtil = applicationContext.getBean(SolrUtil.class);
        solrUtil.importItemData();
    }
}
