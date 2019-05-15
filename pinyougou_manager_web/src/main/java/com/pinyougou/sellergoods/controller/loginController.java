package com.pinyougou.sellergoods.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/login")
public class loginController {

    @RequestMapping(value = "/getName")
    public Map<String, Object> loginName() {

        Map<String, Object> map = new HashMap<>();
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginUsername", name);
        return map;
    }
}
