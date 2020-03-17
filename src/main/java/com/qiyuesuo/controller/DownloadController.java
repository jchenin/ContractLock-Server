package com.qiyuesuo.controller;

import com.alibaba.fastjson.JSONObject;
import com.qiyuesuo.util.SecretUtil;
import com.qiyuesuo.util.SignUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.SimpleFormatter;

/**
 * 文件下载controller
 */
@Controller
public class DownloadController {
    /**
     * 负责下载页面跳转后台请求
     * @return
     */
    @RequestMapping("/download")
    public String download(){
        return "download";
    }

    /**
     * 客户端下载请求
     * @param model
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/downloadFile")
    public String downloadFile(Model model, HttpServletRequest request, HttpServletResponse response) {
        //获取页面传来的uuid参数
        String uuid = request.getParameter("uuid");
        //对应服务端的下载请求路径
        String actionUrl = "http://localhost:8090/download2";
        String outPath = null;
        response.setCharacterEncoding("UTF-8");
        //数字签名两个相关属性
        List<String> list = SignUtil.sign();
        String xSid = list.get(0);
        String signature = list.get(1);
        try {
            //HttpURLConnection连接相关属性
            URL url = new URL(actionUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-SID", xSid);
            conn.setRequestProperty("X-Signature", signature);
            conn.addRequestProperty("uuid", uuid);
            conn.connect();
            InputStream in = conn.getInputStream();
            int statusCode = conn.getResponseCode();
            if(statusCode == 200) {
                //获取前端查询接口信息
                String json = new QueryPojoInfoController().queryInfo(request, response);
                JSONObject resultJson = JSONObject.parseObject(json);
                String token = resultJson.get("token").toString();
                String fileSavePath = resultJson.get("fileSavePath").toString();
                String fileTpye = resultJson.get("fileType").toString();
                String encryPath = fileSavePath + "\\" + uuid + "." + fileTpye;

                //文件保存路径
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String date = sdf.format(new Date());
                outPath = "C:\\ContractLock\\ProjectOutPath\\" + date + "\\"  + uuid + "." + fileTpye ;
//                File file = new File(outPath);
//                if (!file.exists()) {
//                    File dir = file.getParentFile();
//                    dir.mkdirs();
//                    file.createNewFile();
//                }
//                OutputStream out = new FileOutputStream(file);

                //RSA算法生成的私钥地址
                String privateKeyPath = "E:\\Key\\privateKey.key";
                SecretUtil.decrypt(encryPath, outPath, privateKeyPath,token);
                //原有下载方式，直接下载保存文件，现在换成先解密在保存数据
//                StringBuffer sb = new StringBuffer();
//                int len;
//                byte[] b = new byte[1024];
//                while ((len = in.read(b)) != -1) {
//                    out.write(b, 0, len);
//                    sb.append((char) len);
//                }
//                System.out.println(sb.toString());
                //springboot加载两个值到前端展示
                model.addAttribute("outPath", outPath);
                model.addAttribute("uuid", uuid);
                //关闭连接
                conn.disconnect();
                return "downloadSuccess";
            }else if (statusCode == 401){
                return "responseFailure";
            }else {
                return  "uuidFailure";
            }
        } catch (IOException e) {
        }
        return "uuidFailure";
    }
}
