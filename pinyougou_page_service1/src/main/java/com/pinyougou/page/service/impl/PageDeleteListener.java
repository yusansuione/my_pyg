package com.pinyougou.page.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.File;
import java.io.Serializable;

@Component
public class PageDeleteListener implements MessageListener {
    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] ids = (Long[]) objectMessage.getObject();
            for (Long id : ids) {
                boolean result = deletFile(id);
                System.out.println("删除商品 " + id + " 静态页：" + result);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Value("${PAGE_SERVICE_DIR}")
    private String PAGE_SERVICE_DIR;

    /**
     * 删除商品详情页
     *
     * @param goodsId
     * @return 删除结果
     */
    private boolean deletFile(Long goodsId) {
        return new File(PAGE_SERVICE_DIR + goodsId + ".html").delete();
    }
}


