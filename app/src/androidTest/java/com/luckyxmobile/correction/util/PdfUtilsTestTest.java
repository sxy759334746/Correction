package com.luckyxmobile.correction.util;

import androidx.test.runner.AndroidJUnit4;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.luckyxmobile.correction.util.PdfUtils;
import com.luckyxmobile.correction.util.SDCardUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PdfUtilsTestTest {

    private static int count = 0;
    private static String testImagePath = SDCardUtil.getTopic_DIR() + "/";

    /**
     * @author ChangHao
     * 测试能否生成pdf
     */
    @Test
    public void testCreatePdf() {
        PdfUtils pu = null;
        //生成的pdf名称
        String filepath = SDCardUtil.getPdfDir() + "/";
        //测试用图片的路径
        File file = new File(filepath);
        file.delete();
        String filename = null;


        try {
            pu = new PdfUtils(filepath);
            pu.addTitleToPdf("title!!!!!!!!!!中文")
                    .addTextToPdf("test: 英文要换 中文中文中文中文中文7")
                    .addImageToPdfLEFTH(getImagePath(), PageSize.A4.getWidth(), 720)
                    .addBlankToPdf(421)
                    .addTitleToPdf("2title!!!!!!!!!!")
                    .addImageToPdfLEFTH(getImagePath(), PageSize.A4.getWidth(), 480)
                    .addImageToPdfLEFTH(getImagePath(), PageSize.A4.getWidth(), 320)
                    .addImageToPdfLEFTH(getImagePath(), PageSize.A4.getWidth(), 100)
                    .addImageToPdfLEFTH(getImagePath(), PageSize.A4.getWidth(), 640)
                    .addTextToPdf("test: 中文中文中文中文中文7");
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        } finally {
            if (pu != null) {
                filename = pu.close();
                assertNotEquals("error", filename);
            }
        }
        assertNotNull(filename);
        file = new File(filepath + filename);
        long size = file.getTotalSpace();
        assertTrue(size > 0);
        file.delete();
    }

    @Test(expected = IOException.class)
    public void testIOException() throws IOException, DocumentException {
        PdfUtils pu = new PdfUtils("文件不存在");
    }


    //得到image的路径
    public static String getImagePath() {
        List<String> fileList = new ArrayList<>();
        getAllFileName(testImagePath, (ArrayList<String>) fileList);
        return fileList.get(count++ % fileList.size());
    }

    //得到路径下所有的文件
    public static void getAllFileName(String path, ArrayList<String> listFileName) {
        File file = new File(path);
        File[] files = file.listFiles();
        String[] names = file.list();
        if (names != null) {
            String[] completNames = new String[names.length];
            for (int i = 0; i < names.length; i++) {
                if (names[i].endsWith(".jpg") || names[i].endsWith(".png") || names[i].endsWith(".jpeg"))
                    completNames[i] = path + names[i];
            }
            listFileName.addAll(Arrays.asList(completNames));
        }
        for (File a : files) {
            if (a.isDirectory()) {//如果文件夹下有子文件夹，获取子文件夹下的所有文件全路径。
                getAllFileName(a.getAbsolutePath() + "\\", listFileName);
            }
        }
    }
}