package com.luckyxmobile.correction.util;

import com.luckyxmobile.correction.util.PhotoUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * author:zc
 * created on:2019/7/25 11:49
 * description:
 */
public class PhotoUtilTest {
    /**
     * 测试重置存储路径的方法
     * 可能出现两种情况，1.重置成功，将resultPath置为default，2.重置失败，resultPath保持原样
     */
    @Test
    public void resetResultPath(){
        PhotoUtil.resetResultPath();
        Assert.assertEquals("default",PhotoUtil.getResultPath());
    }


    /**
     * 测试获取存储路径的方法
     */
    @Test
    public void getResultPath(){
        PhotoUtil.resultPath = "d:test";
        Assert.assertEquals("d:test",PhotoUtil.getResultPath());

        PhotoUtil.resetResultPath();
        Assert.assertEquals("default",PhotoUtil.getResultPath());
    }
}
