package com.wt.wtplayer.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 播放器设置
 *
 * @author whs
 * @date 2021/1/21
 */
public class PlayerSettings {
    private Context mAppContext;
    private SharedPreferences mSharedPreferences;

    public static final int PV_PLAYER__Auto = 0;
    public static final int PV_PLAYER__AndroidMediaPlayer = 1;
    public static final int PV_PLAYER__IjkMediaPlayer = 2;
    public static final int PV_PLAYER__IjkExoMediaPlayer = 3;

    //编解码方式
    public static final String SETTING_DECODE_TYPE = "SETTING_DECODE_TYPE";
    //取流方式
    public static final String SETTING_STREAM_TYPE = "SETTING_STREAM_TYPE";
    //自定义取流方式
    public static final String SETTING_CUSTOM_STREAM_TYPE = "SETTING_CUSTOM_STREAM_TYPE";
    //wifi自动播放
    public static final String SETTING_WIFI_AUTO_PLAY = "SETTING_WIFI_AUTO_PLAY";


    public PlayerSettings(Context context) {
        mAppContext = context.getApplicationContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
    }

  /*  public boolean getEnableBackgroundPlay() {
        //后台播放
        //String key = mAppContext.getString(R.string.pref_key_enable_background_play);
        return mSharedPreferences.getBoolean(key, false);
        //return true;
    }

    public int getPlayer() {
        //String key = mAppContext.getString(R.string.pref_key_player);
        String value = mSharedPreferences.getString(key, "");
        try {
            return Integer.valueOf(value).intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }*/

    /**
     * 1；标清 2: 高清
     * @return
     */
    public String getStreamType(){
        return mSharedPreferences.getString(PlayerSettings.SETTING_CUSTOM_STREAM_TYPE,"1");
    }

    public void setStreamType(String type){
        mSharedPreferences.edit().putString(PlayerSettings.SETTING_CUSTOM_STREAM_TYPE,type).apply();
    }

    /**
     * 解码方式
     * @return
     */
    public boolean getUsingMediaCodec() {
        //MediaCodec是Android提供的用于对音视频进行编解码的类
        //String key = mAppContext.getString(R.string.pref_key_using_media_codec);
        return mSharedPreferences.getBoolean(SETTING_DECODE_TYPE, false);
    }

    /*public boolean getUsingMediaCodecAutoRotate() {
        String key = mAppContext.getString(R.string.pref_key_using_media_codec_auto_rotate);
        return mSharedPreferences.getBoolean(key, false);
    }

    public boolean getMediaCodecHandleResolutionChange() {
        String key = mAppContext.getString(R.string.pref_key_media_codec_handle_resolution_change);
        return mSharedPreferences.getBoolean(key, false);
    }

    public boolean getUsingOpenSLES() {
        String key = mAppContext.getString(R.string.pref_key_using_opensl_es);
        return mSharedPreferences.getBoolean(key, false);
    }

    public String getPixelFormat() {
        String key = mAppContext.getString(R.string.pref_key_pixel_format);
        return mSharedPreferences.getString(key, "");
    }

    public boolean getEnableNoView() {
        String key = mAppContext.getString(R.string.pref_key_enable_no_view);
        return mSharedPreferences.getBoolean(key, false);
    }

    public boolean getEnableSurfaceView() {
        String key = mAppContext.getString(R.string.pref_key_enable_surface_view);
        return mSharedPreferences.getBoolean(key, false);
    }

    public boolean getEnableTextureView() {
        String key = mAppContext.getString(R.string.pref_key_enable_texture_view);
        return mSharedPreferences.getBoolean(key, false);
    }

    public boolean getEnableDetachedSurfaceTextureView() {
        String key = mAppContext.getString(R.string.pref_key_enable_detached_surface_texture);
        return mSharedPreferences.getBoolean(key, false);
    }

    public boolean getUsingMediaDataSource() {
        String key = mAppContext.getString(R.string.pref_key_using_mediadatasource);
        return mSharedPreferences.getBoolean(key, false);
    }

    public String getLastDirectory() {
        String key = mAppContext.getString(R.string.pref_key_last_directory);
        return mSharedPreferences.getString(key, "/");
    }

    public void setLastDirectory(String path) {
        String key = mAppContext.getString(R.string.pref_key_last_directory);
        mSharedPreferences.edit().putString(key, path).apply();
    }*/
}
