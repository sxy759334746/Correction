package com.luckyxmobile.correction.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.adapter.PaperAdapter;
import com.luckyxmobile.correction.bean.Paper;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.impl.PaperTopicDaoImpl;
import com.luckyxmobile.correction.util.PdfUtils;
import com.luckyxmobile.correction.util.PhotoUtil;
import com.luckyxmobile.correction.util.ProgressDialogUtil;
import com.luckyxmobile.correction.util.SDCardUtil;
import com.luckyxmobile.correction.util.ThreadPool;

import org.litepal.LitePal;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class PaperActivity extends AppCompatActivity{

    private RecyclerView review_recyclerview;
    private PaperAdapter reviewAdapter;
    private TextView add_paper_main;
    private List<Paper> paperList;
    private Toolbar toolbar;
    private String paper_name;
    private int paper_id;
    private final static String TAG = "FragmentPaper";
    private final static int PROGRESSDIALOG_DISMISS = 1;
    private AlertDialog.Builder mChangeBookDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper);

        /*将Toolbar设置为标题栏*/
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            // 显示返回键
            actionBar.setDisplayHomeAsUpEnabled(true);
            // 不显示默认标题
            actionBar.setDisplayShowTitleEnabled(true);
        }
       //设置返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }

        });

        initData();
        add_paper_main = findViewById(R.id.add_paper_main);
        review_recyclerview = findViewById(R.id.review_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        review_recyclerview.setLayoutManager(layoutManager);
        reviewAdapter = new PaperAdapter();
        reviewAdapter.setDatas(paperList);
        review_recyclerview.setAdapter(reviewAdapter);
        //点击事件
        reviewAdapter.setmOnItemClickListener(new PaperAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                paper_name = paperList.get(position).getPaper_name();
                Intent reviewPaperDetailIntent = new Intent(PaperActivity.this, PaperDetailActivity.class);
                reviewPaperDetailIntent.putExtra("reviewPaperId", paperList.get(position).getId() + "");

                startActivity(reviewPaperDetailIntent);
            }
        });
//        //长按点击事件
        reviewAdapter.setmOnItemLongClickListener(new PaperAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                reviewAdapter.setIndex(position);
                deletePaper(position);

            }
        });

        reviewAdapter.setPaperMenuClickListener(new PaperAdapter.OnPaperMenuClickListener() {
            @Override
            public void paperMenuClick(View v, int position) {
                paperMoreClick(v,position);
            }
        });

        //创建复习卷
        add_paper_main.setText(R.string.add_review_page);
        add_paper_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LitePal.deleteAll(Topic.class, "book_id = ?", "0");
                Intent intent = new Intent(PaperActivity.this, SelectTopicActivity.class);
                intent.putExtra("from", "MainActivity");
                startActivity(intent);
