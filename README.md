[![](https://www.jitpack.io/v/wuhongsheng/wtplayer.svg)](https://www.jitpack.io/#wuhongsheng/wtplayer)
# wtplayer
音视频播放器(基于[ijkplayer](https://github.com/bilibili/ijkplayer))
扩展录像、截屏等功能

![](https://github.com/wuhongsheng/wtplayer/blob/master/record.gif)

## 特性

1. 使用kotlin和AndroidX编写，支持java使用
2. 简单易用支持生命周期感知无需处理so库加载和释放
3. RTSP/RTMP 秒开优化


## 导入方式
### 将JitPack存储库添加到您的构建文件中(项目根目录下build.gradle文件)
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### 添加依赖项

```
dependencies {
    implementation 'com.github.wuhongsheng:wtplayer:-SNAPSHOT'
}
```

## WtPlayer使用

1.布局文件
```xml
  <RelativeLayout
            android:id="@+id/video_parent"
            android:background="#00000000"
            app:layout_constraintDimensionRatio="w,3:4"
            app:layout_constraintTop_toBottomOf="@id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="0dp">
            <com.wt.wtplayer.widget.WtVideoView
                android:id="@+id/video_view"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_gravity="center" />
        </RelativeLayout>
```

```java
 class MainTestActivity : AppCompatActivity() {
     private lateinit var mBinding: MainTestActBinding
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)

         mBinding = DataBindingUtil.setContentView(this,
                 R.layout.main_test_act)

         lifecycle.addObserver(mBinding.videoView)

         mBinding.videoView.setHudView(mBinding.hudView)
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
 }
```


### TODO
- [ ] 升级ffmpeg4.0
- [ ] 增加投屏功能

