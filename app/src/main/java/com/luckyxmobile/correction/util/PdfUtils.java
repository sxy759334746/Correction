package com.luckyxmobile.correction.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.util.Log;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.adapter.PaperDetailAdapter;
import com.luckyxmobile.correction.adapter.PrintPreviewAdapter;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Paper;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.bean.TopicImagesPaint;
import com.luckyxmobile.correction.dao.PaperTopicDao;
import com.luckyxmobile.correction.dao.impl.CorrectionLab;
import com.luckyxmobile.correction.dao.impl.PaperTopicDaoImpl;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.PngImage;

import org.litepal.LitePal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.luckyxmobile.correction.util.SDCardUtil.getDiskCachePath;

/**
 * @author ChangHao
 * data: 2019年7月24日
 * 用于生成pdf
 * close的时候可以获取pdf的文件名, 然后通过SDCardUtil获取pdf的存储路径
 * 两个相加就是pdf文件的路径, 注意 '/'
 */
public class PdfUtils {
    private static Document document;
    private final static String TAG = "PdfUtils";
    //左和右的margin
    private float documentMarginLR = 50;
    //top和bottom的margin
    private float documentMarginTB = 30;

    private String filename;
    //文件的路径+名字
    private String filePathName;
    private static BaseFont chinese;
    private static BaseFont english;
    private static Font BoldChinese;
    private static Font NormalChinese;
    private int titleFontSize = 20;
    private int normalFontSize = 16;

    //字间距
    private float CharacterSpace = 1.5f;

