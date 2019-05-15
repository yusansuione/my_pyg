package com.pinyougou.user.service.impl;

import java.util.*;

import com.alibaba.fastjson.JSON;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.user.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbUser;

import entity.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;


/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private TbUserMapper userMapper;


    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination smsDestination;

    @Value("${template_code}")
    private String template_code;
    @Value("${sign_name}")
    private String sign_name;

    /**
     * 查询全部
     */
    @Override
    public List<TbUser> findAll() {
        return userMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbUser> result = new PageResult<TbUser>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbUser> list = userMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbUser> info = new PageInfo<TbUser>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(TbUser user, String smsCode) {
        user.setCreated(new Date());//修改创建日期
        user.setPassword(DigestUtils.md5Hex(user.getPassword()));//使用MD5给密码加密
        user.setUpdated(new Date());
        userMapper.insertSelective(user);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbUser user) {
        userMapper.updateByPrimaryKeySelective(user);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbUser findOne(Long id) {
        return userMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        userMapper.deleteByExample(example);
    }


    @Override
    public PageResult findPage(TbUser user, int pageNum, int pageSize) {
        PageResult<TbUser> result = new PageResult<TbUser>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();

        if (user != null) {
            //如果字段不为空
            if (user.getUsername() != null && user.getUsername().length() > 0) {
                criteria.andLike("username", "%" + user.getUsername() + "%");
            }
            //如果字段不为空
            if (user.getPassword() != null && user.getPassword().length() > 0) {
                criteria.andLike("password", "%" + user.getPassword() + "%");
            }
            //如果字段不为空
            if (user.getPhone() != null && user.getPhone().length() > 0) {
                criteria.andLike("phone", "%" + user.getPhone() + "%");
            }
            //如果字段不为空
            if (user.getEmail() != null && user.getEmail().length() > 0) {
                criteria.andLike("email", "%" + user.getEmail() + "%");
            }
            //如果字段不为空
            if (user.getSourceType() != null && user.getSourceType().length() > 0) {
                criteria.andLike("sourceType", "%" + user.getSourceType() + "%");
            }
            //如果字段不为空
            if (user.getNickName() != null && user.getNickName().length() > 0) {
                criteria.andLike("nickName", "%" + user.getNickName() + "%");
            }
            //如果字段不为空
            if (user.getName() != null && user.getName().length() > 0) {
                criteria.andLike("name", "%" + user.getName() + "%");
            }
            //如果字段不为空
            if (user.getStatus() != null && user.getStatus().length() > 0) {
                criteria.andLike("status", "%" + user.getStatus() + "%");
            }
            //如果字段不为空
            if (user.getHeadPic() != null && user.getHeadPic().length() > 0) {
                criteria.andLike("headPic", "%" + user.getHeadPic() + "%");
            }
            //如果字段不为空
            if (user.getQq() != null && user.getQq().length() > 0) {
                criteria.andLike("qq", "%" + user.getQq() + "%");
            }
            //如果字段不为空
            if (user.getIsMobileCheck() != null && user.getIsMobileCheck().length() > 0) {
                criteria.andLike("isMobileCheck", "%" + user.getIsMobileCheck() + "%");
            }
            //如果字段不为空
            if (user.getIsEmailCheck() != null && user.getIsEmailCheck().length() > 0) {
                criteria.andLike("isEmailCheck", "%" + user.getIsEmailCheck() + "%");
            }
            //如果字段不为空
            if (user.getSex() != null && user.getSex().length() > 0) {
                criteria.andLike("sex", "%" + user.getSex() + "%");
            }

        }

        //查询数据
        List<TbUser> list = userMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbUser> info = new PageInfo<TbUser>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void createSmsCode(final String phone) {
        //产生六位随机验证码
        final String code = ((long) (Math.random() * 10000)) + "";
        redisTemplate.boundHashOps("smsCode").put(phone, code);
        //发送activemq...
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("mobile", phone);//手机号
                mapMessage.setString("template_code", template_code);//模板编号
                mapMessage.setString("sign_name", sign_name);//签名
                Map map = new HashMap();
                map.put("number", code);
                String param = JSON.toJSONString(map);
                mapMessage.setString("param", param);//根据阿里大于格式传入验证码
                return mapMessage;
            }
        });
    }

    /**
     * 验证验证码是否正确
     *
     * @param phone
     * @param code
     * @return
     */
    @Override
    public boolean checkSmsCode(String phone, String code) {
        String smsCode1 = (String) redisTemplate.boundHashOps("smsCode").get(phone);
        if (code.equals(smsCode1)) {
            return true;
        }
        return false;
    }

}
