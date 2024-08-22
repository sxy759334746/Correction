package com.luckyxmobile.correction.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * @author qujiajun
 * copyedBy ChangHao
 * 调用系统自带的pdf打印预览
 */
public class PrintPreviewAdapter extends PrintDocumentAdapter {

    private Context context;
    private PdfDocument mPdfDocument;
    private String pdfPath;
    private int pageHeight;
    private int pageWidth;
    private int totalPages = 1;//设置一共打印一张纸
    private List<Bitmap> mlist;

    public PrintPreviewAdapter(Context context, String pdfPath) {
        this.context = context;
        this.pdfPath = pdfPath;
    }

    @Override
    public void onFinish() {
        File file = new File(pdfPath);
        if (file.exists() && !file.delete()) {
            Log.d(TAG, "onFinish: 文件删除失败");
        }
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal,
                         LayoutResultCallback callback,
                         Bundle metadata) {

        //创建可打印PDF文档对象
        mPdfDocument = new PrintedPdfDocument(context, newAttributes);

        //设置尺寸
        pageHeight = newAttributes.getMediaSize().ISO_A4.getHeightMils() * 72 / 1000;
        pageWidth = newAttributes.getMediaSize().ISO_A4.getWidthMils() * 72 / 1000;

        //取消申请
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        ///计算打印页面的预期数量
        ParcelFileDescriptor mFileDescriptor = null;

        PdfRenderer pdfRender = null;

        PdfRenderer.Page page = null;

        try {
            mFileDescriptor = ParcelFileDescriptor.open(new File(pdfPath), ParcelFileDescriptor.MODE_READ_ONLY);

            if (mFileDescriptor != null) {
                pdfRender = new PdfRenderer(mFileDescriptor);
            }

            mlist = new ArrayList<>();

            if (pdfRender.getPageCount() > 0) {
                totalPages = pdfRender.getPageCount();
                for (int i = 0; i < pdfRender.getPageCount(); i++) {
                    if (null != page) {
                        page.close();
                    }
                    page = pdfRender.openPage(i);
                    Bitmap bmp = Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_8888);
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    mlist.add(bmp);
                }
            }
            if (null != page) {
                page.close();
            }

            if (null != mFileDescriptor) {
                mFileDescriptor.close();
            }

            pdfRender.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (totalPages > 0) {
            //将打印信息返回打印框架
            PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                    .Builder("correction.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalPages);  //构建文档配置信息


            PrintDocumentInfo info = builder.build();
            callback.onLayoutFinished(info, true);
        } else {
            //否则向打印框架报告错误
            callback.onLayoutFailed("Page count calculation failed.");
        }

    }

    @Override
    public void onWrite(final PageRange[] pageRanges, final ParcelFileDescriptor destination,
                        final CancellationSignal cancellationSignal,
                        final WriteResultCallback callback) {

        //迭代文档的每一页，
        //检查它是否在输出范围内。
        for (int i = 0; i < totalPages; i++) {

            if (pageInRange(pageRanges, i))//保证页码正确
            {
                //创建新页面
                PdfDocument.PageInfo newPage = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i).create();
                PdfDocument.Page page = mPdfDocument.startPage(newPage);

                //取消信号
                if (cancellationSignal.isCanceled()) {
                    callback.onWriteCancelled();
                    mPdfDocument.close();
                    mPdfDocument = null;
                    return;
                }

                //将内容绘制到页面Canvas上
                drawPage(page, i);

                // Rendering is complete, so page can be finalized.
                mPdfDocument.finishPage(page);
            }
        }

        // Write PDF document to file
        try {
            mPdfDocument.writeTo(new FileOutputStream(
                    destination.getFileDescriptor()));
        } catch (IOException e) {
            callback.onWriteFailed(e.toString());
            return;
        } finally {
            mPdfDocument.close();
            mPdfDocument = null;
        }

        // Signal the print framework the document is complete
        callback.onWriteFinished(pageRanges);
    }

    private boolean pageInRange(PageRange[] pageRanges, int page) {
        for (PageRange pageRange : pageRanges) {
            if ((page >= pageRange.getStart()) && (page <= pageRange.getEnd())) {
                return true;
            }
        }
        return false;
    }

    //页面绘制（渲染）
    private void drawPage(PdfDocument.Page page, int pagenumber) {

        Canvas canvas = page.getCanvas();
        if (mlist != null) {
            Paint paint = new Paint();
            Bitmap bitmap = mlist.get(pagenumber);
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            // 计算缩放比例
            float scale = (float) pageWidth / (float) bitmapWidth;
            // 取得想要缩放的matrix参数
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            canvas.drawBitmap(bitmap, matrix, paint);
        }

    }

}
