package com.luckyxmobile.correction.ui.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Tag;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.impl.TagDaoImpl;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;
import org.litepal.LitePal;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * 处理标签页面（新建、添加、删除）
 * @author qjj
 * @date 2019/07/26
 */
public class TagActivity extends AppCompatActivity {


    /**错题对象id*/
    private int topicId;
    /**选中标签对象*/
    private TagFlowLayout tagLayoutChoose;
    /**全部标签对象*/
    private TagFlowLayout tagLayoutAll;
    /**选中标签集合*/
    private List<Tag> chooseTagList = new ArrayList<>();
    /**选中标签集合（备份）*/
    private List<Tag> chooseTagListStandby = new ArrayList<>();
    /**全部标签集合*/
    private List<Tag> allTagList = new ArrayList<>();
    /**标签对象*/
    private Tag newTag = new Tag();

    /**
     *isDelete
     * 用于判断是否进入删除状态
     * 初始值为false
     */
    private boolean isDelete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        //初始化标题栏
        initToolBar();

        //初始化页面布局
        initView();

        //初始化数据
        initDate();

        //设置全部标签适配器
        initAdapterAll();

        //设置选中标签适配器
        initAdapterChoose();

    }

    /**
     * 设置标题栏
     * 添加退出事件
     */
    private void initToolBar() {

        Toolbar toolbar = findViewById(R.id.add_tag_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.set_tag_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_tag:
                isDelete = !isDelete;
                tagLayoutAll.onChanged();
                break;
            case R.id.add_tag:
                if (isDelete) {
                    isDelete = false;
                    tagLayoutAll.onChanged();
                }
                addNewTag();
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 获取topic_id
     * 绑定布局
     */
    private void initView() {

        topicId = getIntent().getIntExtra("topic_id",0);
        tagLayoutChoose = findViewById(R.id.tag_layout_choose);
        tagLayoutAll = findViewById(R.id.tag_layout_all);

    }

    /**
     * 获取已经选中的标签--chooseTagList
     * 获取全部标签--allTagList
     * 循环 将选中的tag加入--chooseTagListStandby
     * 添加新建按钮和删除按钮
     */
    private void initDate() {

        chooseTagList = TagDaoImpl.findTagByTopic(LitePal.find(Topic.class, topicId).getTopic_tag());

        allTagList = LitePal.findAll(Tag.class);

        for (Tag tag1:allTagList){
            for (Tag tag2 : chooseTagList){
                if (tag1.getId()==tag2.getId()){
                    chooseTagListStandby.add(tag1);
                }
            }
        }


    }


    /**
     * 初始化已选标签的适配器
     * 设置点击事件
     */
    private void initAdapterChoose() {
        tagLayoutChoose.setAdapter(new TagAdapter<Tag>(chooseTagListStandby) {
            @Override
            public View getView(FlowLayout parent, int position, Tag tag) {

                CheckBox checkBox = (CheckBox) LayoutInflater.from(TagActivity.this).inflate
                        (R.layout.flow_item_tag,tagLayoutChoose, false);
                checkBox.setText(tag.getTag_name());

                checkBox.setChecked(true);
                checkBox.setClickable(false);
                checkBox.setTextColor(getColor(R.color.white));

                return checkBox;
            }
        });

        tagLayoutChoose.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                if (isDelete) {
                    isDelete = false;
                    tagLayoutAll.onChanged();
                }
                TagDaoImpl.topicDeleteTag(topicId,chooseTagListStandby.get(position).getId());
                chooseTagListStandby.remove(chooseTagListStandby.get(position));

                tagLayoutChoose.onChanged();
                tagLayoutAll.onChanged();
                return true;
            }
        });
    }

    /**
     * 初始化全部标签适配器-传入数据
     * 设置点击事件
     */
    private void initAdapterAll() {
        tagLayoutAll.setAdapter(new TagAdapter<Tag>(allTagList) {
            @Override
            public View getView(FlowLayout parent, int position, Tag tag) {
                CheckBox checkBox = (CheckBox) LayoutInflater.from(TagActivity.this).inflate
                        (R.layout.flow_item_tag,tagLayoutAll, false);
                checkBox.setText(tag.getTag_name());

                if (chooseTagListStandby.contains(tag) && !isDelete){
                    checkBox.setTextColor(getColor(R.color.white));
                    checkBox.setChecked(true);
                }else{
                    checkBox.setTextColor(getColor(R.color.gray_7d));
                    checkBox.setBackgroundResource(R.drawable.topic_radio_button_unchecked_white);
                    checkBox.setChecked(false);
                }

                if (isDelete){
                    checkBox.setTextColor(Color.GRAY);
                    checkBox.setButtonDrawable(R.drawable.ic_delete_red_24dp);
                }

                return checkBox;
            }
        });

        //设置点击事件
        tagLayoutAll.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, final int position, FlowLayout parent) {

                if (!isDelete){
                    if (!chooseTagListStandby.contains(allTagList.get(position))){
                        chooseTagListStandby.add(0,allTagList.get(position));
                        TagDaoImpl.topicAddTag(topicId,allTagList.get(position).getId());
                    }else{
                        chooseTagListStandby.remove(allTagList.get(position));
                        TagDaoImpl.topicDeleteTag(topicId,allTagList.get(position).getId());
                    }
                }else{
                    deleteTag(position);
                }

                tagLayoutChoose.onChanged();
                tagLayoutAll.onChanged();
                return true;
            }
        });
    }

    private void addNewTag() {

        View view = LayoutInflater.from(TagActivity.this).inflate(R.layout.add_tag_edit,null,false);
        AlertDialog.Builder builder = new AlertDialog.Builder(TagActivity.this);
        final EditText tagEdit = view.findViewById(R.id.tag_input);
        builder.setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TagDaoImpl.findAllTag().size() == 31){
                            Toasty.warning(TagActivity.this,getString(R.string.tag_num_warn), Toast.LENGTH_SHORT, true).show();
                            return ;
                        }
                        newTag = TagDaoImpl.newTag(tagEdit.getText().toString());
                        //assert newTag != null;
                        if (newTag == null){
                            Toasty.warning(TagActivity.this,R.string.repeated_tag, Toast.LENGTH_SHORT, true).show();
                            return ;
                        }
                        TagDaoImpl.topicAddTag(topicId,newTag.getId());

                        chooseTagListStandby.add(0,newTag);
                        allTagList.add(0,newTag);

                        tagLayoutChoose.onChanged();
                        tagLayoutAll.onChanged();
                    }
                }).show();

    }

    private void deleteTag(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TagActivity.this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_tag));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TagDaoImpl.deleteTag(allTagList.get(position).getId());
                chooseTagListStandby.remove(allTagList.get(position));
                allTagList.remove(position);
                isDelete = false;
                tagLayoutChoose.onChanged();
                tagLayoutAll.onChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDelete) {
            isDelete = false;
            tagLayoutAll.onChanged();
        }
        return true;
    }
}
