package com.us.Utils;

import android.content.SharedPreferences;

import com.us.App;


public class Preferences {
	
	private static String PREFERANCES = "MyHealthyLive_Preferances";

	/**
	 * 获取key的值
	 * @param paramName key
	 * @return
	 */
	public static String getSettingsParam(String paramName) {
		SharedPreferences settings = getPrefferences();
		return settings.getString(paramName, "");
	}

	/**
	 * 获取key对应的布尔值
	 * @param paramName
	 * @return
	 */
	public static Boolean getSettingsParmBoolean(String paramName){
		SharedPreferences settings = getPrefferences();
		return settings.getBoolean(paramName,false);
	}

	public static SharedPreferences getPrefferences() {
		SharedPreferences settings = App.getmContext()
				.getSharedPreferences(PREFERANCES, 0);
		return settings;
	}

	public static void setSettingsParam(String paramName, String paramValue) {
		SharedPreferences settings = getPrefferences();
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(paramName, paramValue);
		editor.commit();
	}

	public static void setSettingsParamBoolean(String paramName,boolean value){
		SharedPreferences settings = getPrefferences();
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(paramName,value);
	}

	public static void setSettingsParamLong(String paramName, long Value) {
		SharedPreferences settings = getPrefferences();
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(paramName, Value);
		editor.commit();
	}

	public static long getSettingsParamLong(String paramName){
		SharedPreferences settings = getPrefferences();
		return settings.getLong(paramName,0);
	}

	public static void setSettingsParamInt(String paramName, int Value) {
		SharedPreferences settings = getPrefferences();
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(paramName, Value);
		editor.commit();
	}

	public static long getSettingsParamInt(String paramName){
		SharedPreferences settings = getPrefferences();
		return settings.getInt(paramName,0);
	}


}
