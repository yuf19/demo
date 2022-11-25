package com.example.demo.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoUtil {
    //生成随机的字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    //MD5加密
    //hello->abc123def456
    //hello+3e4a8->abc123def4565t4t5
    public static String MD5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        //此注释仅用于推送测试
        String id=UUID.randomUUID().toString();
        System.out.println(id);
        System.out.println(id.length());
        System.out.println(id.replaceAll("-",""));
        System.out.println(id.replaceAll("-","").length());
        Map<String, Object> map = new HashMap<>();
        System.out.println(map==null);
        System.out.println(map.isEmpty());
        map.put("name", "张三");
        map.put("age", 21);
        System.out.println(getJSONString(0, "ok", map));
        JSONObject json = new JSONObject();
        json.put("code",001);
        json.put("msg","正常");
        System.out.println(json.toJSONString());
        System.out.println(json);
        System.out.println(JSON.toJSONString(json));
        System.out.println(JSONObject.toJSONString(json));
    }
}
