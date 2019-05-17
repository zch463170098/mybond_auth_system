package com.us.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.us.module.R;


public class SplashActivity extends Activity {
    private boolean isIn = false;
    private ImageView mIvPic;
    private TextView mTvJump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
    }

    private void initView() {
        mIvPic = (ImageView) findViewById(R.id.splash_iv_pic);
        mTvJump = (TextView) findViewById(R.id.splash_tv_jump);
        mTvJump.setOnClickListener(new View.OnClickListener() {
            /**
             * 点击跳过直接进入登录页面
             * @param v
             */
            @Override
            public void onClick(View v) {
                toLoginAcivity();
            }
        });

        //显示默认的图片
        mIvPic.setImageDrawable(getResources().getDrawable(R.drawable.img_transition_default));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toLoginAcivity();

            }
        },5000);
    }


    /**
     * 跳转到登录页面
     */
    private void toLoginAcivity() {
        if (isIn) {
            return;
        }
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        //定义切换Activity的样式
        overridePendingTransition(R.anim.screen_zoom_in, R.anim.screen_zoom_out);
        finish();
        isIn = true;
    }
}
