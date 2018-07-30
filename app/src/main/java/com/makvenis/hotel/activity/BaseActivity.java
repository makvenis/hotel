package com.makvenis.hotel.activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.makvenis.hotel.R;
import com.makvenis.hotel.xutils.AppHelpMethod;

import java.util.List;
import java.util.Map;

@ContentView(R.layout.activity_base)
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);

    }

    public static List<Map<String, Object>> maps;

    /* 获取全局用户信息 */
    static {
        maps = AppHelpMethod.queryBySql(
                "select data from user where key = 'localuser'"
                , new String[]{"data"});
    }

    /**
     * 通过方法{@link #getUserData()} 获取用户的基本信息json格式
     * @return
     */
    public List<Map<String, Object>> getUserData(){
        return maps;
    }

    /**
     *获取用户的登陆次数
     */
    public int getSystemNum(){
        SharedPreferences sdf = getSharedPreferences("start",MODE_PRIVATE);
        return sdf.getInt("count", 0);
    }
}
