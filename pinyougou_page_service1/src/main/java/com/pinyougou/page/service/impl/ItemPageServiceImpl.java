package com.pinyougou.page.service.impl;

import com.github.abel533.entity.Example;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Autowired
    private TbGoodsMapper tbGoodsMapper;
    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;
    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private TbItemMapper tbItemMapper;

    @Value("${PAGE_SERVICE_DIR}")
    private String PAGE_SERVICE_DIR;

    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            //获取连接
            Configuration cfg = freeMarkerConfig.getConfiguration();
            //获取模板
            Template template = cfg.getTemplate("item.ftl");
            Map dataMap = new HashMap();
            TbGoods tbGoods = tbGoodsMapper.selectByPrimaryKey(goodsId);
            dataMap.put("goods", tbGoods);
            TbGoodsDesc tbGoodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);
            dataMap.put("goodsDesc", tbGoodsDesc);
            //输出静态Html
            Writer out = new FileWriter(PAGE_SERVICE_DIR + goodsId + ".html");
            //获取才商品分类
            String categoryId = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
            dataMap.put("categoryId", categoryId);
            String category2Id = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
            dataMap.put("category2Id", category2Id);
            String category3Id = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();
            dataMap.put("category3Id", category3Id);

            //查询sku信息
            Example example = new Example(TbItem.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status", "1");//只查询审核通过的数据
            criteria.andEqualTo("goodsId", goodsId);//指定 id
            example.setOrderByClause("isDefault desc");//查询后进行排序
            List<TbItem> tbItemList = tbItemMapper.selectByExample(example);
            dataMap.put("itemList", tbItemList);

            template.process(dataMap, out);
            //关闭资源
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
