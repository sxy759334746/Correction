package com.luckyxmobile.correction.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.adapter.SelectPaperAdapter;
import com.luckyxmobile.correction.bean.Paper;
import com.luckyxmobile.correction.dao.PaperTopicDao;
import com.luckyxmobile.correction.dao.impl.CorrectionLab;
import com.luckyxmobile.correction.dao.impl.PaperTopicDaoImpl;
import com.luckyxmobile.correction.util.DestroyActivityUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * 选择错题本的界面
 * @author deh
 * */

public class SelectPaperActivity extends AppCompatActivity {
    private Toolbar reviewPaperToolBar;
    private RecyclerView recyclerview;
    private SelectPaperAdapter adapter;
    private List<Paper> AllDatas;
    private PaperTopicDao paper_topic_dao = new PaperTopicDaoImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_paper_select);

        //设置ToolBar
        reviewPaperToolBar = findViewById(R.id.review_paper_toolbar);
        setSupportActionBar(reviewPaperToolBar);
        ActionBar actionBar = getSupportActionBar();
        reviewPaperToolBar.setTitle(R.string.paper_select);
        if (actionBar != null){
            //设置ToolBar返回图标
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        reviewPaperToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initData();
        recyclerview = (RecyclerView) findViewById(R.id.review_page_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SelectPaperActivity.this);
        recyclerview.setLayoutManager(layoutManager);
        adapter = new SelectPaperAdapter();
        adapter.setDatas(AllDatas);
        recyclerview.setAdapter(adapter);

        adapter.setmOnItemClickListener(new SelectPaperAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //获取来自SelectTopicActivity的错题列表
                Intent topicIdIntent = getIntent();
                ArrayList<Integer> topic_ids = topicIdIntent.getIntegerArrayListExtra("topic_ids");
                //存入Paper_Topic表
                paper_topic_dao.save(AllDatas.get(position).getId(),topic_ids);
                Intent intent = new Intent(SelectPaperActivity.this, PaperDetailActivity.class);
                intent.putExtra("reviewPaperId", AllDatas.get(position).getId() + "");
                startActivity(intent);
                DestroyActivityUtil.destroyActivityALL();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.actionbar_menu,menu);
//          //修改菜单字体颜色为白色
//        MenuItem item = menu.findItem(R.id.new_review_page);
//        SpannableString spannableString = new SpannableString(item.getTitle());
//        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white)),
//                0,spannableString.length(),0);
//        item.setTitle(spannableString);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.new_review_page:
                //新建复习卷
                addReviewPageDialog();
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return true;
    }

    //新建复习卷的Dialog
    private void addReviewPageDialog(){
        LayoutInflater layoutInflater = LayoutInflater.from(SelectPaperActivity.this);
        View editReviewPageDialog = layoutInflater.inflate(R.layout.dialog_add_paper, null);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(SelectPaperActivity.this);
        builder.setTitle(R.string.add_review_page);
        builder.setView(editReviewPageDialog);

        //获得控件
        final EditText ReviewPageNameET = (EditText) editReviewPageDialog.
                findViewById(R.id.add_page_text);
        final TextView ReviewPageETNum = (TextView) editReviewPageDialog.
                findViewById(R.id.add_page_text_hint);
        ReviewPageNameET.setHint(R.string.review_page);

        //输入框字数提示和限制
        ReviewPageNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ReviewPageETNum.setText(s.length()+"/12");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ReviewPageETNum.setText(s.length()+"/12");
            }
        });

        //设置对话框的确定，取消事件
        builder.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //获得输入的字符串
                final String pageName = ReviewPageNameET.getText().toString();
                //判断名字是否为空
                if (!"".equals(pageName.trim())) {
                    //把用户输入的数据传入对象
                    CorrectionLab.addPaper(pageName);
                    Toasty.success(SelectPaperActivity.this, R.string.successful, Toast.LENGTH_SHORT).show();
                    //更新列表
                    initData();
                    adapter.setDatas(AllDatas);
                    adapter.notifyDataSetChanged();

                } else {
                    Toasty.warning(SelectPaperActivity.this, R.string.empty_input, Toast.LENGTH_SHORT).show();
                }
            }

        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create();
        builder.show();
    }

    /**初始化复习卷名称*/
    public List<Paper> initData(){
        //将查询到的数据存到AllDatas 方便页面跳转时拿到对应复习卷id
        AllDatas = LitePal.findAll(Paper.class);
        return AllDatas;
    }

}
