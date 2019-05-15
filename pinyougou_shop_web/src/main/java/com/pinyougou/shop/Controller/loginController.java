package com.pinyougou.shop.Controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/login")
public class loginController {
    @RequestMapping(value = "/getname")
    public Map<String, Object> getLogin() {
        Map<String, Object> map = new HashMap<>();
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName", name);
        return map;
    }
}
