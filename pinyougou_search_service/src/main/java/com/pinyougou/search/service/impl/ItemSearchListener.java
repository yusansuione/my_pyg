package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import entity.SolrItem;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    /**
     * 添加索引库
     *
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            List<TbItem> tbItemList = JSON.parseArray(textMessage.getText(), TbItem.class);
            SolrItem solrItem = null;
            List<SolrItem> solrItems = new ArrayList<>();
            for (TbItem tbItem : tbItemList) {
                solrItem = new SolrItem();
                BeanUtils.copyProperties(tbItem, solrItem);
                solrItem.setSpecMap((Map<String, String>) JSON.parse(tbItem.getSpec()));
                solrItems.add(solrItem);
            }
            itemSearchService.importList(solrItems);
            System.out.println("成功到入索引库!");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
