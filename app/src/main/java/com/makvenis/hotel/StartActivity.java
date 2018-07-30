package com.makvenis.hotel;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.makvenis.hotel.activity.AdvActivity;
import com.makvenis.hotel.activity.MainActivity;
import com.makvenis.hotel.registe.LoginActivity;
import com.makvenis.hotel.tools.Configfile;
import com.makvenis.hotel.utils.AppStaticUtils;
import com.makvenis.hotel.xutils.AppHelpMethod;


/* APP 主入口函数 */
@ContentView(R.layout.activity_satrt)
public class StartActivity extends AppCompatActivity {

    @ViewInject(R.id.mStartImage)
    ImageView mStartImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        ViewUtils.inject(this);

        /* 日志 */
        Log.e("LOG","进入---StartActivity---启动页面");

        /* 设置启动动画 */
        createAnimation();
    }
    /* 延时执行 */
    boolean mHandler = new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
            //判断当前是否开启了 广告栏位
            if(Configfile.IS_ADV){
                Intent intent=new Intent(StartActivity.this, AdvActivity.class);
                startActivity(intent);
            }else {
                Intent intent=null;
                /* 获取用户是否第一次登陆 */
                boolean firstStartApp = AppStaticUtils.isFirstStartApp(StartActivity.this);
                /* 日志 */
                Log.e("LOG","进入---#isFirstStartApp()---判断是否第一次进入APP---值为"+firstStartApp);
                if(firstStartApp){
                    intent=new Intent(StartActivity.this, LoginActivity.class);
                }else {
                    /**
                     * 不是第一次登陆，则需要判断当前本地数据库是否存在有用户的基本信息
                     */
                    boolean existBool = AppHelpMethod.isExistBool("select data from user where key='localuser'"
                            , new String[]{"data"});
                    Log.e("LOG","进入---#isFirstStartApp()---判断是否本地数据库是否有用户的基本信息---值为"+existBool);
                    if(existBool){
                        //主页面
                        intent=new Intent(StartActivity.this, MainActivity.class);
                    }else {
                        //登陆页面
                        intent=new Intent(StartActivity.this, LoginActivity.class);
                    }
                }
                startActivity(intent);
                finish();
            }
        }
    }, 3000);

    private void createAnimation() {
        //String mPath="http://p0.so.qhmsg.com/bdr/_240_/t013c40ec1a5671d1df.jpg";
        //Picasso.with(this).load(mPath)
        //        .placeholder(R.drawable.ixon_start_error_01)
        //        .error(R.drawable.ixon_start_error_01)
        //        .into(mStartImage);
        /* 等待图片下载完成 */
        mStartImage.setImageResource(R.drawable.ixon_start_error_01);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //定义动画集合
        AnimationSet set=new AnimationSet(true);

        //缩放
        Animation alphAnimation=new AlphaAnimation(0f, 1f);
        alphAnimation.setDuration(3000);//设置动画持续时间为3秒
        alphAnimation.setFillAfter(true);//设置动画结束后保持当前的位置（即不返回到动画开始前的位置）
        //渐变

        Animation scaleAnimation= AnimationUtils.loadAnimation(this, R.anim.layout_start_animation);
        //加载Xml文件中的动画imgShow.startAnimation(scaleAnimation2);

        set.addAnimation(alphAnimation);
        set.addAnimation(scaleAnimation);
        mStartImage.startAnimation(set);

    }
}
