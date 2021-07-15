package com.wt.wtplayer

import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * description
 * @author whs
 * @date 2021/7/14
 */
class MainViewModel: ViewModel() {

    val sdf = SimpleDateFormat("H:mm:ss", Locale.getDefault())
    // 录像计时
    private val handler = Handler()

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            Log.d("TAG", "run: ")
            var time: Long = System.currentTimeMillis() - startTime
            sdf.timeZone = TimeZone.getTimeZone("GMT+0")
            recordTip.value = sdf.format(time)
            handler.postDelayed(this, 1000)
        }
    }
    //录像开始时间
    var startTime: Long = System.currentTimeMillis();

    private val TAG: String = "MainViewModel"

    var message: MutableLiveData<String> = MutableLiveData()

    /**
     * 是否横屏
     */
    var screenLandscape: MutableLiveData<Boolean> = MutableLiveData()
    /**
     * 录像状态
     */
    var isRecording: Boolean = false

    var recordTip: MutableLiveData<String> = MutableLiveData()


    init {
        recordTip.value = "录像"
        screenLandscape.value = false
    }
    /**
     * 录像开关
     */
    fun switchVideoRecord() {
        Log.e(TAG, "switchVideoRecord:" + isRecording)
        isRecording =  !isRecording
        if (isRecording) {
            message.value = "开始录像"
            startTime = System.currentTimeMillis()
            //开启计时器
            handler.postDelayed(runnable, 1000)
        } else {
            //停止计时器
            handler.removeCallbacks(runnable)
            recordTip.value = "录制"
        }
    }
}