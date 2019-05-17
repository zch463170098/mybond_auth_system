package com.us.Utils;

import android.util.Log;


import com.us.listener.OnResultListener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by ZhangChenghui on 2018/3/13.
 * Email: 463170098@qq.com
 */

public class HttpUtil {

    private static  final String TAG = "HttpUtil";

    /**
     * get同步请求
     * @param url
     * @param listener
     */
    public static void getDatasync(final String url, final OnResultListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                    Request request = new Request.Builder()
                            .url(url)//请求接口。如果需要传参拼接到接口后面。
                            .build();//创建Request 对象
                    Response response = null;
                    response = client.newCall(request).execute();//得到Response 对象
                    if (response.isSuccessful()) {
                        Log.d("zch","response.code()=="+response.code());
//                        Log.d("zch","response.message()=="+response.message());
//                        Log.d("zch","res=="+response.body().string());
                        //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
                        String result = response.body().string();
                        listener.onResult(result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * get异步方式
     * @param url
     * @param listener
     */
    public static void getDataAsync(final String url, final OnResultListener listener) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    //回调的方法执行在子线程。
                    Log.i(TAG,"获取数据成功了");
                    listener.onResult(response.body().string());

                }else{
                    listener.onError();
                }
            }
        });
    }

    /**
     * post同步方式
     * @param url
     * @param data
     * @param listener
     */
    public static void postDatasync(final String url, final String data, final OnResultListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json,charset=utf-8");
                RequestBody body = RequestBody.create(JSON,data);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                if (client == null) {
                    listener.onError();
                    return;
                }
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        listener.onError();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        listener.onResult(response.body().string());

                    }
                });


            }
        }).start();
    }

    /**
     * post 异步方式
     * @param url
     * @param data
     * @param listener
     */
    public static void postDataAsync(final String url, final String data, final OnResultListener listener){
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json,charset=utf-8");
        RequestBody body = RequestBody.create(JSON,data);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        if (client == null) {
            Log.i(TAG,"cilent 初始化错误");
            listener.onError();
            return;
        }
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG,"cilent 连接错误"+e.toString());
                listener.onError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                listener.onResult(response.body().string());

            }
        });

    }

}
