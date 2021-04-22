package com.wt.wtplayer

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wt.wtplayer.databinding.MainTestActBinding
import com.wt.wtplayer.ui.CustomMediaController
import com.wt.wtplayer.widget.WtVideoView

/**
 * 主页面
 */
class MainTestActivity : AppCompatActivity() {
    private lateinit var mBinding: MainTestActBinding
    private var mMediaController: CustomMediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,
                R.layout.main_test_act)

        lifecycle.addObserver(mBinding.videoView)

        initMediaController()

        mBinding.tvPlay.setOnClickListener(View.OnClickListener {
            if(TextUtils.isEmpty(mBinding.etPath.text)){
                Toast.makeText(this,"请输入路径",Toast.LENGTH_SHORT).show()
            }else{
                var videoPath = mBinding.etPath.text.toString()
                mBinding.videoView.setVideoPath(videoPath)
                mBinding.videoView.start()
            }
        })
        mBinding.tvPlay.callOnClick()
    }


    private fun initMediaController(){
        mMediaController = CustomMediaController(this, false)
        //mMediaController?.videoControlListener = this
        mBinding.videoView.setMediaController(mMediaController)
    }

}
