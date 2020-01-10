package com.smart.reyog;

import com.smart.reyog.Retrofit.APIService;
import com.smart.reyog.Retrofit.RetrofitClient;

public class Common {

    //public static String currentToken = "";

    private static String baseUrl = "https://fcm.googleapis.com/";

    public static APIService getFCMClient(){

        return RetrofitClient.getClient(baseUrl).create(APIService.class);
    }
}