    /**
     * @param filepath 保存的pdf的路径 理应为系统的临时文件目录
     * @throws DocumentException on error
     */
    public PdfUtils(String filepath) throws DocumentException, IOException {
        File file = new File(filepath);

        //创建新的PDF文档：A4大小
        document = new Document(PageSize.A4, documentMarginLR, documentMarginLR, documentMarginTB, documentMarginTB);
        //如果路径不存在则创建该路径
        if (!file.exists()) {
            file.mkdir();
        }

        //获取PDF书写器
        filename = "pdf_" + SDCardUtil.getCurrentTime() + ".pdf";
        this.filePathName = filepath + filename;
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(filePathName));
        pdfWriter.setStrictImageSequence(true);
        //打开文档
        document.open();
        initFonts();
    }

    private void initFonts() throws IOException, DocumentException {
        english = BaseFont.createFont();
        chinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        BoldChinese = new Font(chinese, titleFontSize, Font.BOLD);
        NormalChinese = new Font(chinese, normalFontSize, Font.NORMAL);
    }

    /**
     * 关闭并保存pdf
     *
     * @return error表示关闭失败 否则就是filename表示关闭成功
     */
    public String close() {
        if (document.isOpen()) {
            document.close();
            return filePathName;
        } else {
            Log.d(TAG, "close: failed have not opened a document");
            return "error";
        }
    }

    public PdfUtils addPngToPdf(InputStream inputStream) throws DocumentException, IOException {
        Image img = PngImage.getImage(inputStream);
        img.setAlignment(Element.ALIGN_LEFT);

        //添加到PDF文档
        document.add(img);
        return this;
    }

    // 添加文本到pdf中
    public PdfUtils addTextToPdf(String content) throws DocumentException {
        Paragraph elements = new Paragraph(content, new Font(chinese, 12, Font.NORMAL));
        elements.setAlignment(Element.ALIGN_BASELINE);
        document.add(elements);
        return this;
    }

    /**
     * @param Height 空白的大小  相对于A4纸的大小 note: A4的高度为842
     * @throws DocumentException on error
     * @author Changhao
     * 给pdf添加空白
     */
    public PdfUtils addBlankToPdf(Integer Height) throws DocumentException {
        Paragraph elements = new Paragraph(" ", NormalChinese);

        //设置行间距
        elements.setLeading(Height);
        document.add(elements);
        return this;
    }

    /**
     * 生成一个有字间距的chunk
     *
     * @param content 文字内容
     * @param font    使用的字体
     * @return chunk
     */
    private Chunk getParFromChunk(String content, Font font) {
        Chunk chunk = new Chunk(content, font);
        chunk.setCharacterSpacing(CharacterSpace);
        return chunk;
    }

    // 给pdf添加标题，居中黑体
    public PdfUtils addTitleToPdf(String title) throws DocumentException {
        Paragraph elements = new Paragraph();

        elements.add(getParFromChunk(title, BoldChinese));
        //设置居中对齐
        elements.setAlignment(Element.ALIGN_CENTER);
        document.add(elements); // result为保存的字符串
        return this;
    }

    public PdfUtils addCenterTextToPdf(String title) throws DocumentException {
        Paragraph elements = new Paragraph(title, NormalChinese);
        //设置居中对齐
        elements.setAlignment(Element.ALIGN_CENTER);
        document.add(elements); // result为保存的字符串
        return this;
    }

    public PdfUtils addRightTextToPdf(String title) throws DocumentException {
        Paragraph elements = new Paragraph(title, NormalChinese);
        //设置居中对齐
        elements.setAlignment(Element.ALIGN_RIGHT);
        document.add(elements); // result为保存的字符串
        return this;
    }

    //给pdf按照左对齐的方式添加图片
    public PdfUtils addImageToPdfLEFTH(String imgPath, float imgWidth, float imgHeight) throws IOException, DocumentException {
        float maxWidth = PageSize.A4.getWidth() - 2 * documentMarginLR;
        //获取图片
        Bitmap bitmap = ImageUtil.file2Bitmap(imgPath);
//        bitmap = ImageUtil.convertThreshod(bitmap);
        ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream3);
        Image img = Image.getInstance(stream3.toByteArray());
        //设置左对齐
        img.setAlignment(Element.ALIGN_LEFT);
        //宽度要适中,如果比A4的宽, 宽度就减去两倍的margin大,就是它的最大值
        img.scaleToFit(imgWidth > maxWidth ? maxWidth : imgWidth, imgHeight);

        Paragraph imgParagraph = new Paragraph();
        imgParagraph.add(img);
        //添加到PDF文档
        document.add(imgParagraph);
        return this;
    }
    //给pdf按照左对齐的方式添加图片
    public PdfUtils addImageToPdfLEFTH(Bitmap bitmap, float imgWidth, float imgHeight) throws IOException, DocumentException {
        float maxWidth = PageSize.A4.getWidth() - 2 * documentMarginLR;
//        bitmap = ImageUtil.convertThreshod(bitmap);
        ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream3);
        Image img = Image.getInstance(stream3.toByteArray());
        //设置左对齐
        img.setAlignment(Element.ALIGN_LEFT);
        //宽度要适中,如果比A4的宽, 宽度就减去两倍的margin大,就是它的最大值
        img.scaleToFit(imgWidth > maxWidth ? maxWidth : imgWidth, imgHeight);

        Paragraph imgParagraph = new Paragraph();
        imgParagraph.add(img);
        //添加到PDF文档
        document.add(imgParagraph);
        return this;
    }

    /**
     * @param title          创建pdf的标题
     * @return 生成的pdf的存储路径
     */
    public static String CreatePaperPdf(Context context, String title, String pattern, String CacheDir, List<Topic> topicList, List<Book> booksList) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);

        PdfUtils pu = null;
        //生成的pdf名称
