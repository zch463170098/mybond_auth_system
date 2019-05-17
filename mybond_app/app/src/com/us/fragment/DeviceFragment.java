package com.us.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.us.Utils.UtilsTools;
import com.us.adaptr.FragAdapter;
import com.us.view.MainActivity;
import com.us.module.R;
import com.us.listener.UpdateChartsListener;
import com.us.listener.UpdateListListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.us.ble.central.BLEDevice;
import com.us.ble.central.L;
import com.us.ble.listener.BLEDeviceListener;
import com.us.ble.listener.DeviceMessageListener;
import com.us.ble.listener.ErrorListener;
import com.us.ble.listener.HistoryDataListener;
import com.us.ble.listener.OtherDataListener;
import com.us.ble.listener.RealtimeDataListener;

import static com.us.Utils.UtilsTools.stampToDate;

@SuppressLint("ValidFragment")
public class DeviceFragment extends Fragment {
    private ViewPager device_viewpager;
    private List<Fragment> fragments;
    private FragAdapter adapter;
    private BLEDevice bleDevice;
    private View rootView = null;// 缓存Fragment com.us.view
    public DeviceFragment mDeviceFragment;
    private MainActivity mActivity;
    public UpdateChartsListener mUpdateChartsListener;
    private String TAG = "DeviceFragment";
    FileOutputStream fos_acceleration; //加速度输出文件流
    FileOutputStream fos_rawHeart;  //心率裸数据文件流
    FileOutputStream fos_realHeart;  // 实时心率文件流
    FileOutputStream fos_realTemperate;  //实时体温文件流
    FileOutputStream fos_historyHeart;  //历史心率文件流
    FileOutputStream fos_historyTemperate; //历史体温文件流
    FileOutputStream fos_historySport;  //历史运动
    FileOutputStream fos_realSport;  //实时运动
    int[] tep = new int[3];//加速度裸数据数组
    public UpdateListListener mUpdateListListener;
    private  final  String filepath = "/sdcard/ustone/";

