package com.us.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Path;
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
import com.us.entity.User;
import com.us.listener.OnResultListener;
import com.us.module.R;

public class RegisterActivity extends Activity {

    private final  String TAG = "RegisterActivity";

    private Boolean isIn = false;

    private EditText username;
    private EditText password;
    private EditText email;
    private EditText phone;
    private Button button_reg;

    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    private void initView() {
        username = findViewById(R.id.edit_user);
        password = findViewById(R.id.edit_pass);
        email = findViewById(R.id.editEmail);
        phone = findViewById(R.id.editPhone);
        button_reg = findViewById(R.id.button_reg);

        button_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        String myUser,myPass,myEmail,myPhone;
        myUser = username.getText().toString();
        myPass = password.getText().toString();
        myEmail = email.getText().toString();
        myPhone = phone.getText().toString();

        if(Tools.isNull(myUser)){
            username.setError("用户名不能为空");
            return;
        }
        if(Tools.isNull(myPass)){
            password.setError("密码不能为空");
            return;
        }
        if(Tools.isNull(myEmail)){
            email.setError("用户邮箱不能为空");
            return;
        }
        if(Tools.isNull(myPhone)){
            phone.setError("用户联系方式不能为空");
            return;
        }

        User user =new User(myUser,myPass,myEmail,myPhone);
        String registerData = gson.toJson(user);
        String registerUrl = Tools.IpAdresss + "user/addUser";
        HttpUtil.postDataAsync(registerUrl, registerData, new OnResultListener() {
            @Override
            public void onResult(String result) {
                Log.i(TAG,"接收到注册结果"+result);
                OperationResult operationResult = gson.fromJson(result,OperationResult.class);
                if(operationResult.isStatus()){
                    Log.i(TAG,"注册成功");
                    toLoginActivity();
                }else{
                    Log.i(TAG,"注册失败"+operationResult.getMessage());
                    Looper.prepare();
                    showDialog("系统提示", "注册失败，用户名已存在",
                            RegisterActivity.this, "重新注册");
                    Looper.loop();
                }
            }

            @Override
            public void onError() {
                Log.i(TAG,"连接失败"+registerUrl);

            }
        });
        toLoginActivity();
    }

    private void toLoginActivity() {
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
