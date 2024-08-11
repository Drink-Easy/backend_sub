package com.drinkeg.drinkeg.controller;

import com.drinkeg.drinkeg.apipayLoad.ApiResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// 테스트용 컨드롤러
@Controller
public class MainController {

    @GetMapping("/maindy")
    @ResponseBody
    public String mainAPI() {

        return "main route";

    }

    @GetMapping("/main")
    public ApiResponse<?> mainP() {
        return ApiResponse.onSuccess("하윙");
    }
}