    private final  Handler msgHandler = new Handler() {
        public void handleMessage(Message msg) {
            String text = (String) msg.obj;
            switch (msg.arg1) {
                case 1:
                    Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (MainActivity) activity;
    }

    public DeviceFragment(BLEDevice device) {
        if (device != null) {
            this.bleDevice = device;
        }
        mDeviceFragment = this;
    }

    public void setUpdateListListener(UpdateListListener updateListListener) {
        if (updateListListener != null)
            mUpdateListListener = updateListListener;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragments = new ArrayList<Fragment>();
        fragments.add(new DeviceListFragment(bleDevice, mDeviceFragment));
        adapter = new FragAdapter(getChildFragmentManager(), fragments);
    }

    /**
     * 装配显示的页面
     * @param inflater 布局
     * @param container 容器
     * @param savedInstanceState 状态
     * @return 返回页面
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.device_viewpager, container, false);
            device_viewpager = (ViewPager) rootView.findViewById(R.id.device_viewPager);
            device_viewpager.setOffscreenPageLimit(2);
            device_viewpager.setAdapter(adapter);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        //下面两个看需要具体用哪一个
        setBLEDeviceListener();
        setAnalysisListener();
        return rootView;
    }

    /**
     *初始化加速度文件
     */
    public void initAccelerationFile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        final String path = filepath+"Acceleration/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String SavePath =  "acceleration"+str+".txt";
        File saveFile = new File(SavePath);
        try {
            fos_acceleration = new FileOutputStream(path + saveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化心率裸数据文件
     */
    public void initRawHeartFile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        final String path = filepath+"RawHeart/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String SavePath =  "rawHeart"+str+".txt";
        File saveFile = new File(SavePath);
        try {
            fos_rawHeart = new FileOutputStream(path + saveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化实时心率文件
      */
    public void initRealHeartFile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        final String path = filepath+"RealHeart/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String SavePath =  "realHeart"+str+".txt";
        File saveFile = new File(SavePath);
        try {
            fos_realHeart = new FileOutputStream(path + saveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initHistoryHeartFile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        final String path = filepath+"HistoryHeart/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String SavePath =  "historyHeart"+str+".txt";
        File saveFile = new File(SavePath);
        try {
            fos_historyHeart = new FileOutputStream(path + saveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initRealTemperateFile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        final String path = filepath+"RealTemperate/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String SavePath =  "realTemperate"+str+".txt";
        File saveFile = new File(SavePath);
        try {
            fos_realTemperate = new FileOutputStream(path + saveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initHistoryTemperateFile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        final String path = filepath+"HistoryTemperate/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String SavePath =  "historyTemperate"+str+".txt";
        File saveFile = new File(SavePath);
        try {
            fos_historyTemperate = new FileOutputStream(path + saveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initRealSportFile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        final String path = filepath+"RealSport/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String SavePath =  "realSport"+str+".txt";
        File saveFile = new File(SavePath);
        try {
            fos_realSport = new FileOutputStream(path + saveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initHistorySportFile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        final String path = filepath+"HistorySport/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String SavePath =  "historySport"+str+".txt";
        File saveFile = new File(SavePath);
        try {
            fos_historySport = new FileOutputStream(path + saveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    /**
     * 设置数据已经解析的接口
     */
    private void setAnalysisListener() {
        bleDevice.setErrorListener(new ErrorListener() {
            @Override
            public void onError(String address, int cmd, int errorCode) {  //命令是否发送成功，出错提示
                String text = getString(R.string.result_order) + ":0x" + Integer.toHexString(cmd) + "," + getString(R.string.send_result) + ":" + errorCode;
                Message msg = msgHandler.obtainMessage();
                msg.arg1 = 1;
                msg.obj = text;
                Log.i("DeviceFragment",text);
                if (errorCode != 0x0c)  //Ox0c 代表包数据不完整
                    msgHandler.sendMessage(msg);
            }
        });
        bleDevice.setRealtimeDataListener(new RealtimeDataListener() {
            int tempTemperate = 0 ;
            @Override
            public void onRealtimeTemperature(String address, float temperature) {
                String temp = "体温: " + temperature + "°";
                if (mUpdateListListener != null)
                    mUpdateListListener.onRealtimeData(address, temp);
                //TODO　把体温数据写进文件中
                if(mActivity.get_temprateneed_towrite()){
                    if(tempTemperate == 0){
                        initRealTemperateFile();
                        tempTemperate++;
                    }
                    L.i(TAG, "实时体温数据需要写入文件");
                     saveRealTemperateToFle(temperature+"");
                }else{
//                    if (mUpdateListListener != null)
//                        mUpdateListListener.onRealtimeData(address, temp);
                    L.i(TAG, "实时体温数据不需要写入文件");
                    if(fos_realTemperate!=null){
                        try {
                            fos_realTemperate.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            int tempSport  = 0;
            @Override
            public void onRealtimeSports(String address, int step, int distance, int calory) {
                String sports = getString(R.string.steps) + ":" + step + " ，" + getString(R.string.distance) + ":" + distance + "m , " + " ，" + getString(R.string.Calorie) + ":" + calory + "cal";
                if (mUpdateListListener != null)
                    mUpdateListListener.onRealtimeData(address, sports);
                //TODO 运动数据写进文件中
                if(mActivity.get_realSport_towrite()){
                    if(tempSport == 0){
                        initRealSportFile();
                        tempSport++;
                    }
                    L.i(TAG, "实时运动数据需要写入文件");
                    saveRealSportToFle(sports);
                }else{
                    if (mUpdateListListener != null)
                        mUpdateListListener.onRealtimeData(address, sports);
                    L.i(TAG, "实时运动数据不需要写入文件");
                    if(fos_realSport!=null){
                        try {
                            fos_realSport.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onRealtimePressure(String address, float atmospheric,
                                           float altitude, float ambientTemperature) {
                String sports = "气压: " + atmospheric + "Kpa，海拔：" + altitude + "m , 环境温度：" + ambientTemperature + "°";
                if (mUpdateListListener != null)
                    mUpdateListListener.onRealtimeData(address, sports);
            }

            int tempRealHeart = 0;
            @Override
            public void onRealtimeHearts(String address, int heart) {
                String sports = getString(R.string.heartrate) + ": " + heart;
//                if (mUpdateListListener != null)
//                    mUpdateListListener.onRealtimeData(address, sports);
                if (mActivity.get_heartneed_towrite()) {
                    if (tempRealHeart == 0) {
                        initRealHeartFile();
                        tempRealHeart++;
                    }
                    L.i(TAG, "实时心率数据需要写入文件");
                    saveRealHeartToFile(heart);
                }else{

                    if (mUpdateListListener != null)
                        mUpdateListListener.onRealtimeData(address, sports);
                    L.i(TAG, "实时心率数据不需要写入文件");
                    if(fos_rawHeart!=null){
                        try {
                            fos_rawHeart.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


            @Override
            public void onRecentSleep(String address, int[] startTime,
                                      int[] stopTime, int[] span) {
                String data = getString(R.string.lastday_sleep_status) + "\n " +
                        getString(R.string.start_sleep_time) + startTime[0] + "-" + startTime[1] + "-" + startTime[2] + " " + startTime[3] + ":" + startTime[4] +
                        "\n" + getString(R.string.stop_sleep_time) + stopTime[0] + "-" + stopTime[1] + "-" + stopTime[2] + " " + stopTime[3] + ":" + stopTime[4] +
                        "\n" + getString(R.string.sleep_time) + span[0] + getString(R.string.hour) + span[1] + getString(R.string.minute);
                if (mUpdateListListener != null)
                    mUpdateListListener.onRealtimeData(address, data);
            }

            @Override
            public void onRealLocationAction(String address, int number,
                                             int action) {
                String data = "当前位置动作：" + number + " 基站编号," + action + " 动作编号";
                if (mUpdateListListener != null)
                    mUpdateListListener.onRealtimeData(address, data);
            }

            /**
             * @param address
             * @param electricity  电量数据
             */
            @Override
            public void onRealElectricity(String address, int electricity) {
                String data = "电量：" + electricity;
                if (mUpdateListListener != null)
                    mUpdateListListener.onRealtimeData(address, data);
            }

            /**
             * @param address
             * @param Hearrate         心率
             * @param LBloodPressure   低血压
             * @param HBloodPressure   高血压
             * @param QxygenPercentsge 血氧浓度
             * @param BreateFraquency  呼吸频率
             */
            @Override
            public void onRealAllHealthData(String address, int Hearrate, int LBloodPressure, int HBloodPressure, int QxygenPercentsge, int BreateFraquency) {
                String data = "健康数据返回：" + "心率:" + Hearrate + ",低血压: " + LBloodPressure +
                        ",高血压: " + HBloodPressure + ",血氧浓度:" + QxygenPercentsge +
                        ",呼吸频率:" + BreateFraquency;
                if (mUpdateListListener != null)
                    mUpdateListListener.onRealtimeData(address, data);
                //// TODO: 2018/11/8
            }


            int tempHeart = 0;
            @Override
            public void onRealRawHearrate(String address, byte[] data) {
                final byte[] tempData = new byte[20];
                System.arraycopy(data, 0, tempData, 0, 20);
                int[] a = new int[10] ;
                for (int i = 0; i < 10; i++) {
                    final byte[] by = new byte[4];
                    System.arraycopy(tempData, i * 2, by, 0, 2);
                    //a[i] = Utils.bytesToInt(by,0);
                    a[i] = UtilsTools.bytesToInt(by,0);

                }

                L.i(TAG,"转换后的心率信号:"+Arrays.toString(a));

                final byte[] rate = tempData;
                if (mUpdateListListener != null){
                    if (mActivity.get_rawHeart_towrite()) {//需要写入文件
                        L.i(TAG,"心率裸数据需要写入文件");
                        if (tempHeart == 0) {
                            initRawHeartFile();
                            tempHeart++;
                        }if(fos_rawHeart!=null) {
                            saveRawHeartToFile(data);
                        }
                    } else {
                        L.i(TAG, "不需要写入文件");
                        String d = "心率裸数据返回"+Arrays.toString(data);
                        mUpdateListListener.onRealtimeData(address,d);
                        if(fos_rawHeart!=null){
                            try {
                                fos_rawHeart.close();
                                tempHeart = 0;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }}
                }

                /**
                 * 加速度裸数据返回
                 *
                 * @param address
                 * @param x       x轴
                 * @param y       y轴
                 * @param z       z轴
                 */
                int tempaccelert = 0;
                @Override
                public void onRealRawAcceleration (String address,final int x, final int y, final int z){
                    String data = "加速度裸数据返回：" + "x:" + x + ",y:" + y + ",z:" + z;
                    if (mUpdateListListener != null){
                        if (mActivity.get_accelertneed_towrite()) {//需要写入文件
                            if (tempaccelert == 0) {  //temp用来保证文件只创建了一次
                                initAccelerationFile();
                                tempaccelert++;
                            }
                            L.i(TAG, "加速度裸数据需要写入文件");
                            tep[0] = x;
                            tep[1] = y;
                            tep[2] = z;
                            saveAccelerationToFile(tep);
                        } else {
                            L.i(TAG, "不需要写入文件");
                            mUpdateListListener.onRealtimeData(address, data);
                            if(fos_acceleration!=null){
                                try {
                                    fos_acceleration.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }}
                }

            @Override
            public void onRealRawEulerangles(String s, int i, int i1, int i2, int i3) {

            }

            @Override
            public void onRealRawHearRatePeak(String s, String s1) {

            }

            @Override
            public void onRawHearRatePeakPointer(String s, byte[] bytes) {

            }

        });
        bleDevice.setHistoryDataListener(new HistoryDataListener() {

                @Override
                public void onHistoryTemperature (String address, ArrayList < Long > times, ArrayList < Float > data){
                    String aa = "历史体温数据长度：" + data.size();
                    Log.d("DeveiceFragment",aa);
                    Log.i(TAG,"历史体温数据写入文件");
                    writeHistoryTemperateToFile(times,data);
                    if (mUpdateListListener != null)
                        mUpdateListListener.onRealtimeData(address, aa);
                }
                @Override
                public void onHistorySprots (String address, ArrayList < Long > times, ArrayList < Integer > data){
                    Log.i(TAG,"历史运动数据写入文件");
                    writeHistorySportToFile(times,data);
                    if (mUpdateListListener != null) {
                        mUpdateListListener.onHistoryData(address, 0x35, times, data);
                    }
                }
                @Override
                public void onHistoryHeart (String address, ArrayList < Long > times, ArrayList < Integer > data){
                    Log.i(TAG,"历史心率数据写入文件");
                    writeHistoryHeartToFile(times,data);
                    if (mUpdateListListener != null) {
                        mUpdateListListener.onHistoryData(address, 0x42, times, data);
                    }
                }
                @Override
                public void onHistoryDetailedSleep (String address, ArrayList < Long > times, ArrayList < Integer > data){
                    if (mUpdateListListener != null) {
                        mUpdateListListener.onHistoryData(address, 0x34, times, data);
                    }
                }
                @Override
                public void onHistoryTourniquet (String address,
                        ArrayList < Long > times, ArrayList < Integer[]>data){

                    if (mUpdateListListener != null) {
                        mUpdateListListener.onHistoryDosageData(address, 0x59, times, data);
                    }
                }
                @Override
                public void onHistoryLocationAction (String address,
                        ArrayList < Long > times, ArrayList < Integer[]>data){
                    if (mUpdateListListener != null) {
                        mUpdateListListener.onHistoryDosageData(address, 0x3B, times, data);
                    }
                }
            });
        bleDevice.setOtherDataListener(new OtherDataListener() {
            @Override
            public void onFunction (String address,int oxygen, int blood, int temperature, int heart, int sleep, int step){
                String sports = "oxygen: " + oxygen + " ,blood：" + blood + ",temperature：" + temperature + ",heart:" + heart + ",sleep:" + sleep + ",step:" + step;
                if (mUpdateListListener != null)
                    mUpdateListListener.onRealtimeData(address, sports);
                }

                @Override
                public void onAlarmList (String address, ArrayList <int[]>alarm){
                    // TODO Auto-generated method stub
                    if (mUpdateListListener != null)
                        mUpdateListListener.onAlarm(address, alarm);
                }

                @Override
                public void onLogin (String address,boolean success){
                    String st;
                    if (success) {
                        st = getString(R.string.login_successful) + "";
                    } else {
                        st = "登录失败";
                    }
                    if (mUpdateListListener != null)
                        mUpdateListListener.onRealtimeData(address, st);
                }

                @Override
                public void onbound (String address,boolean success){
                    String st;
                    if (success) {
                        st = getString(R.string.bindig_Success) + "";
                    } else {
                        st = "绑定失败";
                    }
                    if (mUpdateListListener != null)
                        mUpdateListListener.onRealtimeData(address, st);
                }

                @Override
                public void onFall (String address,int degree){
                    String st = getString(R.string.fall) + ": " + degree;
                    if (mUpdateListListener != null)
                        mUpdateListListener.onRealtimeData(address, st);
                }

                @Override
                public void onDelbound (String address,boolean success){
                    String st;
                    if (success) {
                        st = getString(R.string.delete_binding_success) + "";
                    } else {
                        st = "删除绑定失败";
                    }
                    if (mUpdateListListener != null)
                        mUpdateListListener.onRealtimeData(address, st);

                }

                @Override
                public void onSendImageAndFontsResult (String address,int cmd,
                int progress, int group){
                    String data = "cmd：0x" + Integer.toHexString(cmd) + "， 发送结束：" + progress + " ,组编号" + group;
                    if (mUpdateListListener != null)
                        mUpdateListListener.onRealtimeData(address, data);
                }
            });

        }


    public void saveAccelerationToFile(int[] by) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        String data = str + " " + Arrays.toString(by);
        L.i(TAG, "开始写入加速度数据." + Arrays.toString(by));
        if (getfile()) {//如果外部存储可用
            //写数据
            try {
                if (fos_acceleration != null) {
                    fos_acceleration.write(data.getBytes());
                    fos_acceleration.write("\r\n".toString().getBytes());
                    if (!bleDevice.isConnected()) {
                        fos_acceleration.close();
                    }
                } else {
                    L.i(TAG, "fos_acceleration ==null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Looper.prepare();
            Toast.makeText(mActivity, ": 外部存储卡不可用", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    public void saveRawHeartToFile(byte[] rate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        String data = str + " " + Arrays.toString(rate);
        L.i(TAG, "开始写入实时心率裸数据." +data);
        if (getfile()) {//如果外部存储可用
            //写数据
            try {
                if (fos_rawHeart != null) {
                    fos_rawHeart.write(data.getBytes());
                    fos_rawHeart.write("\r\n".toString().getBytes());
                    if (!bleDevice.isConnected()) {
                        fos_rawHeart.close();
                    }
                } else {
                    L.i(TAG, "fos_rawHeart ==null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Looper.prepare();
            Toast.makeText(mActivity, ": 外部存储卡不可用", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    public void saveRealHeartToFile(int rate){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        String data = str + " " + rate;
        L.i(TAG, "开始写入实时心率裸数据." +data);
        if (getfile()) {//如果外部存储可用
            //写数据
            try {
                if (fos_realHeart != null) {
                    fos_realHeart.write(data.getBytes());
                    fos_realHeart.write("\r\n".toString().getBytes());
                    if (!bleDevice.isConnected()) {
                        fos_realHeart.close();
                    }
                } else {
                    L.i(TAG, "fos ==null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Looper.prepare();
            Toast.makeText(mActivity, ": 外部存储卡不可用", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    public void saveHistoryHeartToFile(String time,Integer rate){

        String data = time + " " + rate;
        L.i(TAG, "开始写入历史心率数据." +data);
        if (getfile()) {//如果外部存储可用
            //写数据
            try {
                if (fos_historyHeart != null) {
                    fos_historyHeart.write(data.getBytes());
                    fos_historyHeart.write("\r\n".toString().getBytes());
                    if (!bleDevice.isConnected()) {
                        fos_historyHeart.close();
                    }
                } else {
                    L.i(TAG, "fos_historyHeart ==null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Looper.prepare();
            Toast.makeText(mActivity, ": 外部存储卡不可用", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    public void writeHistoryHeartToFile(ArrayList < Long > times,ArrayList < Integer > data){
        int m;
        if(data.size()>times.size()){
            m = times.size();
        }else{
            m = data.size();
        }
        initHistoryHeartFile();
        for(int i = 0;i< m;i++){
            String time = stampToDate(times.get(i));
            saveHistoryHeartToFile(time,data.get(i));
        }
        try {  //写入完成关闭文件流
            fos_historyHeart.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveRealTemperateToFle(String value){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String time = formatter.format(curDate);
        if (getfile()) {//如果外部存储可用
            //写数据
            try {
                fos_realTemperate.write(time.getBytes());
                fos_realTemperate.write(("    "+value).getBytes());
                fos_realTemperate.write("\n".getBytes());
                Log.i(TAG,"体温数据"+time+" "+value+"写入成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Looper.prepare();
            Toast.makeText(mActivity, ": 外部存储卡不可用", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    public void saveHistoryTemperateToFle(String time,float value){
        if (getfile()) {//如果外部存储可用
            //写数据
            try {
                fos_historyTemperate.write(time.getBytes());
                fos_historyTemperate.write(("    "+value).getBytes());
                fos_historyTemperate.write("\n".getBytes());
                Log.i(TAG,"体温数据"+time+" "+value+"写入成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Looper.prepare();
            Toast.makeText(mActivity, ": 外部存储卡不可用", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    public void writeHistoryTemperateToFile(ArrayList < Long > times,ArrayList < Float > data){
        int m;
        if(data.size()>times.size()){
            m = times.size();
        }else{
            m = data.size();
        }
       initHistoryTemperateFile();
        for(int i = 0;i< m;i++){
            String time = stampToDate(times.get(i));
            saveHistoryTemperateToFle(time,data.get(i));
        }
        try {  //写入完成关闭文件流
            fos_historyTemperate.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveRealSportToFle(String value){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String time = formatter.format(curDate);
        if (getfile()) {//如果外部存储可用
            //写数据
            try {
                fos_realSport.write(time.getBytes());
                fos_realSport.write(("    "+value).getBytes());
                fos_realSport.write("\n".getBytes());
                Log.i(TAG,"实时运动数据"+time+" "+value+"写入成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Looper.prepare();
            Toast.makeText(mActivity, ": 外部存储卡不可用", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    public void saveHistorySportToFle(String time,float value){
        if (getfile()) {//如果外部存储可用
            //写数据
            try {
                fos_historySport.write(time.getBytes());
                fos_historySport.write(("    "+value).getBytes());
                fos_historySport.write("\n".getBytes());
                Log.i(TAG,"历史运动"+time+" "+value+"写入成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Looper.prepare();
            Toast.makeText(mActivity, ": 外部存储卡不可用", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    public void writeHistorySportToFile(ArrayList < Long > times,ArrayList < Integer > data){
        int m;
        if(data.size()>times.size()){
            m = times.size();
        }else{
            m = data.size();
        }
        initHistorySportFile();
        for(int i = 0;i< m;i++){
            String time = stampToDate(times.get(i));
            saveHistorySportToFle(time,data.get(i));
        }
        try {  //写入完成关闭文件流
            fos_historySport.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断外部存储是否可用
     * @return
     */
    public boolean getfile() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//如果外部存储可用
            return true;
        } else {
            return false;
        }
    }

    /**
     *设置设备接口
     */
    private void setBLEDeviceListener() {
        bleDevice.setBLEDeviceListener(new BLEDeviceListener() {
            @Override
            public void onDisConnected(String address) {
                if (mUpdateChartsListener != null)
                    mUpdateChartsListener.onDisConnected(address);
                if (mUpdateListListener != null)
                    mUpdateListListener.onDisConnected(address);
            }

            @Override
            public void onConnected(String address) {
                if (mUpdateChartsListener != null)
                    mUpdateChartsListener.onConnected(address);
                if (mUpdateListListener != null)
                    mUpdateListListener.onConnected(address);
            }

            @Override
            public void updateRssi(String address, int rssi) {
                if (mUpdateListListener != null)
                    mUpdateListListener.updateRssi(address, rssi);

            }
        });
        /**DeviceMessageListener接口和 HistoryDataListener，RealtimeDataListener ，setOtherDataListener 这三个接口功能重复了
         *
         * DeviceMessageListener该接口返回的是未解析的数据
         *
         * HistoryDataListener，RealtimeDataListener ，setOtherDataListener 这三个接口返回的是已经解析的数据
         *
         *只要用其中一种就可以了，不用两个都用
         * */
        bleDevice.setDeviceMessageListener(new DeviceMessageListener() {


            @Override
            public void onSendResult(String address, int cmd, byte[] data) {
                Log.i("DYKDeviceFragment", address + Arrays.toString(data));
                //接收设备返回未解析的数据信息
                if (mUpdateChartsListener != null)
                    mUpdateChartsListener.onSendResult(address, cmd, data);

            }

            @Override
            public void onSendHistory(String address, int cmd, List<byte[]> historyData) {
                //接收设备返回未解析的历史数据信息
				if(mUpdateListListener !=null)
					mUpdateListListener.onSendHistory(address, cmd, historyData);
                for(int i = 0;i < historyData.size();i++) {
                    Log.i("未解析的历史数据:",Arrays.toString(historyData.get(i)) );
                }
            }
        });
    }


}
