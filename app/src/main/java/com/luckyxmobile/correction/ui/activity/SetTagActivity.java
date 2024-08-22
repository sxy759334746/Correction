package com.luckyxmobile.correction.ui.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Tag;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.impl.TagDaoImpl;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.litepal.LitePal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class SetTagActivity extends AppCompatActivity {

    private TagFlowLayout tagLayoutAll;

    /**全部标签集合*/
    private List<Tag> allTagList = new ArrayList<>();

    private boolean isDelete = false;

    private Tag newTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_tag);
        initToolBar();
        initView();

        initData();
        initAdapter();


    }

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
                addTagDialog().show();
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        tagLayoutAll = findViewById(R.id.tag_layout_all);
    }

    private void initData() {
        allTagList = LitePal.findAll(Tag.class);
        tagLayoutAll.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {

                if (isDelete) {
                    deleteDialog(position).show();
                }
                return false;
            }
        });

    }

    private void initAdapter() {
        tagLayoutAll.setAdapter(new TagAdapter<Tag>(allTagList) {
            @Override
            public View getView(FlowLayout parent, int position, Tag tag) {
                CheckBox checkBox = (CheckBox) LayoutInflater.from(SetTagActivity.this).inflate
                        (R.layout.flow_item_tag,tagLayoutAll, false);
                checkBox.setText(tag.getTag_name());
                checkBox.setChecked(true);
                checkBox.setTextColor(getColor(R.color.white));
                checkBox.setClickable(false);
                if (isDelete){
                    checkBox.setTextColor(Color.GRAY);
                    checkBox.setButtonDrawable(R.drawable.ic_delete_red_24dp);
                    checkBox.setBackgroundResource(R.color.colorWhite);
                }

                ViewGroup.LayoutParams params = checkBox.getLayoutParams();
                ViewGroup.MarginLayoutParams marginLayoutParams = null;
                if (params instanceof  ViewGroup.MarginLayoutParams) {
                    marginLayoutParams = (ViewGroup.MarginLayoutParams) params;

                }else {
                    marginLayoutParams = new ViewGroup.MarginLayoutParams(params);
                }
                marginLayoutParams.setMargins(12,12,12,12);

                return checkBox;
            }
        });
    }


    //添加标签对话框
    private AlertDialog.Builder addTagDialog() {

        View view = LayoutInflater.from(SetTagActivity.this).inflate(R.layout.add_tag_edit,null,false);
        AlertDialog.Builder builder = new AlertDialog.Builder(SetTagActivity.this);
        final EditText tagEdit = view.findViewById(R.id.tag_input);
        builder.setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TagDaoImpl.findAllTag().size() == 31){
                            Toasty.warning(SetTagActivity.this,getString(R.string.tag_num_warn), Toast.LENGTH_SHORT, true).show();
                            return ;
                        }
                        newTag = TagDaoImpl.newTag(tagEdit.getText().toString());
                        //assert newTag != null;
                        if (newTag == null){
                            Toasty.warning(SetTagActivity.this,R.string.repeated_tag, Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                        allTagList.add(0,newTag);
                        tagLayoutAll.onChanged();
                    }
                });

        return builder;
    }

    //删除标签对话框
    private AlertDialog.Builder deleteDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SetTagActivity.this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_tag));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TagDaoImpl.deleteTag(allTagList.get(position).getId());
                allTagList.remove(position);
                isDelete = false;
                tagLayoutAll.onChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create();
        return builder;
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