//                addPaperDialog();
            }
        });

    }

    @Override
    public void onResume() {
//        toolbar.setTitle("");
        super.onResume();
        initData();
        reviewAdapter.setDatas(paperList);
        reviewAdapter.notifyDataSetChanged();
        ProgressDialogUtil.dismiss();
    }

    //初始化复习卷名称
    public List<Paper> initData(){
        paperList = LitePal.findAll(Paper.class);
        return paperList;
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESSDIALOG_DISMISS:
                    ProgressDialogUtil.dismiss();
                    break;

                default:
                    break;
            }
            return false;

        }
    });


    private void paperMoreClick(View view, final int position) {
        final Paper paper = paperList.get(position);
        PopupMenu popupMenu = new PopupMenu(PaperActivity.this,view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_pop, popupMenu.getMenu());//2.加载Menu资源
        //3.为弹出菜单设置点击监听
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.share:
                        ProgressDialogUtil.showProgressDialog(PaperActivity.this);
                        ThreadPool.singleThreadExecutor.execute(
                                ThreadPool.runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            PdfUtils.sharePdfUris(PaperActivity.this, paperList.get(position));
                                        } catch (IllegalArgumentException e){
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toasty.warning(PaperActivity.this, R.string.paper_empty_cant_share, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                        handler.sendEmptyMessage(PROGRESSDIALOG_DISMISS);
                                    }
                                }
                        );
                        return true;
                    case R.id.print:
                        ProgressDialogUtil.showProgressDialog(PaperActivity.this);
                        ThreadPool.singleThreadExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    PdfUtils.printPreviewWindow(PaperActivity.this, paperList.get(position));
                                }catch (IllegalArgumentException e){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toasty.warning(PaperActivity.this, R.string.paper_empty_cant_print, Toast.LENGTH_SHORT).show();
                                            ProgressDialogUtil.dismiss();
                                        }
                                    });
                                }
                            }
                        });
                        return true;

                    case R.id.rename:
                        renamePaper(paper);
                        return  true;


                    case R.id.delete:
                        deletePaper(position);

                    default:
                        return false;
                }
            }
        });
        popupMenu.show();//4.显示弹出菜单
    }


    /**
     * 重命名复习卷
     */
    private void renamePaper(final Paper paper) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(PaperActivity.this);
        View view = LayoutInflater.from(PaperActivity.this).inflate(R.layout.dialog_add_paper,null);
        dialog.setView(view);
        final EditText inputText = view.findViewById(R.id.add_page_text);
        final TextView inputTextNum = (TextView) view.findViewById(R.id.add_page_text_hint);
        inputText.setText(paper.getPaper_name());
        inputText.setSelection(paper.getPaper_name().length());
        inputTextNum.setText(paper.getPaper_name().length() + "/12");

        //输入框字数提示和限制
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                inputTextNum.setText(s.length()+"/12");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputTextNum.setText(s.length()+"/12");
            }
        });

        dialog.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = inputText.getText().toString();

                if (newName.length() >0) {
                    Log.d(TAG, "onClick: "+newName);
                    paper.setPaper_name(newName);
                    Paper newPaper = new Paper();
                    newPaper.setPaper_name(newName);
                    newPaper.update(paper.getId());
                    reviewAdapter.notifyDataSetChanged();

                }else {
                    Toasty.warning(PaperActivity.this,R.string.empty_input,Toast.LENGTH_SHORT).show();
                }


            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setTitle(R.string.rename);

        dialog.create();
        dialog.show();

    }

    private void deletePaper(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaperActivity.this);
        builder.setIcon(R.drawable.ic_delete_red_24dp).setTitle(R.string.confirm_delete);
        builder.setMessage(R.string.confirm_delete_paper);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                paper_id = paperList.get(position).getId();
                LitePal.delete(Paper.class,paper_id);
                PaperTopicDaoImpl paper_topic_dao = new PaperTopicDaoImpl();
                paper_topic_dao.deleteByPaperId(paper_id);
                initData();
                reviewAdapter.setDatas(paperList);
                reviewAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel,null);
        builder.create().show();
    }


    //添加复习卷
    private void addPaperDialog() {
        View view =  LayoutInflater.from(PaperActivity.this).inflate(R.layout.dialog_add_paper,null);
        final EditText paperNameEdt = view.findViewById(R.id.add_page_text);
        final TextView paperNameNum = (TextView) view.findViewById(R.id.add_page_text_hint);

        //输入框字数提示和限制
        paperNameEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                paperNameNum.setText(s.length()+"/10");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                paperNameNum.setText(s.length()+"/10");
            }
        });

        mChangeBookDialog = new AlertDialog.Builder(PaperActivity.this);


        mChangeBookDialog.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String paperName = paperNameEdt.getText().toString();
                //保存创建
                if (paperName.length() <= 0) {

                    Toasty.warning(PaperActivity.this,R.string.empty_input, Toast.LENGTH_SHORT, true).show();
                } else {
                    Paper newPaper = new Paper();
                    newPaper.setPaper_name(paperNameEdt.getText().toString());
                    //插入到litepal数据库
                    newPaper.save();
//                    mBookList.add(newBook);
//                    mBookAdapter.notifyDataSetChanged();
                    reviewAdapter.addPaper(newPaper);
                    reviewAdapter.notifyDataSetChanged();
                    Toasty.success(PaperActivity.this, R.string.successful, Toast.LENGTH_SHORT, true).show();

                }
            }
        });
        mChangeBookDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });

        mChangeBookDialog.setView(view);
        mChangeBookDialog.create();
        mChangeBookDialog.show();
    }

}
