package com.xulaoyao.ezphotoedit.listener;

/**
 * PhotoEditListener
 * Created by renwoxing on 2018/3/20.
 */
public interface PhotoEditListener {
    //code == 200 表示加载成功  5xx 代表一些提示  500表示错误
    void info(int code, String msg); // 返回信息，如
}
