package com.luckyxmobile.correction.util;

import android.app.Activity;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cn.forward.androids.utils.LogUtil;

/**
 * @author qjj
 * @date 2019/08/26
 * 用于销毁指定活动
 */
public class DestroyActivityUtil {
    private static Map<String, Activity> destroyMap = new HashMap<>();

    /**
     * 将Activity添加到队列中
     * @param activity 活动
     * @param activityName 活动名
     */
    public static void addDestroyActivityToMap(Activity activity, String activityName) {
        destroyMap.put(activityName, activity);
    }

    /**
     * 根据名字销毁制定Activity
     * @param activityName 活动名
     */
    public static void destroyActivity(String activityName) {
        Set<String> keySet = destroyMap.keySet();
        LogUtil.i(String.valueOf(keySet.size()));
        if (keySet.size() > 0) {
            for (String key : keySet) {
                if (activityName.equals(key)) {
                    Objects.requireNonNull(destroyMap.get(key)).finish();
                }
            }
        }
    }


    /**
     * 销毁全部Activity
     */
    public static void destroyActivityALL(){
        Set<String> keySet = destroyMap.keySet();
        LogUtil.i(String.valueOf(keySet.size()));
        if (keySet.size() > 0) {
            for (String key : keySet) {
                Objects.requireNonNull(destroyMap.get(key)).finish();
            }
        }
    }
}
