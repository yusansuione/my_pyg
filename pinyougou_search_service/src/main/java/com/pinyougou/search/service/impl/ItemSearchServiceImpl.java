package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.search.service.ItemSearchService;
import entity.SolrItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 10000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 搜索查询
     *
     * @param searchMap 搜索条件列
     * @return
     */
    @Override
    public Map search(Map searchMap) {

       /* //创建查询类
        Query query=new SimpleQuery("*:*");
        //组装查询条件
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //分页查询
        ScoredPage<SolrItem> page= solrTemplate.queryForPage(query, SolrItem.class);
            map.put("rows",page.getContent() );*/

        Map map = new HashMap();
        if (searchMap != null) {
//        1.商品列表
            searchList(searchMap, map);
//        2.列表查询
            searchCategoryList(searchMap, map);
//       3.商品品牌规格查询
            String category = (String) map.get("category") == null ? "" : (String) searchMap.get("category");
            if (category.trim().length() < 1) {
                if (map.get("categoryList") != null) {
                    List<String> categoryList = (List) map.get("categoryList");
                    category = categoryList.get(0);
                }
            }
            searchBrandAndSpecList(map, category);
        }
        return map;
    }

    /**
     * 保存索引
     *
     * @param list
     */
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(Long[] goodsIdList) {
        SolrDataQuery query = new SimpleQuery();
        Criteria ctiteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(ctiteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


    /**
     * 查询分类列表-使用spring data solr的分组查询
     *
     * @param searchMap 分组的条件
     * @param map       返回结果分装的对象
     */
    private void searchCategoryList(Map searchMap, Map map) {
        List<String> list = new ArrayList();
//        1.	创建查询条件对象query = new SimpleQuery()
        Query query = new SimpleQuery();
//        2.	复制之前的Criteria组装查询条件的代码
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
//        3.	创建分组选项对象new GroupOptions().addGroupByField(域名)
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
//        4.	设置分组对象query.setGroupOptions
        query.setGroupOptions(groupOptions);
//        5.	得到分组页对象page = solrTemplate.queryForGroupPage
        GroupPage<SolrItem> page = solrTemplate.queryForGroupPage(query, SolrItem.class);
//        6.	得到分组结果集groupResult = page.getGroupResult(域名)
        GroupResult<SolrItem> groupResult = page.getGroupResult("item_category");
//        7.	得到分组结果入口groupEntries = groupResult.getGroupEntries()
        Page<GroupEntry<SolrItem>> groupEntries = groupResult.getGroupEntries();
//        8.	得到分组入口集合content = groupEntries.getContent()
        List<GroupEntry<SolrItem>> content = groupEntries.getContent();
//        9.	遍历分组入口集合content.for(entry)，记录结果entry.getGroupValue()
        for (GroupEntry<SolrItem> itemGroupEntry : content) {
            list.add(itemGroupEntry.getGroupValue());
        }
        map.put("categoryList", list);
    }

    /**
     * 搜索商品列表
     *
     * @param searchMap
     * @param map
     */
    public void searchList(Map searchMap, Map map) {
        if (searchMap != null) {
            // 2.   构建query高亮查询对象
            HighlightQuery query = new SimpleHighlightQuery();

            //3.6去空格分词查询
            String keywords = (String) searchMap.get("keywords");
            keywords = keywords.replace(" ", "");

            //3.  Criteria组装查询条件的代码
            Criteria criteria = new Criteria("item_keywords").is(keywords);
            query.addCriteria(criteria);

            //3.1	按品牌筛选
            if (!"".equals(searchMap.get("brand"))) {
                Criteria filtercriteria = new Criteria("item_brand").is(searchMap.get("brand"));
                FilterQuery filterquery = new SimpleQuery(filtercriteria);
                query.addFilterQuery(filterquery);
            }
            //3.2 按分类筛选
            if (!"".equals(searchMap.get("category"))) {
                Criteria filtercriteria = new Criteria("item_category").is(searchMap.get("category"));
                FilterQuery filterquery = new SimpleQuery(filtercriteria);
                query.addFilterQuery(filterquery);
            }

            //3.3过滤规格
            if (searchMap.get("spec") != null) {
                Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
                for (String key : specMap.keySet()) {
                    Criteria example = new Criteria("item_spec_" + key).is(specMap.get(key).toString());
                    FilterQuery filterquery = new SimpleQuery(example);
                    query.addFilterQuery(filterquery);
                }
            }

            //3.4 按照价格筛选
            if (!"".equals(searchMap.get("price"))) {
                String[] price = ((String) searchMap.get("price")).split("-");
                if (!price[0].equals('0')) {
                    Criteria filtercriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                    FilterQuery filterquery = new SimpleQuery(filtercriteria);
                    query.addFilterQuery(filterquery);
                }
                if (!price[1].equals("*")) { //如果区间终点不等于*
                    Criteria filtercriteria = new Criteria("item_price").lessThanEqual(price[1]);
                    FilterQuery filterquery = new SimpleQuery(filtercriteria);
                    query.addFilterQuery(filterquery);
                }
            }

            //3.5 分页查询
            Integer pageNo = searchMap.get("pageNo") == null ? 1 : new Integer(searchMap.get("pageNo").toString());
            //每页查询的记录数
            Integer pageSize = searchMap.get("pageSize") == null ? 20 : new Integer(searchMap.get("pageSize").toString());

            query.setOffset((pageNo - 1) * pageSize);
            query.setRows(pageSize);

            //3.7 排序
            String fieldsort = searchMap.get("filedsort") == null ? "" : searchMap.get("filedsort").toString();
            String sort = searchMap.get("sort") == null ? "" : searchMap.get("sort").toString();
            if (sort.equalsIgnoreCase("DESC")) {
                Sort sortDesc = new Sort(Sort.Direction.DESC, "item_" + fieldsort);
                query.addSort(sortDesc);
            }
            if (sort.equalsIgnoreCase("ase")) {
                Sort sortAsc = new Sort(Sort.Direction.ASC, "item_" + fieldsort);
                query.addSort(sortAsc);
            }


            //     4.   调用query.setHighlightOptions()方法，构建高亮数据三步曲：addField(高亮业务域)，
//              .setSimpleP..(前缀)，.setSimpleP..(后缀)

            HighlightOptions hOptions = new HighlightOptions().addField("item_title");
            hOptions.setSimplePrefix("<em style='color:red;'>");
            hOptions.setSimplePostfix("</em>");
            //     1.   调用solrTemplate.queryForHighlightPage(query,class)方法，高亮查询数据
            query.setHighlightOptions(hOptions);
//        5.   接收solrTemplate.queryForHighlightPage的返回数据，定义page变量

            HighlightPage<SolrItem> page = solrTemplate.queryForHighlightPage(query, SolrItem.class);

            //6.   遍历解析page对象，page.getHighlighted().for，item = h.getEntity()，
//       item.setTitle(h.getHighlights().get(0).getSnipplets().get(0))，在设置高亮之前最好判断一下;

            for (HighlightEntry<SolrItem> h : page.getHighlighted()) {
                if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
                    SolrItem solrItem = h.getEntity();
                    solrItem.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
                }
            }
            //  7.   在循环完成外map.put("rows", page.getContent())返回数据列表
            map.put("rows", page.getContent());

            map.put("total", page.getTotalElements());//返回总记录数
            map.put("totalpages", page.getTotalPages());//返回总页数
        }
    }

    /**
     * 根据商品分类查询商品品牌规格列表
     *
     * @param map      返回结果集
     * @param catetory 商品分类
     */
    public void searchBrandAndSpecList(Map map, String catetory) {
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(catetory);
        if (catetory != null) {
            List paceList = (List) redisTemplate.boundHashOps("specIds").get(typeId);
            map.put("specList", paceList);
            List brandList = (List) redisTemplate.boundHashOps("brandIds").get(typeId);
            map.put("brandList", brandList);
        }
    }

}
