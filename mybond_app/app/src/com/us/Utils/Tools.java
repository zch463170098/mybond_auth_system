package com.us.Utils;


import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {

//
//	public static String IpAdresss = "http://10.170.24.225:8080/tour/"; //实验室台式机
//
	public static String IpAdresss = "http://192.168.43.122:8080/mybond/"; //笔记本自己wifi

//	public static String IpAdresss = "http://192.168.31.124:8080/mybond/"; //家里

//	public static String IpAdresss = "http://139.199.81.242:8080/tour/"; //云
//	public static String imgAdress = "http://139.199.81.242:8080/";  //

	/**
	 * 判断是否为空
	 * @param string
	 * @return
	 */
	public static boolean isNull(String string){
		if (string !=null && !string.isEmpty()) {
			return false;
		}else{
			return true;
		}


	}

	public static String getTime(){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");// HH:mm:ss
		Date date = new Date(System.currentTimeMillis());
		String time = simpleDateFormat.format(date);
		return time;
	}


	public static String getStringTime(){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date(System.currentTimeMillis());
		String time = simpleDateFormat.format(date);
		return time;
	}



	
}
