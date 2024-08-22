package com.luckyxmobile.correction.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import androidx.core.content.FileProvider;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.bean.TopicImagesPaint;

import org.litepal.LitePal;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class SDCardUtil {
    private static final String FILE_DIR = "/Correction";
    private static final String PDF_DIR = "/Pdf";
    private static final String Book_DIR = "/Book";
    private static final String Topic_DIR = "/Topic";

    /**
     * @return 获取外部存储目录, pdf, book, topic的存储目录
     * @author Changhao
     * @date 2019年7月25日
     */
    public static String getFileDir() {
        File fileDir = new File(Environment.getExternalStorageDirectory() + FILE_DIR);
        if (!fileDir.exists()){
            fileDir.mkdir();
        }
        return fileDir.getAbsolutePath();
    }

    public static String getPdfDir() {
        File pdfDir = new File(Environment.getExternalStorageDirectory() + FILE_DIR + PDF_DIR);
        if (!pdfDir.exists()){
            pdfDir.mkdirs();
        }
        return pdfDir.getAbsolutePath();
    }

    public static String getBookDIR() {
        File bookDir = new File(Environment.getExternalStorageDirectory() + FILE_DIR + Book_DIR);
        if (!bookDir.exists()){
            bookDir.mkdirs();
        }
        return bookDir.getAbsolutePath();
    }

    public static String getTopicDIR() {
        File topicDir = new File(Environment.getExternalStorageDirectory() + FILE_DIR + Topic_DIR);
        if (!topicDir.exists()){
            topicDir.mkdirs();
        }
        return topicDir.getAbsolutePath();
    }

    /**
     * 动态获取读写权限
     */
    public static void checkPermission(Activity activity) {

        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //没有写sd卡权限
            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    102);
        }
    }

    /**
     * editor: ChangHao
     *
     * @return 当前的时间的字符串形式
     */
    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
    }

    /**
     * 保存bitmap
     * @param whichFileDir 保存在哪个文件夹内
     * @param id 图片对应的题目（或错题本）id
     * @param whichImage 图片类型
     * @param bitmap 图片bitmap
     * @return 保存的路径
     */
    public static String saveBitmapFile(String whichFileDir,int id,int position,String whichImage,Bitmap bitmap) {
        File nomedia;
        File imageFile;
        try {

            //创建.nomedia文件，避免图片资源泄露在系统图库当中
            nomedia = new File(whichFileDir + "/.nomedia" );

            imageFile = new File(whichFileDir + "/id-"+id+"-"+whichImage+"-"+position+".jpeg");

            if (!nomedia.exists()){
                nomedia.createNewFile();
            }

            if (!imageFile.exists()){
                imageFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(imageFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return imageFile.getAbsolutePath();
    }

    /**
     * oldPath 和 newPath必须是新旧文件的绝对路径
     * */
    public static void renameFile(String oldPath, String newPath) {
        if(TextUtils.isEmpty(oldPath)) {
            return;
        }

        if(TextUtils.isEmpty(newPath)) {
            return;
        }

        File file = new File(oldPath);
        if (file.exists()){
            file.renameTo(new File(newPath));
        }
    }

    /**
     * 根据Uri获取文件
     *
     * @param context
     * @param uri
     * @return file
     * @author qjj
     */
    public static File getFile(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        ContentResolver resolver = context.getContentResolver();
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
            if (pfd == null) {
                return null;
            }
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);


            File outputDir = context.getCacheDir();
            File outputFile = File.createTempFile("image", "tmp", outputDir);
            String tempFilename = outputFile.getAbsolutePath();
            output = new FileOutputStream(tempFilename);

            int read;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }

            return new File(tempFilename);
        } catch (Exception ignored) {

            ignored.getStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (Throwable t) {
                // Do nothing
            }
        }
        return null;
    }

    public static Uri getUri(Context context, File file) {

        if (context == null || file == null) {
            throw new NullPointerException();
        }
        //判断版本
        if (Build.VERSION.SDK_INT >= 24) {
            //如果在Android7.0以上,使用FileProvider获取Uri
            try {
                return FileProvider.getUriForFile(context.getApplicationContext(), "com.luckyxmobile.correction.fileprovider", file);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            //否则使用Uri.fromFile(file)方法获取Uri
            return Uri.fromFile(file);
        }

        return null;
    }


    /**
     * @param filepath //文件路径
     * @param context  //执行删除操作活动的context
     * @author:lyw
     */
    public static void deleteFile(String filepath, Context context) {
        if (TextUtils.isEmpty(filepath) || filepath.equals("null")) {
            //如果删除路径为空，不执行删除操作
            return;
        }
        try {
            File file = new File(filepath);
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = context.getContentResolver();
            String where = MediaStore.Images.Media.DATA + "='" + filepath + "'";
            //删除缩略图
            mContentResolver.delete(uri, where, null);
            //删除图片
            file.delete();
        } catch (Exception e) {
            Log.e(TAG, "deleteFile: 图片路径不存在！" + e.getMessage());
        }
    }

    /**
     * 级联删除错题本内所有的图片
     *
     * @author zc
     */
    public static void cascadeDeleteBook(int bookId, Context context) {
        //获取要删除的错题本对象
        Book deleteBook = LitePal.find(Book.class, bookId);
        //删除封面的本地图片
        deleteFile(deleteBook.getBook_cover(), context);

        List<Topic> topicList = LitePal.where("book_id=?", String.valueOf(bookId)).find(Topic.class);

        //删除错题本下的所有错题的图片
        for (Topic deleteTopic : topicList) {
            cascadeDeleteTopic(deleteTopic.getId(),context);
        }
    }

    /**
     * 级联删除错题内所有的图片
     * @author zc
     */
    public static void cascadeDeleteTopic(int topicId, Context context) {
        //要删除的错题对象
        Topic deleteTopic = LitePal.find(Topic.class, topicId);
        TopicImagesPaint topicImagesPaint = FastJsonUtil.jsonToObject(deleteTopic.getTopic_original_picture(), TopicImagesPaint.class);
        if (topicImagesPaint != null){
            topicImagesPaint.removeAllImage(context);
        }

        topicImagesPaint = FastJsonUtil.jsonToObject(deleteTopic.getTopic_right_solution_picture(), TopicImagesPaint.class);
        if (topicImagesPaint != null){
            topicImagesPaint.removeAllImage(context);
        }

        topicImagesPaint = FastJsonUtil.jsonToObject(deleteTopic.getTopic_error_solution_picture(), TopicImagesPaint.class);
        if (topicImagesPaint != null){
            topicImagesPaint.removeAllImage(context);
        }

        topicImagesPaint = FastJsonUtil.jsonToObject(deleteTopic.getTopic_error_cause_picture(), TopicImagesPaint.class);
        if (topicImagesPaint != null){
            topicImagesPaint.removeAllImage(context);
        }

        topicImagesPaint = FastJsonUtil.jsonToObject(deleteTopic.getTopic_knowledge_point_picture(), TopicImagesPaint.class);
        if (topicImagesPaint != null){
            topicImagesPaint.removeAllImage(context);
        }
    }


    /**
     * 将path按照“,”切割，获取最终的路径的列表
     * @param path 需要处理的路径
     * @return 返回最终的路径列表，路径不存在则返回null
     * @author zc
     */
    public static List<String> handlePath(String path) {
        List<String> resultPaths = new ArrayList<String>();
        if (!TextUtils.isEmpty(path) && path != "") {//路径非空时处理

            //将切割出来的数组转为列表
            Collections.addAll(resultPaths, path.split(","));
            Log.d("SDCardUtil(handlePath)", resultPaths.toString());
            return resultPaths;
        }
        //路径为空时返回null
        return resultPaths;
    }

    /**
     * 获取cache路径
     *
     * @param context
     * @return
     */
    public static String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            return context.getExternalCacheDir().getPath();
        } else {
            return context.getCacheDir().getPath();
        }
    }

    /**
     * 获取app 版本号
     * @param context
     * @return
     */
    public static String packageName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }

}
