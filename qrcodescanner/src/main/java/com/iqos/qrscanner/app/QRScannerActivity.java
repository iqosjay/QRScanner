package com.iqos.qrscanner.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.iqos.qrscanner.R;
import com.iqos.qrscanner.camera.CameraManager;
import com.iqos.qrscanner.decoding.CaptureActivityHandler;
import com.iqos.qrscanner.decoding.InactivityTimer;
import com.iqos.qrscanner.utils.QRCodeDecoder;
import com.iqos.qrscanner.widget.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;


public class QRScannerActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String[] READ_GALLERY_PERMISSION = new String[]{READ_EXTERNAL_STORAGE};
    private static final long VIBRATE_DURATION = 200L;
    private static final float BEEP_VOLUME = 0.10f;
    public static final int SCAN_RESULT_CODE = 15613;
    public static final String SCAN_RESULT = "scan_result";
    private ViewfinderView mScanView;
    private CaptureActivityHandler handler;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean hasSurface;
    private boolean playBeep;
    private boolean vibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResources());
//        this.setFitSystem();
        this.findViews();
        this.init();
    }

    /**
     * fitSystem="true"
     */
    private void setFitSystem() {
        ViewGroup mContentView = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            mChildView.setFitsSystemWindows(true);
        }
    }

    /**
     * 获取布局文件
     *
     * @return XML里面的布局文件
     */
    protected int getLayoutResources() {
        return R.layout.activity_qrscanner;
    }


    /**
     * 获取XML里面的控件
     */
    protected void findViews() {
        this.mScanView = findViewById(R.id.viewfinder_view);
    }


    /**
     * 初始化
     */
    protected void init() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CameraManager.init(getApplication());
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        ActionBar supportActionBar = getSupportActionBar();
        if (null != supportActionBar) {
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            this.onBackPressed();
            return true;
        } else if (i == R.id.open_gallery) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                if (null == getDeniedPms(READ_GALLERY_PERMISSION)) {
                    openGallery();
                } else {
                    requestPermissions(READ_GALLERY_PERMISSION, 300);
                }
            } else {
                openGallery();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private String getDeniedPms(String[] permissions) {
        for (String s : READ_GALLERY_PERMISSION) {
            if (0 != checkSelfPermission(s)) {
                return s;
            }
        }
        return null;
    }

    private void openGallery() {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intentToPickPic, 2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            //noinspection deprecation
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;
        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService != null && audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * 处理扫描结果
     *
     * @param result 扫描的结果
     */
    public void handleDecode(Result result) {
        playBeepSoundAndVibrate();
        showResult(result.getText());
        /*inactivityTimer.onActivity();
        String resultStr = result.getText();
        Intent intent = new Intent();
        intent.putExtra(SCAN_RESULT, resultStr);
        this.setResult(SCAN_RESULT_CODE, intent);
        finish();*/
    }

    private void showResult(final String result) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("识别结果")
                .setMessage(result)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartQRScanner();
                    }
                })
                .setNegativeButton("复制", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 得到剪贴板管理器
                        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        cmb.setPrimaryClip(ClipData.newPlainText("iqosjay@gmail.com", result));
                        Toast.makeText(QRScannerActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException | RuntimeException ioe) {
            ioe.printStackTrace();
        }
        if (null == handler) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        mScanView.drawViewfinder();
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);
            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    /**
     * 扫码成功后播放“滴”并且振动手机
     */
    protected final void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(VIBRATE_DURATION);
            }
        }
    }

    /**
     * 连续扫描、调用此方法即可重新扫描
     */
    protected final void restartQRScanner() {
        if (null != handler) {
            handler.restartPreviewAndDecode();//重新启动预览和解码
        }
    }


    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (300 == requestCode) {
            String pms;
            if (null == (pms = getDeniedPms(permissions))) {
                openGallery();
            } else {
                if (!shouldShowRequestPermissionRationale(pms)) {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setTitle("权限缺失")
                            .setMessage("从相册读取图片必须要使用读取手机内存权限")
                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .setCancelable(true)
                            .create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (2 == requestCode) {
            if (null == data) return;
            final Uri uri = data.getData();
            if (null != uri) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String qrContent = QRCodeDecoder.syncDecodeQRCode(QRScannerActivity.this, uri);
                        if (TextUtils.isEmpty(qrContent)) {
                            Toast.makeText(QRScannerActivity.this, "未发现二维码", Toast.LENGTH_SHORT).show();
                        } else {
                            showResult(qrContent);
                        }
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
