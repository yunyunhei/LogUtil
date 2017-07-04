package com.czm.library;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import com.czm.library.crash.CrashHandler;
import com.czm.library.encryption.IEncryption;
import com.czm.library.save.ISave;
import com.czm.library.save.imp.LogWriter;
import com.czm.library.upload.ILogUpload;
import com.czm.library.upload.UploadService;
import com.czm.library.util.NetUtil;

/**
 * 日志崩溃管理框架
 * Created by czm.
 */
public class LogUtil {

    private static LogUtil mLogUtil;
    /**
     * 设置上传的方式
     */
    public ILogUpload mUpload;
    /**
     * 设置缓存文件夹的大小,默认是50MB
     */
    private long mCacheSize = 50 * 1024 * 1024;

    /**
     * 设置日志保存的路径
     */
    private String mROOT;

    /**
     * 设置加密方式
     */
    private IEncryption mEncryption;

    /**
     * 设置日志的保存方式
     */
    private ISave mLogSaver;

    /**
     * 设置在哪种网络状态下上传，true为只在wifi模式下上传，false是wifi和移动网络都上传
     */
    private boolean mWifiOnly = true;

    /**
     * 设置上传日志信息为所有
     */
    public static final int LOG_LEVE_INFO = 1;

    /**
     * 设置上传日志信息为crash
     */
    public static final int LOG_LEVE_ERROR = 0;

    /**
     * 设置默认为只记录crash日志
     */
    private int mLogLeve = LOG_LEVE_ERROR;


    private LogUtil() {
    }


    public static LogUtil getInstance() {
        if (mLogUtil == null) {
            synchronized (LogUtil.class) {
                if (mLogUtil == null) {
                    mLogUtil = new LogUtil();
                }
            }
        }
        return mLogUtil;
    }

    public LogUtil setCacheSize(long cacheSize) {
        this.mCacheSize = cacheSize;
        return this;
    }

    public LogUtil setEncryption(IEncryption encryption) {
        this.mEncryption = encryption;
        return this;
    }

    public LogUtil setUploadType(ILogUpload logUpload) {
        mUpload = logUpload;
        return this;
    }

    public LogUtil setWifiOnly(boolean wifiOnly) {
        mWifiOnly = wifiOnly;
        return this;
    }

    public LogUtil setLogLeve(int logLeve){

        if (logLeve==LOG_LEVE_INFO) {
            mLogLeve = LOG_LEVE_INFO;
        }else {
            mLogLeve = LOG_LEVE_ERROR;
        }

        return this;
    }


    public LogUtil setLogDir(Context context, String logDir) {
        if (TextUtils.isEmpty(logDir)) {
            //如果SD不可用，则存储在沙盒中
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                mROOT = context.getExternalCacheDir().getAbsolutePath();
            } else {
                mROOT = context.getCacheDir().getAbsolutePath();
            }
        } else {
            mROOT = logDir;
        }
        return this;
    }

    public LogUtil setLogSaver(ISave logSaver) {
        this.mLogSaver = logSaver;
        return this;
    }

    public int getLogLeve(){
        return mLogLeve;
    }


    public String getROOT() {
        return mROOT;
    }

    public void init(Context context) {
        if (TextUtils.isEmpty(mROOT)) {
            //如果SD不可用，则存储在沙盒中
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                mROOT = context.getExternalCacheDir().getAbsolutePath();
            } else {
                mROOT = context.getCacheDir().getAbsolutePath();
            }
        }
        if (mEncryption != null) {
            mLogSaver.setEncodeType(mEncryption);
        }
        CrashHandler.getInstance().init(mLogSaver);
        LogWriter.getInstance().init(mLogSaver);
    }

    public ILogUpload getUpload() {
        return mUpload;
    }

    public long getCacheSize() {
        return mCacheSize;
    }

    /**
     * 调用此方法，上传日志信息
     *
     * @param applicationContext 全局的application context，避免内存泄露
     */
    public void upload(Context applicationContext) {
        //如果没有设置上传，则不执行
        if (mUpload == null) {
            return;
        }
        //如果网络可用，而且是移动网络，但是用户设置了只在wifi下上传，返回
        if (NetUtil.isConnected(applicationContext) && !NetUtil.isWifi(applicationContext) && mWifiOnly) {
            return;
        }
        Intent intent = new Intent(applicationContext, UploadService.class);
        applicationContext.startService(intent);
    }


}