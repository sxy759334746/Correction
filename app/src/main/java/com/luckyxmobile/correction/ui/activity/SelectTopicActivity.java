package com.luckyxmobile.correction.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.adapter.SelectTopicAdapter;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Tag;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.BookDao;
import com.luckyxmobile.correction.dao.PaperTopicDao;
import com.luckyxmobile.correction.dao.TopicDao;
import com.luckyxmobile.correction.dao.impl.BookDaoImpl;
import com.luckyxmobile.correction.dao.impl.PaperTopicDaoImpl;
import com.luckyxmobile.correction.dao.impl.TopicDaoImpl;
import com.luckyxmobile.correction.util.DestroyActivityUtil;
import com.luckyxmobile.correction.util.PhotoUtil;
import com.luckyxmobile.correction.util.ThreadPool;
import com.zj.myfilter.FiltrateBean;
import com.zj.myfilter.FlowPopWindow;
import org.litepal.LitePal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import es.dmoral.toasty.Toasty;
import static java.util.regex.Pattern.*;

/**
 * 选择错题
 *
 * @author yanghao
 */
public class SelectTopicActivity extends AppCompatActivity {
    /**
     * 点击筛选的下拉窗口
     */
    private FlowPopWindow flowPopWindow;
    /**
     * 筛选框数据
     */
    private List<FiltrateBean> filterList = new ArrayList<>();
    /**
     * 错题列表
     */
    private RecyclerView select_topic_list;
    /**
     * 错题列表适配器
     */
    private SelectTopicAdapter selectTopicListAdapter;
    /**
     * 错题列表数据
     */
    private List<Map<String, Object>> selectTopicListDatas = new ArrayList<>();
    /**
     * 错题列表CheckBox的值
     */
    private Map<Integer, Boolean> map;
    /**
     * 按钮
     */
    private Button btn;
    /**
     * 错题本
     */
    private FiltrateBean filter_book;
    /**
     * 标签
     */
    private FiltrateBean label;

    /**
     * 错题本列表
     */
    private List<Book> books;

    /**
     * 标签列表
     */
    private List<Tag> labels;

    /**
     * 筛选点击的位置
     */
    private StringBuilder select_position;
    /**
     * 判断是否进行过筛选
     */
    private boolean isFiltered = false;

    /**
     * 从复习卷详情传过来的paperId
     */
    private String paper_id = null;
    /**
     * 筛选后的错题列表
     */
    private List<Topic> selectTopicList = new ArrayList<>();
    /**
     * 选中的错题
     */
    private ArrayList<Integer> selectedTopicList = new ArrayList<>();

    /**
     * 改变ui
     */
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    selectTopicListAdapter = new SelectTopicAdapter(selectTopicListDatas, SelectTopicActivity.this);
                    select_topic_list.setAdapter(selectTopicListAdapter);
                    // 获取错题列表CheckBox的值
                    map = selectTopicListAdapter.getMap();
                    break;
                case 2:
                    selectTopicListAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
            return false;

        }
    });
    private PaperTopicDao paper_topic_dao = new PaperTopicDaoImpl();
    private TopicDao topicDao = new TopicDaoImpl();
    private BookDao bookDao = new BookDaoImpl();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_select);

        select_topic_list = findViewById(R.id.select_topic_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SelectTopicActivity.this);
        // 为错题列表设置布局
        select_topic_list.setLayoutManager(layoutManager);
        // 为错题列表添加分割线
