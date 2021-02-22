package com.wt.wtplayer

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * 主页面
 */
class MainTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_test_act)
        var tvPlay: TextView = findViewById(R.id.tv_play)
        tvPlay.setOnClickListener(View.OnClickListener {
           /* RetrofitHelper.getInstance().serviceApi
                    .getVideoStream("43100111011320000002", "0", "rtmp")
                    .enqueue(object : Callback<VideoResult> {
                        override fun onFailure(call: Call<VideoResult>, t: Throwable) {
                            ToastUtils.showShort("获取视频流失败");
                        }
                        override fun onResponse(call: Call<VideoResult>, response: Response<VideoResult>) =
                                try {
                                    if (response.body()?.result?.errorNum == 200) {
                                        var path: String = response.body()?.result!!.url
                                        var name: String = "RTMP";
                                        try {
                                            WtVideoPlayer.setPath("")
                                                    .startPlayer(this@MainTestActivity)
                                        }catch (e:Exception){
                                            ToastUtils.showShort("打开视频异常"+e.message)
                                        }

                                    }else{
                                        ToastUtils.showShort("获取视频流失败");
                                    }

                                } catch (e: Exception) {
                                    ToastUtils.showShort("获取视频流异常:$e");
                                }

                    })*/
        })
    }
}
