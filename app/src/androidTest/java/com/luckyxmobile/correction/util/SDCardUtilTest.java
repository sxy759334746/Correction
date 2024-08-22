package com.luckyxmobile.correction.util;

import android.net.Uri;
import android.os.Environment;
import androidx.test.InstrumentationRegistry;
import com.luckyxmobile.correction.LitePalTestCase;
import com.luckyxmobile.correction.bean.Topic;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.litepal.LitePal;

import java.io.File;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SDCardUtilTest extends LitePalTestCase {
    private Topic topic;
    private String path1 = "/storage/emulated/0/Correction/Book/bookCover_20190822164427.jpeg";
    private String path2 = "/storage/emulated/0/Correction/Topic/topic_20190822165057.jpeg";
    private String otherPath1 = "/storage/emulated/0/Correction/Topic/topic_20190822161121.jpeg";
    private String otherPath2 = "/storage/emulated/0/Correction/Topic/topic_20190822165781.jpeg";

    @Before
    public void insertFakeData(){
        topic = new Topic();
        topic.setTopic_original_picture(path1 + "," + path2 + ";" + otherPath1 + "," + otherPath2);
        assertTrue(topic.save());
    }

    @After
    public void deleteFakeData(){
        assertEquals(1, LitePal.delete(Topic.class,topic.getId()));
    }

    @Test
    public void findPhotoPath(){

//        String[] results = SDCardUtil.findPhotoPath(topic, "topic_error_cause_picture");
//        assertNotNull(results);
//        assertEquals(path1 + "," + path2,results[0]);
//        assertEquals(otherPath1 + "," + otherPath2, results[1]);

    }

    /**
     * 检测SD卡是否存在
     * @author:lyw
     */
    @Test
    public void hasSdcard() {
        String state = Environment.getExternalStorageState();
        //判断是否存在
        assertTrue(state.equals(Environment.MEDIA_MOUNTED));
    }
    /**
     * 检测动态获取读写权限
     * @author:lyw
     */
    @Test
    public void checkPermission() {
//        SDCardUtil.checkPermission(MainActivity);
//        boolean isCheck = true;
//        if(MainActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//               isCheck = false;
//        }
//        assertTrue(isCheck);
    }

    /**
     * 检测保存图片
     * @author lyw
     */
    @Test
    public void savePhoto() {
//        File file_zero = SDCardUtil.savePhoto(0);
//        File file_one = SDCardUtil.savePhoto(1);
//        //文件不为空
//        assertNotNull(file_zero);
//        assertNotNull(file_one);
    }
    /**
     * 检测根据Uri获取文件
     * @author lyw
     */
    @Test
    public void getFile() {
     File file = new File(SDCardUtil.getBookDIR());
     File file1 = SDCardUtil.getFile(InstrumentationRegistry.getTargetContext(), Uri.fromFile(file));
     //文件为空
     assertNull(file1);
    }
    /**
     * 检测根据文件获取Uri
     * @author lyw
     */
    @Test
    public void getUri() {
        File file = new File(SDCardUtil.getBookDIR());
        Uri uri = SDCardUtil.getUri(InstrumentationRegistry.getTargetContext(),file);
        //在这里，Uri相当于文件路径，不为空
        assertNotNull(uri);

    }
    /**
     * 检测根据文件路径删除文件
     * @author lyw
     */
    @Test
    public void deleteFile() {
        String filePath = SDCardUtil.getBookDIR();
        File file = new File(filePath);
        SDCardUtil.deleteFile(filePath,InstrumentationRegistry.getTargetContext());
        File file1 = SDCardUtil.getFile(InstrumentationRegistry.getTargetContext(), Uri.fromFile(file));

        assertNull(file1);
    }


}