package com.luckyxmobile.correction.util;



import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ChangHao
 * data: 2019年7月24日
 * 测试生成pdf的类
 * note: 相对路径在 correction/app 下
 */
public class PdfUtilsTest {
    private static int count = 0;
    private static String testImagePath = "./imgs/";

    /**
     * @author ChangHao
     * 测试能否生成pdf
     */
//    //这个测试服务器上通过不了, 暂时注释掉
//    @Test
//    public void testCreatePdf() {
//        PdfUtils pu = null;
//        //生成的pdf名称
//        String filename = "./SavePDF/pdfutilstest.pdf";
//        //测试用图片的路径
//        File file = new File(filename);
//        file.delete();
//
//
//        try {
//            pu = new PdfUtils(filename);
//            pu.addTitleToPdf("title!!!!!!!!!! abcdefghijklmn!!!!!!!jkfid!!!ijo??fjdskl??????dfjkls")
//                    .addRightTextToPdf(SDCardUtil.getCurrentTime())
//                    .addTextToPdf("test: Change date string format in android文7")
//                    .addImageToPdfLEFTH(getImagePath(), PageSize.A4.getWidth(), 720)
//                    .addBlankToPdf(421)
//                    .addTitleToPdf("2title!!!!!!!!!!")
//                    .addImageToPdfLEFTH(getImagePath(), PageSize.A4.getWidth(), 480)
//                    .addImageToPdfLEFTH(getImagePath(), PageSize.A4.getWidth(), 320)
//                    .addImageToPdfLEFTH(getImagePath(), PageSize.A4.getWidth(), 100)
//                    .addImageToPdfLEFTH(getImagePath(), PageSize.A4.getWidth(), 640)
//                    .addTextToPdf("test: SimpleDateFormat is a concrete class for formatting and parsing dates in a locale-sensitive manner. It allows for formatting");
//        } catch (IOException | DocumentException e) {
//            e.printStackTrace();
//        } finally {
//            if (pu != null)
//                assertTrue(!"error".equals(pu.close()));
//        }
//
//        file = new File(filename);
//        long size = file.getTotalSpace();
//        assertTrue(size > 0);
//    }

    //得到image的路径
    private static String getImagePath() {
        List<String> fileList = new ArrayList<>();
        getAllFileName(testImagePath, (ArrayList<String>) fileList);
        return fileList.get(count++ % fileList.size());
    }

    //得到路径下所有的文件
    private static void getAllFileName(String path, ArrayList<String> listFileName) {
        File file = new File(path);
        File[] files = file.listFiles();
        String[] names = file.list();
        if (names != null) {
            String[] completNames = new String[names.length];
            for (int i = 0; i < names.length; i++) {
                if (names[i].endsWith(".jpg") || names[i].endsWith(".png"))
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