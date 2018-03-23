package com.porster.gift.sdk;

import android.content.Context;

import cn.waps.AppConnect;

/**
 * 万普世纪
 * Created by Porster on 2017/9/21.
 */

public class WAPS {
    public static String APP_ID="1227eb1d0fd394f06a66da7a0308d6de";

    public static void init(Context context){
        AppConnect.getInstance(context);
    }
    public static void exit(Context context){
        AppConnect.getInstance(context).close();
    }
}
