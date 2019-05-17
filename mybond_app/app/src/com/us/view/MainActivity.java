package com.us.view;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.us.Utils.UtilsTools;
import com.us.adaptr.CustomViewPager;
import com.us.adaptr.PerAdapter;
import com.us.adaptr.ShowBLEAdapter;
import com.us.fragment.DeviceFragment;
import com.us.module.R;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.us.Utils.Utils;
import com.us.ble.central.BLEDevice;
import com.us.ble.central.BLEManager;
import com.us.ble.central.L;
import com.us.ble.listener.ScanListener;

public class MainActivity extends FragmentActivity {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 0;
    private BluetoothManager myBluetoothManager;
    private BluetoothAdapter myBluetoothAdapter;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1001;
    private static final String TAG = "MainActicity";
    private PagerTabStrip pagerTabStrip;
    private CustomViewPager per_viewPager;
    private PerAdapter perAdater;
    private BLEManager mBleManager;
    private ListView lv;
    private TextView tv_hint;
    private ProgressBar pbar;
    private ShowBLEAdapter mViewAdapter;
    private MenuItem itemSacn;

    private boolean scaning = false; // 是否正在扫描
    private List<String> addressView;// 每个设备Key
    private BLEDevice dBleDevice; // 当前正在操作的设备
    private String dAddress; // 当前正在操作当前的设备地址
    private ArrayList<BLEDevice> mBLEList;

