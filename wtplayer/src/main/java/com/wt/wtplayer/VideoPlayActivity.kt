package com.wt.wtplayer

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.wt.wtplayer.widget.AndroidMediaController
import com.wt.wtplayer.widget.WtVideoView
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.misc.ITrackInfo

/**
 * 视频播放界面
 */
class VideoPlayActivity : AppCompatActivity() {

    private var mVideoPath: String? = null
    private var mVideoUri: Uri? = null

    private var mMediaController: AndroidMediaController? = null
    private var mVideoView: WtVideoView? = null
    private var mToastTextView: TextView? = null

    /**
     * 视频调试字段显示
     */
    private var mHudView: TableLayout? = null
    private var mBackPressed = false

    companion object{
        private val TAG = VideoPlayActivity.javaClass.name
        private var isDebug:Boolean = true
        @JvmStatic
        fun intentTo(context: Context, videoPath: String, videoTitle: String?,isDebug: Boolean) {
            Log.e(TAG, "videoPath:$videoPath")
            this.isDebug = isDebug;
            context.startActivity(newIntent(context, videoPath, videoTitle))
        }
        @JvmStatic
        fun newIntent(context: Context?, videoPath: String?, videoTitle: String?): Intent? {
            val intent = Intent(context, VideoPlayActivity::class.java)
            intent.putExtra("videoPath", videoPath)
            intent.putExtra("videoTitle", videoTitle)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player_act)
        
        // handle arguments
        mVideoPath = intent.getStringExtra("videoPath")
        val intent = intent
        val intentAction = intent.action
        if (!TextUtils.isEmpty(intentAction)) {
            if (intentAction == Intent.ACTION_VIEW) {
                mVideoPath = intent.dataString
            } else if (intentAction == Intent.ACTION_SEND) {
                mVideoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    val scheme = mVideoUri?.scheme
                    if (TextUtils.isEmpty(scheme)) {
                        Log.e(TAG, "Null unknown scheme\n")
                        finish()
                        return
                    }
                    mVideoPath = if (scheme == ContentResolver.SCHEME_ANDROID_RESOURCE) {
                        mVideoUri?.path
                    } else if (scheme == ContentResolver.SCHEME_CONTENT) {
                        Log.e(TAG, "Can not resolve content below Android-ICS\n")
                        finish()
                        return
                    } else {
                        Log.e(TAG, "Unknown scheme $scheme\n")
                        finish()
                        return
                    }
                }
            }
        }
        // init UI
        initView()

        mMediaController = AndroidMediaController(this, false)
        //mMediaController!!.setSupportActionBar(actionBar)
        mToastTextView = findViewById<View>(R.id.toast_text_view) as TextView
        // init player
        IjkMediaPlayer.loadLibrariesOnce(null)
        IjkMediaPlayer.native_profileBegin("libijkplayer.so")

        mVideoView = findViewById<View>(R.id.video_view) as WtVideoView
        mVideoView?.setMediaController(mMediaController)
        mHudView = findViewById<View>(R.id.hud_view) as TableLayout
        mVideoView?.setHudView(mHudView)
        if(!isDebug){
            //debug界面
            mHudView?.visibility = View.GONE
        }

        // prefer mVideoPath
        if (mVideoPath != null) {
            mVideoView!!.setVideoPath(mVideoPath)
        } else if (mVideoUri != null) {
            mVideoView!!.setVideoURI(mVideoUri)
        } else {
            Log.e(TAG, "Null Data Source\n")
            finish()
            return
        }
        mVideoView?.start()
    }

    private fun initView() {

    }

    override fun onBackPressed() {
        mBackPressed = true
        super.onBackPressed()
    }

    override fun onStop() {
        super.onStop()
        if (mBackPressed || !mVideoView!!.isBackgroundPlayEnabled) {
            mVideoView?.stopPlayback()
            mVideoView?.release(true)
            mVideoView?.stopBackgroundPlay()
        } else {
            mVideoView?.enterBackground()
        }
        IjkMediaPlayer.native_profileEnd()
    }
}
