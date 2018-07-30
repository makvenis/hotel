package com.makvenis.hotel.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.makvenis.hotel.R;
import com.makvenis.hotel.xutils.DBHelp;
import com.makvenis.hotel.xutils.JavaXutilsCoffee;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.List;

@ContentView(R.layout.activity_test)
public class TestActivity extends AppCompatActivity {

    /* 获取测试控件 */
    @ViewInject(R.id.mTestTextView)
    TextView mTestTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);

        try {
            String mTxt="";
            /* 查询数据库所有数据 */
            DbManager db = DBHelp.initDb();
            List<JavaXutilsCoffee> list = db.findAll(JavaXutilsCoffee.class);
            for (int i = 0; i < list.size(); i++) {
                JavaXutilsCoffee e = list.get(i);
                String mAllString = "编号--->"+e.getId()+"\n数量--->"+e.getNum()+"\n名称--->"+e.getClassName()+"\n";
                mTxt+=mAllString;
            }
            mTestTextView.setText("数据为："+mTxt);
        } catch (DbException e) {
            e.printStackTrace();
        }


    }


}
