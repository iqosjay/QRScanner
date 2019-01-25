package com.iqos.test;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.iqos.qrscanner.app.QRScannerActivity;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.VIBRATE;

public class MainActivity extends AppCompatActivity {
    private static final String[] PERMISSIONS = new String[]{CAMERA, VIBRATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openScan(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (null == getDeniedPms()) {
                startActivity(new Intent(this, QRScannerActivity.class));
            } else {
                requestPermissions(PERMISSIONS, 1);
            }
        } else {
            startActivity(new Intent(this, QRScannerActivity.class));
        }
    }

    private String getDeniedPms() {
        return getDeniedPms(PERMISSIONS);
    }

    private String getDeniedPms(String[] permissions) {
        for (String permission : permissions) {
            if (0 != checkSelfPermission(permission)) {
                return permission;
            }
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (1 == requestCode) {
            String deniedPermission = getDeniedPms(permissions);
            if (null == deniedPermission) {
                startActivity(new Intent(this, QRScannerActivity.class));
            } else {
                if (!shouldShowRequestPermissionRationale(deniedPermission)) {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setTitle("权限缺失")
                            .setMessage("扫描二维码必须要使用相机权限")
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
}
