package com.us.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.us.Utils.HttpUtil;
import com.us.Utils.Tools;
import com.us.entity.OperationResult;
import com.us.entity.UserLogin;

import com.us.listener.OnResultListener;
import com.us.module.R;


public class LoginActivity extends Activity {

    private final  String TAG = "LoginActivity";

    private Boolean isIn = false;

    private EditText userName;
    private EditText password;
    private Button button_login;
    private Button button_register;

    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        userName = (EditText) findViewById(R.id.editUserName);
        password = (EditText)findViewById(R.id.editPassword);
        button_login = (Button)findViewById(R.id.button_login);
        button_register = (Button)findViewById(R.id.button_register);

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toRegisterActivity();
            }
        });
    }

    private void toRegisterActivity() {

        if (isIn) {
            return;
        }
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        //定义切换Activity的样式
        overridePendingTransition(R.anim.screen_zoom_in, R.anim.screen_zoom_out);
        finish();
        isIn = true;

    }

    private void login() {
        String name,pass;
        name = userName.getText().toString();
        pass=password.getText().toString();

        if(Tools.isNull(name)){
            userName.setError("用户名不能为空");
            return;
        }

        if(Tools.isNull(pass)){
            password.setError("用户密码不能为空");
            return;
        }

        UserLogin userLogin = new UserLogin();
        userLogin.setUserName(name);
        userLogin.setPassword(pass);
         String loginData = gson.toJson(userLogin);
         String loginUrl = Tools.IpAdresss + "user/login";
        HttpUtil.postDataAsync(loginUrl, loginData, new OnResultListener() {
            @Override
            public void onResult(String result) {
                Log.i(TAG,"接收到登录结果"+result);
                OperationResult operationResult = gson.fromJson(result,OperationResult.class);
                if(operationResult.isStatus()){
                    Log.i(TAG,"登录成功");
                    toMainAcivity();
                }else{
                    Log.i(TAG,"登录失败"+operationResult.getMessage());

                    Looper.prepare();
                    showDialog("系统提示", "登陆失败，请检查密码是否正确",
                            LoginActivity.this, "重新登陆");
                    Looper.loop();
                }
            }

            @Override
            public void onError() {

                Log.i(TAG,"出现异常错误，检查网络连接或者url是否正确"+loginUrl);

            }
        });

    }

    private void toMainAcivity() {

        if (isIn) {
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //定义切换Activity的样式
        overridePendingTransition(R.anim.screen_zoom_in, R.anim.screen_zoom_out);
        finish();
        isIn = true;
    }

    public void showDialog(String title, String msg, Context contacts,
                           String buttopn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(contacts);
        builder.setMessage(msg);
        builder.setTitle(title);
        builder.setNegativeButton(buttopn,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

}
