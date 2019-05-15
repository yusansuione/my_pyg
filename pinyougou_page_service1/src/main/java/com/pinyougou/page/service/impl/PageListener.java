package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class PageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;

    /**
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        try {
            //获取消息内容
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] ids = (Long[]) objectMessage.getObject();
            //遍历生成html
            for (Long id : ids) {
                boolean result = itemPageService.genItemHtml(id);
                System.out.println("生成商品 " + id + " 静态页：" + result);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
