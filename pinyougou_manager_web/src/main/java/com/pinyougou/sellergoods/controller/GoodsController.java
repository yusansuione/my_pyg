package com.pinyougou.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.Goods;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * 请求处理器
 *
 * @author Steven
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

//	@Reference
//	private ItemSearchService itemSearchService;

//	@Reference
//	private ItemPageService itemPageService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination queueSolrDestination;

    @Autowired
    private Destination queueSolrDeleteDestination;

    @Autowired
    private Destination topicPageDestination;

    @Autowired
    private Destination topicPageDeleteDestination;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * 增加
     *
     * @param goods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbGoods goods) {
        try {
            goodsService.add(goods);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(final Long[] ids) {
        try {
            goodsService.delete(ids);
//			itemSearchService.deleteByGoodsIds(ids);
            //消息队列
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    //发送序列化对象消息
                    return session.createObjectMessage(ids);
                }
            });

            jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    /**
     * 更改商品状态
     *
     * @param ids
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatu(final Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
            if ("1".equals(status)) {
                List<TbItem> itemList = goodsService.findItemListByGoodsIdsAndStatus(ids, status);
                if (itemList != null && itemList.size() > 0) {
					/*List<SolrItem> solrItemList=new ArrayList<>();
					SolrItem solrItem=null;
					for (TbItem tbItem : itemList) {
						solrItem=new SolrItem();
						BeanUtils.copyProperties(tbItem,solrItem);
						Map specmap=JSON.parseObject(tbItem.getSpec());
						solrItem.setSpecMap(specmap);
						solrItemList.add(solrItem);
					}*/
//					itemSearchService.importList(solrItemList);
                    //创建activeMQ提供者
                    final String itemString = JSON.toJSONString(itemList);
                    jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {

                            return session.createTextMessage(itemString);
                        }
                    });

					/*//生成商品静态化
					for (Long id : ids) {
						itemPageService.genItemHtml(id);
					}*/
                    //
                    jmsTemplate.send(topicPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createObjectMessage(ids);
                        }
                    });

                } else {
                    System.out.println("没有找到SKU列表信息");
                }
            }
            return new Result(true, "操作成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "操作失败");
    }

    /**
     * 生成静态页
     * @param goodsId
     * @return
     */
//	@RequestMapping("/genHtml")
//	public boolean getHtml(Long goodsId){
//		return itemPageService.genItemHtml(goodsId);
//	}

}
