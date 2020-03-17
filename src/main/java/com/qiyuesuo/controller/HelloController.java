package com.qiyuesuo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {
    @RequestMapping("/hello")
    @ResponseBody
    public String Hello(){
        return "Hello";
    }

    @RequestMapping("/index")
    public String index(Model model){
        model.addAttribute("name", "jack");
        model.addAttribute("age", 23);
        return "index";
    }
    @RequestMapping("/submit")
    public String submit(){
        return "submit.html";
    }


}
