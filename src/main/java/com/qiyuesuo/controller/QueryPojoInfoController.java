package com.qiyuesuo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qiyuesuo.util.SecretUtil;
import com.qiyuesuo.util.SignUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 根据指定uuid查询相关元数据
 */
@Controller
public class QueryPojoInfoController {
    @RequestMapping("/query")
    public String query(){
        return "query";
    }

    /**
     * 查询方法
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/queryInfo")
    @ResponseBody
    public String queryInfo(HttpServletRequest request, HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        //数字签名相关
        List<String> list = SignUtil.sign();
        String xSid = list.get(0);
        String signature = list.get(1);
        String uuid = request.getParameter("uuid");
        if(uuid == null || "".equals(uuid)){
            return "uuid错误，请重新输入";
        }
        String actionUrl = "http://localhost:8090/queryPojoInfo";
        try {
            URL url = new URL(actionUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");
            conn.addRequestProperty("uuid", uuid);
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
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//                String date = sdf.format(new Date());
//                outPath = "C:\\ContractLock\\ProjectOutPath\\" + date ;;
//                File file = new File(outPath + "\\" + uuid);
//                if (!file.exists()) {
//                    File dir = file.getParentFile();
//                    dir.mkdirs();
//                    file.createNewFile();
//                }
//                OutputStream out = new FileOutputStream(file);
                StringBuffer sb = new StringBuffer();
                int len;
                while ((len = in.read()) != -1) {
                    sb.append((char) len);
                }
                System.out.println(sb.toString());
                String result = sb.toString();
                JSONObject json = JSONObject.parseObject(result);
                return JSONObject.toJSONString(json);
            }else if (statusCode == 401){
                return "响应异常";//JSON.parseObject("响应异常");
            }else {
                return  "uuid错误";//JSON.parseObject("uuid错误");
            }
        } catch (Exception e) {
        }
        return "服务端出现未知错误";
    }
}
