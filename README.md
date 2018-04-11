# QRScanner
项目的名字就是它的功能、简单、一个扫描二维码的库
目前这是1.0.1版本、功能非常简单、只能使用、不能修改颜色
这些修改会后续添加

使用方法:
build.gradle(Project)文件的
allprojects->repositories标签中加入

maven {url "https://jitpack.io"}


build.gradle(app)在dependencies标签中加入

implementation 'com.github.iqosjay:QRScanner:v1.0.1'

然后在需要使用的地方启动

Intent intent = new Intent(this, QRScannerActivity.class);
startActivityForResult(intent, QRScannerActivity.SCAN_RESULT_CODE);


获取扫描的结果、重写onActivityResult()方法

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