//        String filepath = this.getCacheDir().getAbsolutePath() + "/";
        String filepath = CacheDir + "/";
        String filename = null;

        //复习卷不能为空
        if(topicList.size() == 0){
            throw new IllegalArgumentException("topicList can not be empty");
        }
        try {
            int count = 0;
            Bitmap bitmap = null;
            pu = new PdfUtils(filepath);
            pu.addTitleToPdf(title);
            pu.addRightTextToPdf(format.format(new Date()));

            for(Topic topic : topicList){
                bitmap = PhotoUtil.convertTopicImageByWhichs(context, topic.getId(), null,0);
                //给每一道题添加标题
                pu.addTextToPdf(count + 1 + "  • " + PaperDetailAdapter.getBookName(count, booksList, topicList));
                //添加被白色涂抹过的图片 如果想要打印多种类型， 修改which
                pu.addImageToPdfLEFTH(bitmap, bitmap.getWidth(), 480);
                //添加手敲的补充的原题题干
                pu.addTextToPdf(topicList.get(count).getTopic_original_text());
                count++;
            }
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        } finally {
            if (pu != null) {
                filename = pu.close();
            }
        }
        return filename;
    }

    public static void printPreviewWindow(final Context context,
                                          final List<Topic> topicList,
                                          final List<Book> booksList,
                                          final Paper paper) {
        //调用android自带的pdf预览器
        final PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        final PrintAttributes.Builder builder = new PrintAttributes.Builder();
        //设置色彩模式，黑白或者彩色
        builder.setColorMode(PrintAttributes.COLOR_MODE_COLOR);

        printManager.print(String.valueOf(R.string.app_name), new PrintPreviewAdapter(context,
                CreatePaperPdf(context,paper.getPaper_name(),
                        context.getString(R.string.date_pattern),
                        context.getCacheDir().getAbsolutePath(),
                        topicList,
                        booksList)), builder.build());

    }

    public static void printPreviewWindow(final Context context,
                                          final Paper paper) {

        PaperTopicDao paper_topic = new PaperTopicDaoImpl();
        List<Topic> topicList = paper_topic.selectPaper(paper.getId());
        List<Book> booksList = LitePal.findAll(Book.class);

        printPreviewWindow(context, topicList, booksList, paper);
    }

    public float getCharacterSpace() {
        return CharacterSpace;
    }

    public void setCharacterSpace(float characterSpace) {
        CharacterSpace = characterSpace;
    }

    public float getDocumentMarginLR() {
        return documentMarginLR;
    }

    public void setDocumentMarginLR(float documentMarginLR) {
        this.documentMarginLR = documentMarginLR;
    }

    public float getDocumentMarginTB() {
        return documentMarginTB;
    }

    public void setDocumentMarginTB(float documentMarginTB) {
        this.documentMarginTB = documentMarginTB;
    }

    /**
     * 根据topicList里的题获取图片列表, 目前只添加了原题题干的,之后可能会添加其他的
     * TODO:add other types
     * @param topicList topicLists
     * @return 可用于迭代的map, 第一层的integer是topic的id, 第二层map是根据 '原题题干','正解','错解'进行分类
     */
//    public static Map<Integer, Map<String, List<String>>> getImagesMap(List<Topic> topicList){
//        ArrayList<String> imageList = new ArrayList<>();
//        Map<Integer, Map<String, List<String>>> ImagesMap = new LinkedHashMap<>();
//        TopicImagesPaint topicImagesPaint;
//        for (Topic imagePath : topicList) {
////            imageList.add(SDCardUtil.handlePath(SDCardUtil.findPhotoPath(imagePath, "original")[1]).get(1));
//            //获取图片在sd卡上的绝对路径
//            topicImagesPaint = FastJsonUtil.jsonToObject(imagePath.getTopic_original_picture(),TopicImagesPaint.class);
//            assert topicImagesPaint != null;
//            List<String> paperOriginal = topicImagesPaint.getOperationalImagesPath();
//            Map<String, List<String>> originalMap = new LinkedHashMap<>();
//            originalMap.put("original", paperOriginal);
//
//            ImagesMap.put(imagePath.getId(), originalMap);
//        }
//        return ImagesMap;
//    }

    /**
     * 分享pdf
     *
     * @param context 上下文
     */
    public static void sharePdfUris(Context context, Paper paper) {
        PaperTopicDao paperTopic = new PaperTopicDaoImpl();
        List<Topic> topicList = paperTopic.selectPaper(paper.getId());
        List<Book> booksList = LitePal.findAll(Book.class);
        ArrayList<String> imageList = new ArrayList<>();

        String cachePath = getDiskCachePath(context);
        String pdfPath = CreatePaperPdf(context,paper.getPaper_name(), context.getString(R.string.date_pattern),
                    cachePath, topicList, booksList);
        Uri pdfUri = SDCardUtil.getUri(context, new File(pdfPath));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.setType("application/pdf");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "分享到"));
    }
}
