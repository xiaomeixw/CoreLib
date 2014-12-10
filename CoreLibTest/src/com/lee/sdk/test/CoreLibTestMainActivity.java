package com.lee.sdk.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CoreLibTestMainActivity extends BaseListActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i("DemoShell", "os_name:" + System.getProperty("os.name"));
        Log.i("DemoShell", "os_arch:" + System.getProperty("os.arch"));
        Log.i("DemoShell", "os_version:" + System.getProperty("os.version"));
        
        setUseDefaultPendingTransition(true);
    }
    
//    void testLockFile() {
//        File root = android.os.Environment.getExternalStorageDirectory();
//        File baiduFile = new File(root, "baidu/test");
//        
//        if (!baiduFile.exists()) {
//            try {
//                baiduFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        
//        if (baiduFile.exists()) {
//            try {
//                RandomAccessFile randomAccessFile = new RandomAccessFile(baiduFile, "rws");
//                FileLock lock = randomAccessFile.getChannel().tryLock();
//                
////                FileOutputStream fis = new FileOutputStream(baiduFile);
////                FileChannel fc = fis.getChannel();
////                if (null != fc) {
////                    FileLock fl = fc.tryLock();
////                    if (null == fl) {
////                        System.out.println("已被打开");
////                    } else {
////                        System.out.println("获取对此通道的文件的独占锁定。");
////                    }
////                }
//                
//                byte[] buffer = new byte[1000];
//                buffer[0] = 100;
//                buffer[1] = 100;
//                buffer[2] = 100;
//                randomAccessFile.write(buffer);
//                
//                try {
//                    Thread.sleep(1000 * 1000 * 10000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    
    @Override
    protected void onDestroy() {
//        GoogleAnalyticsBL.getInstance().dispatch();

        super.onDestroy();
    }

    @Override
    public Intent getQueryIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(CORELIT_TEST_CAGEGORY);

        return intent;
    }
    
    @Override
    public void finish() {
        super.finish();
    }
}