//        select_topic_list.addItemDecoration(new DividerItemDecoration(SelectTopicActivity.this, 1));
        selectTopicListAdapter = new SelectTopicAdapter(selectTopicListDatas, SelectTopicActivity.this);
        select_topic_list.setAdapter(selectTopicListAdapter);
        // 初始化ToolBar
        initToolbar();
        // 初始化Button 判断按钮是完成还是下一步
        initButton();
        // 初始化数据
        initData();

    }

    /**
     * 初始化Button
     */
    private void initButton() {
        btn = findViewById(R.id.btn);
        // 从PaperDetailActivity过来，按钮为完成
        if (SelectTopicActivity.this.getIntent().getStringExtra("reviewPaperId") != null) {
            btn.setText(R.string.finish);
        } else {
            // 不是从PaperDetailActivity过来，按钮为下一步
            btn.setText(R.string.next_step);
        }
        // 按钮点击事件
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理选中的题目
                handleData();
            }
        });

    }

    /**
     * 处理选中的题目
     */
    private void handleData() {
        // 把选中的错题id加到topic_ids里
        ArrayList<Integer> topic_ids = new ArrayList<>();
        if (map != null) {
            for (int i = 0; i < map.size(); i++) {
                if (map.get(i)) {
                    topic_ids.add((Integer) selectTopicListDatas.get(i).get("topic_id"));
                }
            }
        }
        // 完成按钮
        if (SelectTopicActivity.this.getIntent().getStringExtra("reviewPaperId") != null) {
            paper_topic_dao.update(Integer.valueOf(paper_id), topic_ids);
            setResult(RESULT_OK);
            finish();
        } else {
            // 下一步按钮
            DestroyActivityUtil.addDestroyActivityToMap(this, "SelectTopicActivity");
            Intent intent = new Intent(SelectTopicActivity.this, SelectPaperActivity.class);
            intent.putIntegerArrayListExtra("topic_ids", topic_ids);
            startActivity(intent);
        }

    }

    /**
     * 初始化数据
     */
    private void initData() {
        ThreadPool.runnable = new Runnable() {
            @Override
            public void run() {
                // 初始化筛选框的数据
                initFilterData();
                // 初始化错题列表
                initTopicList();
                // 通过handler改变ui
                handler.sendEmptyMessage(1);
            }
        };
        ThreadPool.singleThreadExecutor.execute(ThreadPool.runnable);
    }

    /**
     * 初始化筛选框的数据
     */
    private void initFilterData() {
        // 从数据库获取全部错题本
        books = LitePal.findAll(Book.class);
        String[] wrong_books = null;
        if (books != null && books.size() != 0) {
            wrong_books = new String[books.size()];
            for (int i = 0; i < books.size(); i++) {
                wrong_books[i] = books.get(i).getBook_name();
            }
        }

        if (wrong_books != null) {
            filter_book = new FiltrateBean();
            filter_book.setTypeName(this.getString(R.string.wrong_book));
            List<FiltrateBean.Children> childrenList = new ArrayList<>();
            for (String wrong_book : wrong_books) {
                FiltrateBean.Children cd = new FiltrateBean.Children();
                cd.setValue(wrong_book);
                childrenList.add(cd);
            }
            filter_book.setChildren(childrenList);
        }

        // 从数据库获取全部标签
        labels = LitePal.findAll(Tag.class);
        String[] Tags = null;
        if (labels != null && labels.size() != 0) {
            Tags = new String[labels.size()];
            for (int i = 0; i < labels.size(); i++) {
                Tags[i] = labels.get(i).getTag_name();
            }
        }

        if (Tags != null) {
            label = new FiltrateBean();
            label.setTypeName(this.getString(R.string.tag));
            List<FiltrateBean.Children> childrenList = new ArrayList<>();
            for (String label : Tags) {
                FiltrateBean.Children cd = new FiltrateBean.Children();
                cd.setValue(label);
                childrenList.add(cd);
            }
            label.setChildren(childrenList);
        }

        filterList.add(filter_book);
        filterList.add(label);

    }

    /**
     * 初始化错题列表
     */
    private void initTopicList() {
        // 查全部的错题
        List<Topic> topicLitePals = LitePal.findAll(Topic.class);
        // 把查出来的错题列表添加到错题列表
        createTopicList(topicLitePals);

    }

    /**
     * 初始化ToolBar
     */
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.select_wrong);

        // 设置回退按钮
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 设置回退按钮点击事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.select_topic_random:
                inputRandomDialog().show();
                break;
            case R.id.select_topic_filter:
                flowPopWindow = new FlowPopWindow(SelectTopicActivity.this, filterList);
                flowPopWindow.showAsDropDown(findViewById(R.id.select_topic_filter));
                flowPopWindow.setOnConfirmClickListener(new FlowPopWindow.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick() {
                        // 处理点击筛选按钮后的错题列表显示
                        select_position = new StringBuilder();
                        for (FiltrateBean fb : filterList) {
                            List<FiltrateBean.Children> cdList = fb.getChildren();
                            for (int x = 0; x < cdList.size(); x++) {
                                FiltrateBean.Children children = cdList.get(x);
                                if (children.isSelected()) {
                                    select_position.append(fb.getTypeName() + ":" + x + ";");
                                }
                            }
                        }
                        // 判断是否是筛选后再次筛选，再次筛选为空则显示全部错题
                        if (TextUtils.isEmpty(select_position.toString()) && isFiltered) {
                            refresh();
                            isFiltered = false;
                        } else if (!TextUtils.isEmpty(select_position.toString())) {
                            refresh();
                            isFiltered = true;
                        } else if (TextUtils.isEmpty(select_position.toString()) && !isFiltered) {
                            return;
                        }
                    }
                });
                break;
            default:
                break;
        }

        return true;

    }

    /**
     * 判断是否为数字
     *
     * @param str
     * @return      
     */
    public boolean isNumber(String str) {
        Pattern pattern = compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 点击筛选重新加载数据
     */
    private void refresh() {
        // 清空选中的错题，防止重复添加选中的错题
        selectedTopicList.clear();
        // 添加选中的错题
        if (map != null) {
            for (int i = 0; i < map.size(); i++) {
                if (map.get(i)) {
                    selectedTopicList.add((Integer) selectTopicListDatas.get(i).get("topic_id"));
                }
            }
        }
        // 清除错题的选中状态
        selectTopicListAdapter.clearMap();
        // 添加错题的选中状态
        for (int i = 0; i < selectedTopicList.size(); i++) {
            map.put(i, true);
        }

        // 对筛选选中的条件进行处理
        String s = select_position.toString();
        String[] split = s.split(";");
        List<String> select_filter_book = new ArrayList<>();
        List<String> select_label = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            String[] split1 = split[i].split(":");
            if (split1[0].equals(this.getString(R.string.wrong_book))) {
                select_filter_book.add(String.valueOf(books.get(Integer.parseInt(split1[1])).getId()));
            } else if (split1[0].equals(this.getString(R.string.tag))) {
                select_label.add(String.valueOf(labels.get(Integer.parseInt(split1[1])).getTag_topic()));
            }
        }
        // 刷新错题列表
        refreshTopicData(select_filter_book, select_label);

    }


    /**
     * 刷新错题列表
     *
     * @param
     * @param select_filter_book 错题本
     * @param select_label       标签
     */
    private void refreshTopicData(final List<String> select_filter_book, final List<String> select_label) {
        ThreadPool.runnable = new Runnable() {
            @Override
            public void run() {
                // 清空筛选后的错题列表
                selectTopicList.clear();
                // 添加在筛选前选中的错题列表
                selectTopicList.addAll(topicDao.selectTopicById(selectedTopicList));
                // 获取筛选的错题列表
                List<Topic> selectTopic = topicDao.selectTopic(select_filter_book, select_label);
                // 去除筛选后的错题列表和筛选前选中的错题列表的重复错题
                selectTopic.removeAll(selectTopicList);
                // 将筛选好的错题列表添加到筛选错题列表
                selectTopicList.addAll(selectTopic);
                // 清空错题列表
                selectTopicListDatas.clear();
                // 把查出来的错题列表添加到错题列表
                createTopicList(selectTopicList);
                // handler改变ui
                handler.sendEmptyMessage(2);
            }
        };
        ThreadPool.singleThreadExecutor.execute(ThreadPool.runnable);
    }

    /**
     * 把查出来的错题列表添加到错题列表
     *
     * @param List 查出来的错题列表
     */
    private void createTopicList(List<Topic> List) {
        // 判断是否是从复习卷详情过来的，如果是则获取传过来的paper_id
        if (SelectTopicActivity.this.getIntent().getStringExtra("reviewPaperId") != null) {
            paper_id = SelectTopicActivity.this.getIntent().getStringExtra("reviewPaperId");
        }
        // 复习卷id列表，用于判断错题是否已添加到复习卷
        List<String> paper_id_list;
        // 存错题在哪个错题本，错题图片的uri，是否选中此错题
        Map<String, Object> map;
        for (int i = 0; i < List.size(); i++) {
            paper_id_list = paper_topic_dao.selectByTopicId(List.get(i).getId());
            map = new HashMap<>();
            map.put("topic_id", List.get(i).getId());
            map.put("topic_imgUri", PhotoUtil.convertTopicImageByWhichs(this,List.get(i).getId(),null,0));
            map.put("book_name", this.getString(R.string.wrong_book) + ":" + bookDao.selectBookNameByTopic(List.get(i)));
            if (paper_id != null) {
                if (paper_id_list.contains(paper_id)) {
                    map.put("topic_selected", true);
                } else {
                    map.put("topic_selected", false);
                }
            } else {
                map.put("topic_selected", false);
            }
            selectTopicListDatas.add(map);
        }

    }

    //输入随机选择的dialog
    private AlertDialog.Builder inputRandomDialog() {
        final View view = LayoutInflater.from(SelectTopicActivity.this).inflate(R.layout.select_topic_input,null,false);
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectTopicActivity.this);
        builder.setView(view)
                .setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText input = view.findViewById(R.id.input_random);
                if (input.getText().toString().length()>0) {
                    if (isNumber(input.getText().toString())) {
                        int num = Integer.parseInt(input.getText().toString()); // 输入的个数
                        if (num == 0) {
                            selectTopicListAdapter.clearMap();
                            selectTopicListAdapter.notifyDataSetChanged();
                        } else {
                            int num2 = selectTopicListDatas.size(); // 当前页面的错题数
                            if (num > num2) {
                                Toasty.warning(SelectTopicActivity.this, R.string.wrong_input, Toast.LENGTH_LONG).show();
                            } else {
                                Random r = new Random();
                                ArrayList list = new ArrayList(); //生成数据集，用来保存随即生成数，并用于判断
                                while (list.size() < num) {
                                    int num3 = r.nextInt(num2);
                                    if (!list.contains(num3)) {
                                        list.add(num3); //往集合里面添加数据。
                                    }
                                }
                                selectTopicListAdapter.clearMap();
                                for (int i = 0; i < list.size(); i++) {
                                    map.put(Integer.parseInt(list.get(i).toString()), true);
                                }
                                selectTopicListAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        Toasty.warning(SelectTopicActivity.this, R.string.wrong_input, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toasty.warning(SelectTopicActivity.this, R.string.wrong_input, Toast.LENGTH_LONG).show();
                }
            }
        });

        return builder;
    }

}
