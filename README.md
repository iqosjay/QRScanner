# QRScanner
如同它的名字一样好理解

这是一个扫描二维码的简单库

方便那些不想麻烦的懒人使用

当前版本是2.0

扫码的速度非常快

支持自定义界面

支持连续扫描

使用方法:

首先

在build.gradle(Project)文件中添加

    allprojects {
        repositories {
            google()
            maven {
                url "https://jitpack.io"
            }
            jcenter()
        }
    }


然后再在build.gradle(app)中添加依赖

    dependencies {
        implementation 'com.github.iqosjay:QRScanner:2.0'
    }

如果你只是想使用这个功能

那么只需要这样启动

    public void openScan() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivityForResult(intent, QRScannerActivity.SCAN_RESULT_CODE);
    }

当你想要获取扫码的结果

只需要重写onActivityResult()方法

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case QRScannerActivity.SCAN_RESULT_CODE:
                if (null == data) break;
                String result = data.getStringExtra(QRScannerActivity.SCAN_RESULT);
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                break;
        }
    }

这样你就能拿到扫码的结果(我这里只是Toast了一下)

如果你觉得这个库的作者弄的界面太恶心了

你也可以使用你自己的界面

使用方法如下

首先你新建一个Activity继承自QRScannerActivity

然后把它的布局文件修改为这样

    <?xml version="1.0" encoding="utf-8"?>
    <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context="com.example.abcd.MyQrScannerActivity">
        <!--SurfaceView-->
        <SurfaceView
            android:id="@+id/preview_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
        <!--扫码界面-->
        <com.iqos.qrscanner.widget.ViewfinderView
            android:id="@+id/viewfinder_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent" 
            app:scan_line_move_speed="3"
            app:scan_line_res="@drawable/qr_code_scan_line"
            app:scan_rect_corner_color="#F5A623"
            app:scan_rect_corner_length="20dp"
            app:scan_rect_corner_width="3dp"
            app:scan_text_above_rect="false"
            app:scan_tip_text="@string/scan_tip"
            app:scan_tip_text_size="14sp" />
        <!--标题栏-->
        <include layout="@layout/app_layout_title" />

    </RelativeLayout>
    
    
 注意！！
 SurfaceView和ViewFinderView的id是不能变的一定要是上面写的那样
 
 标题栏也是必须include的
 
 否则会提示找不到控件导致闪退
 
 
上面的属性意思：

 app:scan_line_move_speed：就是扫描的线移动的速度
 
 app:scan_line_res：扫描的线的图片资源
 
 app:scan_rect_corner_color：扫码框的四个角的颜色
 
 app:scan_rect_corner_length：扫码框的四个角的长度
 
 app:scan_rect_corner_width：扫码框的四个角的宽度
 
 app:scan_text_above_rect：提示文本是否处于扫描框之上
 
 app:scan_tip_text：扫描的提示文本
 
 app:scan_tip_text_size：扫描提示文本的文字大小
 
 
 扫码框的大小不能修改！
 
 长和宽都固定为屏幕宽度的2/3
 
 这个布局文件你已经写好了
 
 现在重写Activity中的方法
 
    public class MyQrScannerActivity extends QRScannerActivity {

        @Override
        protected int getLayoutResources() {
            return R.layout.activity_my_qr_scanner;
        }
    }
    
 把getLayoutResources()方法重写修改返回值为你刚才写的布局
 
 这个时候启动就不能启动QRScannerActivity了
 
 而要启动你刚才写的那个子类
 
 比如这里写的子类是MyQrScannerActivity
 
 所以我启动就是
 
    public void openScan() {
        Intent intent = new Intent(this, MyQrScannerActivity.class);
        startActivity(intent);
    }
 
 这样启动之后就是你自己写的界面了~
 
 那么扫描的结果怎么获取呢？
 
 重写handleDecode()方法
 
     @Override
    public void handleDecode(Result result) {
        Toast.makeText(this, result.getText(), Toast.LENGTH_SHORT).show();
    }
    
 这样就能拿到扫码结果之后写自己的逻辑
 
 其它：
 
 1、如果想要扫描成功之后继续扫描
 
 只需要在需要继续扫描的调用
 
    super.restartQRScanner();
    
 即可继续扫描
 
 2、如果需要在扫描成功之后振动手机和播放声音
 
 只需要在解码成功的方法那里调用
 
    super.playBeepSoundAndVibrate();
    
 像这样
 
     @Override
    public void handleDecode(Result result) {
        Toast.makeText(this, result.getText(), Toast.LENGTH_SHORT).show();
        super.playBeepSoundAndVibrate();
    }

这样就有了振动和声音

其它的功能大家如果需要或者是说遇到任何Bug

欢迎发送建议到我的邮箱

    iqosjay@gmail.com
    
    
感谢能看到这里的你~~

ありがとううううう
