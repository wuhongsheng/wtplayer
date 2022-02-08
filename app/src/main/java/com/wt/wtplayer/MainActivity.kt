package com.wt.wtplayer

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ScreenUtils
import com.wt.wtplayer.databinding.MainTestActBinding
import com.wt.wtplayer.ui.CustomMediaController
import com.wt.wtplayer.widget.WtVideoView

/**
 * 主页面
 */
class MainActivity : ComponentActivity(), CustomMediaController.VideoControlListener {
    private lateinit var mBinding: MainTestActBinding
    private var mMediaController: CustomMediaController? = null
    private var mViewModel:MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,
                R.layout.main_test_act)
        mViewModel = MainViewModel()
        mBinding.viewModel = mViewModel
        lifecycle.addObserver(mBinding.videoView)
        mBinding.lifecycleOwner = this

        initView()

        initMediaController()

        startPlay()

    }

    private fun initView() {
        mBinding.tvRecord.setOnClickListener(View.OnClickListener {
            if(!mBinding.videoView.isPlaying){
                Toast.makeText(this,"请先开启播放",Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            var result: Int = if (mViewModel?.isRecording == true)  mBinding.videoView.stopRecord() else mBinding.videoView.startRecord()
            if(result == 0){
                mViewModel?.switchVideoRecord()
            }else{
                Toast.makeText(this,"当前视频流格式可能不支持录像操作",Toast.LENGTH_SHORT).show()
            }
        })
        mBinding.tvScreenshot.setOnClickListener(View.OnClickListener {
            mBinding.videoView.snapshotPicture()
        })
    }


    private fun startPlay() {
        mBinding.tvPlay.setOnClickListener(View.OnClickListener {
            if(TextUtils.isEmpty(mBinding.etPath.text)){
                Toast.makeText(this,"请输入路径",Toast.LENGTH_SHORT).show()
            }else{
                var videoPath = mBinding.etPath.text.toString()
                mBinding.videoView.setVideoPath(videoPath)
                mBinding.videoView.setRenderType(WtVideoView.RENDER_SURFACE_VIEW)
                mBinding.videoView.start()
            }
        })
        mBinding.tvPlay.callOnClick()
    }

    /**
     * initMediaController
     */
    private fun initMediaController(){
        mMediaController = CustomMediaController(this, false)
        mMediaController?.videoControlListener = this
        mBinding.videoView.setMediaController(mMediaController)
    }

    override fun changeOrientation(landscape: Boolean) {
        if (landscape) this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun switchVideoQuality(type: String?) {
        TODO("Not yet implemented")
        //切换视频质量
    }

    /**
     * 横竖屏切换
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        var  mCurrentOrientation = newConfig.orientation;
        mViewModel?.screenLandscape?.value = mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE
        if(mViewModel?.screenLandscape?.value!!){
            ScreenUtils.setFullScreen(this)
        }else{
            ScreenUtils.setNonFullScreen(this)
        }

        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = resources.getDimensionPixelSize(resourceId)
        val width: Int = if(mViewModel?.screenLandscape?.value!!) ScreenUtils.getScreenWidth()-statusBarHeight else  ScreenUtils.getScreenWidth()
        val height: Int = if (mViewModel?.screenLandscape?.value!!) ScreenUtils.getScreenHeight() else width/4*3
        var layoutParams: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(width, height)
        layoutParams.topToBottom = R.id.toolbar
        mBinding.videoParent.layoutParams = layoutParams

        mMediaController = CustomMediaController(this, false,mViewModel?.screenLandscape?.value!!)
        mMediaController?.videoControlListener = this
        mBinding.videoView.setMediaController(mMediaController)
    }



}
