package com.wt.wtplayer.ui;

import android.graphics.Bitmap;
import android.widget.MediaController;

/**
 * description
 *
 * @author whs
 * @date 2022/4/11
 */
public interface CustomMediaPlayerControl extends MediaController.MediaPlayerControl {
 //截图
 Bitmap screenShot();
 //设置音量
 void setVolume(float volume);
 //开始录像
 int startRecord(String path);
 //结束录像 返回0成功
 int stopRecord();
}
