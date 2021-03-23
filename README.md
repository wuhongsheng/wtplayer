[![](https://www.jitpack.io/v/wuhongsheng/wtplayer.svg)](https://www.jitpack.io/#wuhongsheng/wtplayer)
# wtplayer
音视频播放器(基于ijkplayer)
扩展录像、截屏等方法




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
[![](https://www.jitpack.io/v/wuhongsheng/wtplayer.svg)](https://www.jitpack.io/#wuhongsheng/wtplayer)

```
dependencies {
    implementation 'com.github.wuhongsheng:wtplayer:-SNAPSHOT'
}
```

## 使用示例

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

```
  mBinding!!.videoView.setVideoPath(path)
  mBinding!!.videoView?.start()
```


## TODO