    public boolean accelerToFile = false;
    public boolean heartToFile = false;
    public boolean temprateToFile = false;
    public boolean rawHeartToFile = false;
    public boolean realSportToFile = false;
    /**
     * key is the MAC Address 多设备 每一个BLEDevice实例代表一个设备
     * 把所有的设备即BLEDevice实例放到一个集合里面，通过address 来获得对应的设备，做相应的操作
     */
    Map<String, BLEDevice> mBLEDevices = new LinkedHashMap<String, BLEDevice>();
    private List<DeviceFragment> mFragments = new ArrayList<DeviceFragment>();
    Map<String, Handler> mHandlers = new LinkedHashMap<String, Handler>();
    private Map<String, Integer> rssiMap = new LinkedHashMap<String, Integer>();
    private Map<String, String> uuidMap = new LinkedHashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.peripheral_viewpager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        //获取系统的蓝牙服务
        myBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        myBluetoothAdapter = myBluetoothManager.getAdapter();  //蓝牙适配器
        intit();//初始化：
        intiView();//初始化页面
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success
                    System.out.println("蓝牙权限获取成功");
                }
                break;
        }
    }


    /**
     * 初始化，扫描蓝牙，接收回调
     */
    private void intit() {
        L.isDebug = true; // 打印设备连接，写入，接收数据的信息
        mBleManager = new BLEManager(this);
        mBleManager.setScanListener(new ScanListener() { // 扫描回调监听器
            /**
             *
             * @param result 扫描状态，0表示正在扫描
             * @param bleDevice  扫描到的蓝牙设备
             * @param rssi 信号强度
             * @param scanRecord  扫描记录扫秒到的蓝牙数据
             * @param deviceUUID  设备uuid
             */
            @Override
            public void onScanResult(final int result,
                                     final BLEDevice bleDevice, final int rssi,
                                     final byte[] scanRecord, final String deviceUUID) {
                runOnUiThread(new Runnable() {  //在线程中执行UI更新操作
                    public void run() {
                        if (result == 0) { // 正在扫描
                            String a = "";
                            System.out.println("scanRecord的数据长度 : " + scanRecord.length);
                            a = Utils.bytes2hex03(scanRecord);
                            System.out.println("转换后a的值 :"  + a );
                            int pack = (scanRecord[14] & 0xff);
                            System.out.println("格式类型 : " + pack);
                            int a1 = (scanRecord[15] & 0xff) >> 7;
                            System.out.println("电量更新 : " + a1);
                            int b = (scanRecord[15] & 0xff) & 0b01111111;
                            System.out.println("电量数据 : " + b);
                            int heart = (scanRecord[16] & 0xff); // 心率数据
                            System.out.println("心率数据 : " + heart);
                            int rssi = (scanRecord[17] & 0xff);
                            System.out.println("信号强度 : " + rssi);
                            int step = (( (scanRecord[19] & 0xff) & 0b01111111) << 10) | (((scanRecord[20] & 0xff)) << 2)
                                    | ((scanRecord[21] & 0xff) >> 6); // 运动步数
                            System.out.println("运动步数为："+step);
                            scaning = true;  //正在扫描
                            System.out.println("找到了device:"
                                    + bleDevice.getName() + " Rssi : "
                                    + rssi + "" + "Address : "
                                    + bleDevice.getAddress() + ",uuid:" + deviceUUID);
                            if (bleDevice == null || bleDevice.getName() == null) {
                                return;
                            }
                            //扫描到的设备添加到list列表中
                            if (mBLEList.size() == 0) {
                                mBLEList.add(bleDevice);
                                rssiMap.put(bleDevice.getAddress(), rssi);
                                uuidMap.put(bleDevice.getAddress(), deviceUUID);
                            }

                            for (int i = 0; i < mBLEList.size(); i++) {
                                if ((mBLEList.get(i).getAddress()).equals(bleDevice.getAddress())) {
                                    break;
                                } else if (i == mBLEList.size() - 1) {  //如果已经遍历到集合的最后一个，还是不在集合中，则添加到集合中
                                    if (!(mBLEList.get(i).getAddress()).equals(bleDevice.getAddress())) {
                                    }
                                    mBLEList.add(bleDevice);
                                    rssiMap.put(bleDevice.getAddress(), rssi);
                                    uuidMap.put(bleDevice.getAddress(), deviceUUID);
                                }
                            }
                            //显示扫描到的设备信息
                            lv.setVisibility(View.VISIBLE);
                            mViewAdapter.notifyDataSetChanged();
                        } else {
                            // 扫描结束
                            scaning = false;
                            itemSacn.setTitle("scan");
                            if (mBLEList.size() <= 0) {
                                tv_hint.setText(getString(R.string.device_no)+"");
                                tv_hint.setVisibility(View.VISIBLE);
                            } else {
                                tv_hint.setVisibility(View.GONE);
                            }
                            pbar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        addressView = new ArrayList<String>();
        addressView.clear();
    }


    /**
     * 初始化视图
     */
    @SuppressWarnings("deprecation")
    private void intiView() {
        pagerTabStrip = (PagerTabStrip) findViewById(R.id.per_pagertab);
        pagerTabStrip.setTabIndicatorColorResource(android.R.color.holo_blue_light);
        per_viewPager = (CustomViewPager) findViewById(R.id.per_viewPager);
        per_viewPager.setOffscreenPageLimit(4);
        perAdater = new PerAdapter(getSupportFragmentManager(), mFragments);
        per_viewPager.setAdapter(perAdater);
        per_viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                dAddress = addressView.get(arg0);
                if (mBLEDevices.get(dAddress) != null) {
                    dBleDevice = mBLEDevices.get(dAddress);
                    L.i(TAG, "当前设备：" + dBleDevice.getAddress());
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

    }

    /**
     * 开启蓝牙，初始化蓝牙搜索对话框
     */
    @Override
    protected void onStart() {
        super.onStart();

        /**
         * 判断蓝牙是否开启
         */
        if (myBluetoothAdapter.isEnabled()) {
            System.out.println("蓝牙已开启...");
        } else {
            enableBle();
        }
        initScanDialog();
    }

    /**
     * 开启蓝牙
     */
    public void enableBle() {
        if (!myBluetoothAdapter.enable()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    /**
     * 扫描设备
     */
    private void scanLeDevice() {
        itemSacn.setTitle("stop scan");
        if (mBLEList != null) {
            mBLEList.clear();
        }
        // scaning = true;
        if (scaning)
            stopScan();
        mBleManager.startScan(5);// 扫描5秒
    }

    /**
     * 停止扫描
     */
    private void stopScan() {
        scaning = false;
        mBleManager.stopScan();
    }

    /**
     * 初始化对话框
     */
    private void initDialog() {
        initSettingDialog();
        initSecurityDialog();
        initSportsDialog();
        initHealthDialog();
        initTextDialog();
        initHeartDialog();
        initrawdataDialog();
        initAuthDialog();
        initAuthResultDialog();
    }

    /**
     * 将每次连接的一个设备添加在设备列表中
     * @param device 要连接的设备
     */
    private void addDevice(final BLEDevice device) {
        final String address = device.getAddress();
        byte[] scanRecord = null; // 扫描获取的
        int rssi = 0; // 扫秒是获取的信号
        if (!mBLEDevices.containsKey(address)) {
            //如果扫描的设备中不包含所需要连接的设备，将其添加到列表中
            mBLEDevices.put(device.getAddress(), device);
            if (addressView.size() == 0) {
                dBleDevice = mBLEDevices.get(address);
                dAddress = dBleDevice.getAddress();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addDeviceFragment(device.getAddress(), device);
                    initDialog();
                }
            });
            addressView.add(device.getAddress());
        }
        Toast.makeText(this, getString(R.string.connect_device)+"", Toast.LENGTH_LONG).show();
        per_viewPager.setCurrentItem(addressView.size());
         updateList(address,"开始连接设备..");
        new Thread(new Runnable() {
            @Override
            public void run() {
                connect(address); // 不要同时连几个蓝牙设备，要等连接成功后再连接下一个
            }
        }).start();

    }

    /**
     * 连接设备
     * @param address mac地址
     */
    protected void connect(String address) {
        if (scaning)
            stopScan(); // 先判断是否正在扫描
        dBleDevice = mBLEDevices.get(address);
        dBleDevice.connect();
    }

    /**
     * 每连接一个设备，添加一个fragment
     * @param st
     * @param device
     */
    private void addDeviceFragment(String st, BLEDevice device) {
        DeviceFragment mFragment = new DeviceFragment(device);
        mFragments.add(mFragment);
        perAdater.setListViews(mFragments, st);
        perAdater.notifyDataSetChanged();

    }

    private AlertDialog scanDialog;  //蓝牙扫描对话框

    /**
     * 初始化蓝牙扫描对话框
     */
    private void initScanDialog() {
        AlertDialog.Builder scanBuilder = new AlertDialog.Builder(this);
        scanBuilder.setTitle(getString(R.string.search_device)+"");
        View view = getLayoutInflater().inflate(R.layout.device_list, null);
        mBLEList = new ArrayList<>();
        lv = (ListView) view.findViewById(R.id.device_list);
        tv_hint = (TextView) view.findViewById(R.id.tv);
        pbar = (ProgressBar) view.findViewById(R.id.pbar);
        pbar.setVisibility(View.VISIBLE);
        mViewAdapter = new ShowBLEAdapter(this, mBLEList, rssiMap, uuidMap);
        lv.setAdapter(mViewAdapter);
        scanBuilder.setView(view);
        scanBuilder.setPositiveButton(getString(R.string.retry)+"",
                new DialogInterface.OnClickListener() {
                    // 重试按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {// 下面三句控制弹框的关闭
                            // stopScan();
                            Field field = dialog.getClass().getSuperclass()
                                    .getDeclaredField("mShowing");

                            field.setAccessible(true);

                            field.set(dialog, false);// true表示要关闭

                        } catch (Exception e) {

                            e.printStackTrace();

                        }
                        // reScanLeDevice(true);
                        // actionAlertDialog();
                        // lv.setVisibility(View.GONE);
                        tv_hint.setVisibility(View.GONE);
                        scanLeDevice();
                        // scanDialog.show();
                        pbar.setVisibility(View.VISIBLE);
                    }
                });
        scanBuilder.setNegativeButton(getString(R.string.cancel)+"",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 取消查找设备的操作
                        stopScan();
                        System.out.println("取消查找");
                        try {// 下面三句控制弹框的关闭

                            Field field = dialog.getClass().getSuperclass()
                                    .getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);// true表示要关闭
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        scanDialog.cancel();
                        // scanDialog.dismiss();
                    }
                });

        scanDialog = scanBuilder.create();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                stopScan();
                try {// 下面三句控制弹框的关闭
                    Field field = scanDialog.getClass().getSuperclass()
                            .getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(scanDialog, true);// true表示要关闭
                } catch (Exception e) {
                    e.printStackTrace();
                }
                BLEDevice device = mBLEList.get(position);
                addDevice(device);
                scanDialog.dismiss();
                scanDialog.cancel();

            }
        });
    }

    private AlertDialog settingDialog;

    /**
     * 设置对话框
     */
    private void initSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.main_setting)+"");
        final String[] commants = {
                getString(R.string.set_time)+"",
                getString(R.string.restore_factory)+"",
                "定时心率测量设置(*) ",
                "设备名设置",
                "广播设置"
        };
        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("dBleDevice:" + dBleDevice.getAddress());
                if (dBleDevice == null) {
                }
                switch (which) {
                    case 0:     //时间设置
                        byte[] time = UtilsTools.nowTimeToBytes();  //读取手机中的系统时间
                        updateList(dAddress, "cmd:0x01," + commants[0] + " :"
                                + Arrays.toString(UtilsTools.byteTo16String(time)));
                        write(dAddress, time.length, 0x01, time);
                        break;
                    case 1:    //出厂设置
                        byte[] r = {0x00};
                        updateList(dAddress, "cmd:0x09," + commants[2] + " :"
                                + Arrays.toString(UtilsTools.byteTo16String(r)));
                        write(dAddress, r.length, 0x09, r);
                        break;
                    case 2: // 定时心率测量
                        // 定时设置的值 0或者 15，30，60。0 代表定时测量心率关闭，15 代表每 15 分钟定时测量一次心率，30
                        // 代表每隔 30 分钟定时测量一次性率，60 代表每隔 60 分钟定时测量一次心率。
                        heartDialog.show();
                        break;
                    case 3: //	"设备名设置"
                        String subtitle2 = "默认是 B2，长度不超过 2 个字节";
                        settingDialog(0x0D, commants[3], subtitle2);
                        break;
                    case 4: //	"广播设置"
                        String subtitle3 = "设置范围为 32 到 16384，" +
                                "真正的时间要用设置的间隔×0.625 才可以达到，" +
                                "比如设置成 8000，实际代表 5000 毫秒（8000×0.625）发射一次广播。";
                        settingDialog(0x0E, commants[4], subtitle3);
                        break;
                }
            }
        });

        settingDialog = builder.create();
    }

    /**
     * 设置对话框
     * @param cmd  命令
     * @param title
     * @param subtitle
     */
    private void settingDialog(final int cmd, final String title, String subtitle) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.ed_layout, null);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        TextView subtitleText = (TextView) view.findViewById(R.id.subtitle_text);
        subtitleText.setText(subtitle);
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String s = editText.getText().toString();
                int a = 0;
                if ("".equals(s)) {
                    return;
                }
                L.i(title + ":----------" + s + " --" + cmd);
                switch (cmd) {
                    case 0x0C://	"功率设置"
                        a = Integer.parseInt(s);
                        byte[] dd = {(byte) a};
                        updateList(dAddress, "cmd:0x0C, " + title + ":" + a + " , " + Arrays.toString(UtilsTools.byteTo16String(dd)));
                        write(dAddress, dd.length, cmd, dd);
                        break;
                    case 0x0D://	"设备名设置"
                        byte[] t = s.getBytes();
                        updateList(dAddress, "cmd:0x0D, " + title + ":" + s + " , " + Arrays.toString(UtilsTools.byteTo16String(t)));
                        write(dAddress, t.length, cmd, t);
                        break;
                    case 0x0E://	"广播设置"
                        a = Integer.parseInt(s);
                        float xx = a * 0.625f;
                        byte[] gb = UtilsTools.intToByteArray(a);
                        updateList(dAddress, "cmd:0x0E, " + title + "毫秒:" + a + "*0.625 =" + xx + " , " + Arrays.toString(UtilsTools.byteTo16String(gb)));
                        write(dAddress, gb.length, cmd, gb);
                        break;
                    case 0x62://	"定时温度测量设置"
                        a = Integer.parseInt(s);
                        byte[] ss = {(byte) a};
                        updateList(dAddress, "cmd:0x62, " + " , " + Arrays.toString(UtilsTools.byteTo16String(ss)));
                        write(dAddress, ss.length, cmd, ss);
                        break;
                    case 0x64://	"跌倒灵敏度设置"
                        a = Integer.parseInt(s);
                        byte[] ssselect = {(byte) a};
                        Log.i("TAG", "ssselect:" + a);
                        updateList(dAddress, "cmd:0x64, " + "跌倒灵敏度设置 , " + Arrays.toString(UtilsTools.byteTo16String(ssselect)));
                        write(dAddress, ssselect.length, cmd, ssselect);
                        break;
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        builder.show();
    }


    //定时测量心率dialog
    private AlertDialog heartDialog;

    private void initHeartDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.ed_layout, null);
        final EditText editText2 = (EditText) view.findViewById(R.id.editText);
        builder.setTitle("请输入0~60分钟数");
        builder.setView(view);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String s = editText2.getText().toString();
                int a = Integer.parseInt(s);
                L.i("定时测量心率:----------" + s + " --" + a);

                byte[] data = {(byte) a};
                updateList(dAddress, "cmd:0x08, 每隔 " + a + " 分钟定时测量一次心率, " + Arrays.toString(UtilsTools.byteTo16String(data)));
                write(dAddress, data.length, 0x08, data);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        heartDialog = builder.create();
    }


    private void write(String address, int length, int cmd, byte[] data) {
        BLEDevice bleDevice = mBLEDevices.get(address);
        bleDevice.write(length, cmd, data);
    }

    private void write_nohead(String address, int length, int cmd, byte[] data) {
        BLEDevice bleDevice = mBLEDevices.get(address);
        bleDevice.write_nohead(length, cmd, data);
    }

    private void write_custom(String address, int length, int cmd,int sid, byte[] data) {
        BLEDevice bleDevice = mBLEDevices.get(address);
        bleDevice.write_custon(length, cmd, sid,data);
    }


    private AlertDialog textDialog;

    private void initTextDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.ed_layout, null);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        builder.setTitle("请输入不超过12个汉字的内容");
        builder.setView(view);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String s = editText.getText().toString();
                L.i("string:----------" + s + "--");
                L.i("unicode:----------" + UtilsTools.string2Unicode(s) + "--");
                try {
//					byte [] t = s.getBytes("utf-16"); //因为用utf-16 和 Unicode 编码差不多，所以用两个都可以
                    byte[] t = s.getBytes("Unicode");

                    //要去掉Unicode标识头在发送
                    byte[] data = new byte[16]; //不能超过16个字节
                    byte[] data2 = new byte[16]; //不能超过16个字节
                    if(t.length <=18){
                        System.arraycopy(t, 2, data, 0, t.length - 2);
                    }else if(t.length >18 && t.length<=26){
                        System.arraycopy(t, 2, data, 0, 16);
                        System.arraycopy(t, 18, data2, 0, t.length-18);
                    }else if(t.length>26){
                        System.arraycopy(t, 2, data, 0, 16);
                        System.arraycopy(t, 18, data2, 0, 8);
                    }
                    updateList(dAddress, "cmd:0x19, 发送内容：" + s + "；  Unicode编码 :" + UtilsTools.string2Unicode(s) + " ,第一个包:" +
                            Arrays.toString(UtilsTools.byteTo16String(data)));
                    write_custom(dAddress, data.length, 0x19,0, data);
                    updateList(dAddress, "cmd:0x19, 第二个包：" + Arrays.toString(UtilsTools.byteTo16String(data2)));
                    write_custom(dAddress, data2.length, 0x19,1, data2);

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        textDialog = builder.create();
    }

    private AlertDialog securityDialog;

    private void initSecurityDialog() {
        // 因为直接把Mac地址转换成byte[]之后的长度是 17 会超出长度，所以要去掉“ ：”符号，他的长度变成
        // 12,发送的时候要发的长度是16。
        final byte[] bluAddr = BluetoothAdapter.getDefaultAdapter()
                .getAddress().replace(":", "").getBytes();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.main_security)+"");
        final String[] commants = {
                getString(R.string.phone_to_disconnect)+"",
                getString(R.string.super_connect)+"",
        };
        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:    //手机请求删除绑定(默认)
                        updateList(dAddress, "cmd:0x22," + commants[0] + " :"
                                + Arrays.toString(UtilsTools.byteTo16String(bluAddr)));
                        write(dAddress, 16, 0x22, bluAddr);
                        break;

                    case 1:    //超级登录(超级登录)
                        byte[] SUPER_BOUND_DATA = {0x01, 0x23, 0x45, 0X67,
                                (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF,
                                (byte) 0xFE, (byte) 0xDC, (byte) 0xBA, (byte) 0x98,
                                0x76, 0x54, 0x32, 0x10};
                        updateList(
                                dAddress,
                                "cmd:0x24,"
                                        + commants[1]
                                        + " :"
                                        + Arrays.toString(UtilsTools
                                        .byteTo16String(SUPER_BOUND_DATA)));
                        write(dAddress, SUPER_BOUND_DATA.length, 0x24,
                                SUPER_BOUND_DATA);
                        break;


                }
            }
        });
        securityDialog = builder.create();
    }

    private AlertDialog sportsDialog;

    private void initSportsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.main_sports)+"");
        final String[] commants = {
                getString(R.string.real_sports)+"",
                getString(R.string.history_sports)+"",
                getString(R.string.move_history_data_point),
                getString(R.string.request_real_sports_tofile)
        };

        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // System.out.println("dBleDevice:" + dBleDevice.getAddres());
                switch (which) {
                    case 0:  //请求实时运动数据
                        // 0x00 关闭数据实时同步，0x01 打开数据实时同步
                        byte[] s = {0x01};
                        updateList(dAddress, "cmd:0x31," + commants[0] + " :"
                                + Arrays.toString(s));
                        write(dAddress, s.length, 0x31, s);
                        break;
                    case 1:    //请求历史运动数据并写入文件(这个已经包含有简单的历史睡眠时间了)
                        byte[] sh = {0x01};
                        updateList(dAddress, "cmd:0x35," + commants[1] + " :"
                                + Arrays.toString(sh) + ","+getString(R.string.wait));
                        write(dAddress, sh.length, 0x35, sh);
                        break;
                    case 2: // 请求挪动历史步数数据指针

                        // 为了避免每次同步时间过长，增加请求挪动历史运动数据指针的命令，APP 传送一个时间点过来
                        // 如果发送了一个全 0 的四个数据过来，则代表将指针重置到起始位置。
                        byte[] aa = UtilsTools.record_date(2018, 11, 1, 0); // 年，月，日，时
                        updateList(dAddress, "cmd:0x32," + commants[2] + " :"
                                + Arrays.toString(UtilsTools.byteTo16String(aa))
                                + "，挪动时间：" + "2018-11-1 00:00");
                        write(dAddress, aa.length, 0x32, aa);
                        break;
                    case 3:  //请求实时运动数据并写入文件
                        // 0x00 关闭数据实时同步，0x01 打开数据实时同步
                        byte[] ss = {0x01};
                        updateList(dAddress, "cmd:0x31," + commants[3] + " :"
                                + Arrays.toString(ss));
                        realSportToFile = true;
                        write(dAddress, ss.length, 0x31, ss);
                        break;
                }
            }
        });
        sportsDialog = builder.create();
    }

    private AlertDialog healthDialog;

    private void initHealthDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.main_health)+"");
        final String[] commants = {
                getString(R.string.request_real_heartrate)+"",
                getString(R.string.request_history_heartrate)+"",
                getString(R.string.request_real_temperature)+"",
                getString(R.string.request_history_temperature)+"",
                getString(R.string.request_move_history_heartrate)+"",
                getString(R.string.request_move_history_temperature)+"",
                getString(R.string.close_real_heartrate)+"",
                getString(R.string.request_allhealth_data)+"",
                getString(R.string.close_real_temperature)+"",
                getString(R.string.request_real_heartrate_to_file)+"",
                getString(R.string.request_real_temperature_to_file)
    };
        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("dBleDevice:" + dBleDevice.getAddress());
                switch (which) {
                    case 0:  //请求实时心率数据
                        // 0x00 关闭数据实时同步，0x01 打开数据实时同步
                        byte[] dx = {0x01};
                        updateList(dAddress, "cmd:0x41," + commants[0] + " :"
                                + Arrays.toString(dx));
                        heartToFile = false;
                        write(dAddress, dx.length, 0x41, dx);
                        break;
                    case 1:    //请求历史心率数据并写入文件
                        byte[] hh = {0x01};
                        updateList(dAddress, "cmd:0x43," + commants[1] + " :"
                                + Arrays.toString(hh) + ",请等待，历史数据在后台请求");
                        write(dAddress, hh.length, 0x43, hh);
                        break;
                    case 2:    //请求实时体温数据
                        byte[] t = {0x01};
                        updateList(dAddress, "cmd:0x44," + commants[2] + " :"
                                + Arrays.toString(t));
                        temprateToFile = false;
                        write(dAddress, t.length, 0x44, t);
                        break;
                    case 3:    //请求历史温度数据并写入文件
                        byte[] th = {0x01};
                        updateList(dAddress, "cmd:0x46," + commants[3] + " :"
                                + Arrays.toString(th) + ",请等待，历史数据在后台请求");
                        write(dAddress, th.length, 0x46, th);
                        break;
                    case 4:
                        // 请求挪动历史心率数据指针（4.23日之后开始采集数据）
                        byte[] ah = UtilsTools.record_date(2018, 11, 1, 0);
                        updateList(dAddress, "cmd:0x49," + commants[4] + " :"
                                + "2018-11-1 00:00");
                        write(dAddress, ah.length, 0x49, ah);
                        break;
                    case 5:
                        // 请求挪动历史体温数据指针
                        byte[] at = UtilsTools.record_date(2018, 11, 1, 0);
                        updateList(dAddress, "cmd:0x4A," + commants[5] + " :"
                                + "2018-11-1 00:00");
                        write(dAddress, at.length, 0x4A, at);
                        break;
                    case 6: // 关闭实时心率数据同步
                        byte[] dx0 = {0x00};
                        updateList(dAddress, "cmd:0x41," + commants[6] + " :"
                                + Arrays.toString(dx0));
                        heartToFile = false;
                        write(dAddress, dx0.length, 0x41, dx0);
                        break;
                    case 7: // 请求全部健康数据
                        Log.i("MainActivity", "请求全部健康数据");
                        byte[] dx03 = {0x00};
                        updateList(dAddress, "cmd:0x4B," + commants[7] + " :"
                                + Arrays.toString(dx03));
                        write(dAddress, dx03.length, 0x4B, dx03);
                        break;
                    case 8: // 关闭实时体温数据
                        Log.i("MainActivity", "关闭实时体温数据");
                        byte[] offtem = {0x00};
                        updateList(dAddress, "cmd:0x44," + commants[8] + " :"
                                + Arrays.toString(offtem));
                        temprateToFile = false;
                        write(dAddress, offtem.length, 0x44, offtem);
                        break;
                    case 9: //请求实时心率数据并写入文件
                        byte[] dxl = {0x01};
                        Log.i("MainActivity","请求实时心率数据并且如文件");
                        updateList(dAddress, "cmd:0x41," + commants[9] + " :"
                                + Arrays.toString(dxl));
                        heartToFile = true;
                        write(dAddress, dxl.length, 0x41, dxl);
                        break;
                    case 10: //请求实时体温数据并写入文件
                        byte[] dt = {0x01};
                        Log.i("MainActivity","请求实时体温数据并且如文件");
                        updateList(dAddress, "cmd:0x44," + commants[10] + " :"
                                + Arrays.toString(dt));
                        temprateToFile = true;
                        write(dAddress, dt.length, 0x44, dt);
                        break;

                }
            }
        });
        healthDialog = builder.create();
    }


    private AlertDialog rawdataDialog;

    private void initrawdataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("裸数据请求相关命令");
        final String[] commants = {
                "请求加速度裸数据",
                "请求心率裸数据",
                "请求关闭加速度裸数据实时同步",
                "请求关闭心率裸数据实时同步",
                "请求加速度裸数据写入文件",
                "请求心率裸数据写入文件"
        };
        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // System.out.println("dBleDevice:" + dBleDevice.getAddres());
                switch (which) {
                    case 0:    //请求加速度裸数据
                        byte[] s = {0x01};
                        updateList(dAddress, "cmd:0x71," + commants[0] + " :"
                                + Arrays.toString(s));
                        accelerToFile=false;
                        write(dAddress, s.length, 0x71, s);
                        break;
                    case 1:    //请求心率裸数据
                        byte[] sh = {0x01};
                        updateList(dAddress, "cmd:0x73," + commants[1] + " :"
                                + Arrays.toString(sh) + ",请等待");
                        write(dAddress, sh.length, 0x73, sh);
                        break;
                    case 2:    //请求关闭加速度裸数据实时同步
                        byte[] shi = {0x00};
                        updateList(dAddress, "cmd:0x71," + commants[2] + " :"
                                + Arrays.toString(shi));
                        accelerToFile = false;
                        write(dAddress, shi.length, 0x71, shi);
                        break;
                    case 3:    //请求关闭心率裸数据实时同步
                        byte[] shii = {0x00};
                        updateList(dAddress, "cmd:0x73," + commants[3] + " :"
                                + Arrays.toString(shii) + ",请等待");
                        rawHeartToFile = false;
                        write(dAddress, shii.length, 0x73, shii);
                        break;

                    case 4:    //请求加速度裸数据写入文件
                        byte[] ss = {0x01};
                        updateList(dAddress, "cmd:0x71," + commants[4] + " :"
                                + Arrays.toString(ss));
                        accelerToFile = true;
                        write(dAddress, ss.length, 0x71, ss);
                        break;
                    case 5:    //请求心率裸数据写入文件
                        byte[] shf = {0x01};
                        updateList(dAddress, "cmd:0x73," + commants[5] + " :"
                                + Arrays.toString(shf) + ",请等待");
                        rawHeartToFile = true;
                        write(dAddress, shf.length, 0x73, shf);
                        break;
                }
            }
        });
        rawdataDialog = builder.create();
    }

    private AlertDialog authDialog;

    private void initAuthDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("认证命令");
        final String[] commants = {
                getString(R.string.reg_gait),
                getString(R.string.reg_ppg),
                getString(R.string.auth_gait),
                getString(R.string.auth_ppg),


        };
        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // System.out.println("dBleDevice:" + dBleDevice.getAddres());
                switch (which) {
                    case 0:    //请求步态认证注册
                        updateList(dAddress, "cmd:0x71," + commants[0] + " :" + "数据正在后台请求，请等待...");
                        break;
                    case 1:    //请求PPG认证注册
                        updateList(dAddress, "cmd:0x73," + commants[1] + " :" +"数据正在后台请求，请等待..." );
                        break;
                    case 2:    //请求步态认证
                        updateList(dAddress, "cmd:0x73," + commants[2] + " :" +"数据正在后台请求，请等待..." );
                        break;
                    case 3:    //请求PPG认证
                        updateList(dAddress, "cmd:0x73," + commants[3] + " :" +"数据正在后台请求，请等待..." );
                        break;
                }
            }
        });
        authDialog = builder.create();
    }

    private AlertDialog authResultDialog;

    private void initAuthResultDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("认证结果命令");
        final String[] commants = {
                getString(R.string.reg_gait_result),
                getString(R.string.reg_ppg_result),
                getString(R.string.auth_gait_result),
                getString(R.string.auth_ppg_result),


        };
        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // System.out.println("dBleDevice:" + dBleDevice.getAddres());
                switch (which) {
                    case 0:    //请求步态认证注册结果
                        updateList(dAddress, "步态数据采集结束");
                        updateList(dAddress, "服务器正在处理步态数据，请稍等...");
                        updateList(dAddress, "步态认证注册成功");
                        initResultDialog("步态认证","test01","注册成功");
                        resultDialog.show();
                        break;
                    case 1:    //请求PPG认证注册结果
                        updateList(dAddress, "PPG数据采集结束");
                        updateList(dAddress, "服务器正在处理PPG数据，请稍等...");
                        updateList(dAddress, "PPG认证注册成功." );
                        initResultDialog("PPG认证","test01","注册成功");
                        resultDialog.show();
                        break;
                    case 2:    //请求步态认证结果
                        updateList(dAddress, "步态数据采集结束");
                        updateList(dAddress, "正在处理步态数据并提取特征，请稍等...");
                        updateList(dAddress, "服务器正在验证步态数据，请稍等...");
                        updateList(dAddress, "步态认证成功" );
                        initResultDialog("步态认证","test01","认证成功");
                        resultDialog.show();
                        break;
                    case 3:    //请求PPG认证结果
                        updateList(dAddress, "PPG数据采集结束");
                        updateList(dAddress, "正在处理PPG数据并提取特征，请稍等...");
                        updateList(dAddress, "服务器正在验证PPG数据，请稍等...");
                        updateList(dAddress, "PPG认证成功." );
                        initResultDialog("PPG认证","test01","认证成功");
                        resultDialog.show();
                        break;
                }
            }
        });
        authResultDialog = builder.create();
    }

    private AlertDialog resultDialog;

    private void initResultDialog(String authMethord,String user,String result){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(authMethord);
        builder.setMessage("用户"+user+"认证"+result);
        resultDialog = builder.create();
    }

    public boolean get_accelertneed_towrite(){
        return accelerToFile;
    }

    public boolean get_heartneed_towrite(){
        return heartToFile;
    }

    public boolean get_temprateneed_towrite(){
        return temprateToFile;
    }

    public boolean get_rawHeart_towrite(){
        return rawHeartToFile;
    }

    public boolean get_realSport_towrite(){
        return realSportToFile;
    }


    /***
     * 接收选择的结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        L.i("data数据:----------" +data);
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    // 刚打开蓝牙实际还不能立马就能用
                } else {
                    Toast.makeText(this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_scan:
                itemSacn = item;
                scanDialog.show();
                scanLeDevice();
                break;

            case R.id.action_setting:
                if (dBleDevice != null)
                    settingDialog.show();
                break;

            case R.id.action_security:
                if (dBleDevice != null)
                    securityDialog.show();
                break;

            case R.id.action_sports:
                if (dBleDevice != null)
                    sportsDialog.show();
                break;

            case R.id.action_health:
                if (dBleDevice != null)
                    healthDialog.show();
                break;

            case R.id.action_rawdata:
                if (dBleDevice != null)
                    rawdataDialog.show();
                break;

            case R.id.action_auth:
                if(dBleDevice != null)
                    authDialog.show();
                break;
            case R.id.action_authResult:
                if(dBleDevice != null)
                    authResultDialog.show();
                break;

            case R.id.action_clear:
                clearList(dAddress);
                break;

            case R.id.action_disconn:
                updateList(dAddress, "断开连接...");
                if (dBleDevice != null)
                    dBleDevice.disconnect();
                break;

            case R.id.action_connect:
                updateList(dAddress, "开始连接...");
                if (dBleDevice != null)
                    dBleDevice.connect();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setHandler(String address, Handler mHandler) {
        System.out.println("setHandler :" + address);
        if (!mHandlers.containsKey(address)) {
            mHandlers.put(address, mHandler);
        }
    }

    /**
     * 更新列表打印信息
     *
     * @param address
     */
    public void updateList(String address, String value) {
        Handler cHandler = mHandlers.get(address);
        Message msg = new Message();
         msg.obj = address;
        Bundle b = new Bundle();
        b.putString(address, value);
        msg.setData(b);
        msg.what = 3;
        if (cHandler != null)
            cHandler.sendMessage(msg);
        System.out.println("clearList");
    }

    /**
     * 清除列表信息
     *
     * @param address
     */
    private void clearList(String address) {
        System.out.println("clearList :" + address);
        Handler cHandler = mHandlers.get(address);
        Message msg = new Message();
        msg.obj = address;
        Bundle b = new Bundle();
        msg.setData(b);
        msg.what = 2;
        if (cHandler != null)
            cHandler.sendMessage(msg);
        System.out.println("clearList");
    }


}