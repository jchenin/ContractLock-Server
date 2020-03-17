package com.qiyuesuo.controller;

import com.qiyuesuo.util.SignUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

@Controller
public class SubmitController {

    /**
     * 访问本地址直接跳转表单html页面
     * @return
     */
    @RequestMapping("/upload")
    public String multiSubmit(){
        return "multiSubmit.html";
    }


    /**
     * 基于spring boot的接收表单数据的方法实现
     * @param file  对应的是<input type='file' name='file'/> 对应的是这个name的值；要是多文件上传的话
     *              只能多写几个input，多弄几个name='file1\23'了
     * @return
     * @throws IOException
     */
    @RequestMapping("/submitForm")
    @ResponseBody
    public String submitForm(@RequestParam("file") MultipartFile file) throws IOException{
        if(file != null){
            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(new File(file.getOriginalFilename())));
            out.write(file.getBytes());
            out.flush();
            out.close();
            return  "上传成功";
        }else{
            return "空的文件";
        }
    }

    /**
     * 表单里面不只有文件，还有其它字段信息
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("/multiSubmitForm")
    @ResponseBody
    public String multiSubmit(HttpServletRequest request) throws IOException {
        MultipartHttpServletRequest params = (MultipartHttpServletRequest)request;
        MultipartFile files =  ((MultipartHttpServletRequest) request).getFile("file");
        String name = params.getParameter("name");
        System.out.println("name : " + name);
        if(files != null){
            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(new File(files.getOriginalFilename())));
            out.write(files.getBytes());
            out.flush();
            out.close();
            return  "上传成功";
        }else{
            return "上传失败";
        }
    }

    /**
     * 文件上传方法
     * @param file
     * @return
     */
    @RequestMapping("/multiUpload")
    @ResponseBody
    private String multiUpload(@RequestParam("file") MultipartFile file) {
        List<String> list = SignUtil.sign();
        String xSid = list.get(0);
        String signature = list.get(1);
        System.out.println(file.getOriginalFilename());
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String uuid = null;
        String actionUrl = "http://localhost:8090/receive";
        try {
            URL url = new URL(actionUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("X-SID", xSid);
            con.setRequestProperty("X-Signature", signature);
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
            DataOutputStream ds = new DataOutputStream(con.getOutputStream());
            ds.writeBytes(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; " + "name = \"file1\"; filename = \"" + URLEncoder.encode(file.getOriginalFilename(), "UTF-8")+ "\"" + end);
            System.out.println(new String(file.getOriginalFilename().getBytes(), "UTF-8"));
            ds.writeBytes(end);
            ds.write(file.getBytes());
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
            ds.flush();
            // 取得Response内容
            InputStream is = con.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            ds.close();
            uuid = b.toString();
            System.out.println("uuid : " + b.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "文件上传成功，返回UUID: " + uuid;
    }
}
