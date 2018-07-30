package com.makvenis.hotel.utils;

/* 与服务器交互的基本统一的解析类 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link JSON} 通过本类解析一些普通格式的JSON或者固定格式的JSON字符串
 * {@link #GetJson(String, String[])} 解析依次为jsonArray --->>  jsonObject
 * {@link #getObjectJson(String, String[])}  解析只有一层 jsonObject
 *
 */

public class JSON {

    /* 统一数据JSON */
    /**
     * @ 解释 使用于当只有一层的时候 jsonArray --->>  jsonObject
     * @param mJson 传递过来的原始参数
     * @param keyString 需要解析的键值对关系
     */
    public static List<Map<String,String>> GetJson(String mJson, String[] keyString){
        if(mJson == null || keyString == null){
            return new ArrayList<>();
        }

        try {
            List<Map<String,String>> data = new ArrayList<>();

            //便利所有的键
            List<String> mKey = new ArrayList<>();
            for (String str:keyString) {
                mKey.add(str);
            }

            JSONArray arr = new JSONArray(mJson);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject object=arr.getJSONObject(i);

                Map<String, String> map=new HashMap<>();
                for (int j = 0; j < mKey.size(); j++) {
                    map.put(mKey.get(j), object.optString(mKey.get(j)));
                }
                data.add(map);
            }
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return new ArrayList<>();
    }

    /**
     * {@linkplain=Android} 适应与当只有一层Object的时候
     * 使用此方法
     * @param json
     * @param key string[]{}
     * @return 返回的类型  Map<String, Object>
     */
    public static Map<String, Object> getObjectJson(String json,String[] key) {
        try {
            if(json == null || key.length == 0)
                return new HashMap<>();
            //返回的集合
            Map<String, Object> data=new HashMap<>();
            JSONObject object=new JSONObject(json);
            List<String> mKey=new ArrayList<>();
            for (int i = 0; i < key.length; i++) {
                mKey.add(key[i]);
            }

            for (int i = 0; i < mKey.size(); i++) {
                data.put(mKey.get(i), object.get(mKey.get(i)));
            }
            if(data.size() != 0)
                return data;

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new HashMap<>();
    }


    /**
     * 创建json对象
     * @param key     键
     * @param value   值
     * @param jxJson  告诉服务器你解析那些对象
     * @return
     */
    public static String createJson(String[] key,Object[] value,String jxJson) {

        JSONObject object=new JSONObject();
        if(key.length == value.length) {
            for (int i = 0; i < key.length; i++) {
                try {
                    object.put(key[i], value[i]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            try {
                object.put("jx", jxJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return object.toString();
        }else {
            new IllegalArgumentException("键的长度与值的长度不相等"+" key >>>"+key.length +" : "+" value >>>" + value.length);
        }
        return null;

    }


}
