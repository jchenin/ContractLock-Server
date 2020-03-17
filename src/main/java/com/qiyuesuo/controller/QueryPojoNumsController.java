package com.qiyuesuo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qiyuesuo.util.SignUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询最近十个上传元数据信息controller
 */
@Controller
public class QueryPojoNumsController {
    @RequestMapping("/queryNums")
    @ResponseBody
    public String queryPojoNums(HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        List<String> list = SignUtil.sign();
        String xSid = list.get(0);
        String signature = list.get(1);
        String actionUrl = "http://localhost:8090/queryPojoNums";
        try {
            URL url = new URL(actionUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-SID", xSid);
            conn.setRequestProperty("X-Signature", signature);
            conn.connect();
            InputStream in = null;
            try {
                in = conn.getInputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
            int statusCode = conn.getResponseCode();
            if(statusCode == 200) {
                StringBuffer sb = new StringBuffer();
                int len;
                while ((len = in.read()) != -1) {
                    sb.append((char) len);
                }
                System.out.println(sb.toString());
                //读取服务端返回数据信息，解码，转换为json,获取文件名
                String result = URLDecoder.decode(sb.toString(), "UTF-8");
                List<String> fileNameList = new ArrayList<>();
                JSONArray jsonArray = JSONArray.parseArray(result);
                for(int i = 0; i < jsonArray.size(); i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    String filaName = object.get("fileName").toString();
                    fileNameList.add(filaName);
                }
                return fileNameList.toString();
            }else {
                return  "响应异常";//JSON.parseObject("uuid错误");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return "查询出错";
    }
}
