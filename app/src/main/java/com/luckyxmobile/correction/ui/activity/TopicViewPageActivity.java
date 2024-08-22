package com.luckyxmobile.correction.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.adapter.ViewPageAdapter;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Tag;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.bean.TopicViewPageItem;
import com.luckyxmobile.correction.dao.impl.CorrectionLab;
import com.luckyxmobile.correction.dao.impl.TagDaoImpl;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.SDCardUtil;
import com.noober.menu.FloatMenu;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;
import org.litepal.LitePal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class TopicViewPageActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "TopicViewPageActivity";
    private ViewPager viewPagerTopics;
    private ViewPageAdapter viewPageAdapter;
    private List<Topic> topics;
    private int book_id, positionPager;
    private Topic nowTopic;
    private List<Tag> tagsAll = new ArrayList<>();
    private LikeButton storeButton;
    private ImageButton moreButton;
    private TagFlowLayout tagFlowLayout;
    private TagFlowLayout topicImagesByNum;
    private int whichImage = 0;
    private RadioGroup displayRadioGroup;
    private boolean isFullScreen = false;
    private boolean isShowTag = true;
    private SharedPreferences preferences;
    public List<Boolean> show_tips_states = new ArrayList<>();
    public List<String> display_image_which = new ArrayList<>();
    private Tag moreTag = new Tag();
    private ProgressBar progressBarTopicViewPage;
    private ImageButton fullScreenBtn;
    private ImageButton editBtn;
    //0:点击按扭 1：点击涂抹 2：均可
    private int viewSmearBy = -1;
    public static boolean IS_CLICK_SMEAR_BY = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_topic_view_page);
        initView();
        initDisplayRadioGroup();
    }

    private void initView() {
        book_id = getIntent().getIntExtra(ConstantsUtil.BOOK_ID,0);
        positionPager = getIntent().getIntExtra(ConstantsUtil.TOPIC_POSITION,0);
        preferences = getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION,MODE_PRIVATE);

        viewPagerTopics = findViewById(R.id.topic_view_pager);
        storeButton = findViewById(R.id.store_button);
        tagFlowLayout = findViewById(R.id.tags);
        topicImagesByNum = findViewById(R.id.num_topic_images);
        displayRadioGroup = findViewById(R.id.display_what);
        moreButton = findViewById(R.id.more_btn);
        fullScreenBtn = findViewById(R.id.full_screen_btn);
        progressBarTopicViewPage = findViewById(R.id.progress_bar_topic_view_page);
        editBtn = findViewById(R.id.edit_image_btn);
        moreButton.setOnClickListener(this);
        fullScreenBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);

        isFullScreen = preferences.getBoolean(ConstantsUtil.TABLE_FULL_SCREEN,false);

        isShowTag = preferences.getBoolean(ConstantsUtil.TABLE_SHOW_TAG,true);

        viewSmearBy = preferences.getInt(ConstantsUtil.TABLE_VIEW_SMEAR_BY,2);

        setFullScreen(isFullScreen);

        if (!isShowTag){
            tagFlowLayout.setVisibility(View.GONE);
        }

        if (viewSmearBy == 1){
            //点击涂抹
            displayRadioGroup.setVisibility(View.GONE);
        }else if (viewSmearBy == 0){
            //点击按扭
            IS_CLICK_SMEAR_BY = false;
        }

    }

    private void initDisplayRadioGroup() {
        for (int i = 0;i < displayRadioGroup.getChildCount();++i) {
            final int finalI = i;
            displayRadioGroup.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFocus(finalI);
                    setViewPageTopicAgain(positionPager);
                }
            });
            show_tips_states.add(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //获取topic
        if (book_id == -1){
            topics = LitePal.findAll(Topic.class);
            Collections.reverse(topics);
        }else{
            Book book = LitePal.find(Book.class,book_id);
            if (book.getBook_cover().equals("R.mipmap.favorite")){
                //收藏
                topics = LitePal.where("topic_collection=?", "1").find(Topic.class);
            }else{
                topics = LitePal.where("book_id=?", String.valueOf(book_id)).find(Topic.class);
            }

            if (preferences.getBoolean(ConstantsUtil.TABLE_SHARED_IS_NEWEST_ORDER,true)){
                Collections.reverse(topics);
            }
        }

        if (topics.size()==0){
            onBackPressed();
        }

        //加载topic
        tagsAll = LitePal.findAll(Tag.class);
        tagsAll.add(moreTag);//更多按钮

        //处理viewPage
        initViewPager();

        progressBarTopicViewPage.setMax(topics.size());

        progressBarTopicViewPage.setProgress(positionPager+1,true);

    }

    private void initViewPager() {

        viewPageAdapter = new ViewPageAdapter(this,topics);
        //绑定viewpage
        setViewPageTopicAgain(positionPager);
        //滚动监听事件 刷新
        viewPagerTopics.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                positionPager = position;
                setStoreButton(position);
                setTagFlowLayout(position);
                setTopicImagesByNum(position);
                progressBarTopicViewPage.setProgress(positionPager+1,true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //当前页码
        positionPager = viewPagerTopics.getCurrentItem();
        //刷新当前页的收藏
        setStoreButton(positionPager);
        //刷新当前页的标签
        setTagFlowLayout(positionPager);
        //刷新当前页的图片角标（一题多图的情况）
        setTopicImagesByNum(positionPager);
    }

    private void setViewPageTopicAgain(int position) {
        viewPageAdapter.showPaperImage(display_image_which);
        //重新为viewPager设置adapter, 来强制重置要显示的图片
        viewPagerTopics.setAdapter(viewPageAdapter);
        viewPagerTopics.setCurrentItem(position);
    }

    /**
     * 收藏按钮
     * @param position 当前页
     */
    private void setStoreButton(final int position){

        nowTopic = LitePal.find(Topic.class,topics.get(position).getId());

        if (nowTopic.getTopic_collection() == 1){
            storeButton.setLiked(true);
        }else{
            storeButton.setLiked(false);
        }

        storeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                topics.get(position).setTopic_collection(1); // 设置选中
                topics.get(position).save();
                Toasty.info(TopicViewPageActivity.this,R.string.collect, Toast.LENGTH_SHORT,true).show();
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                topics.get(position).setTopic_collection(0);
                //topic.setToDefault("topic_collection"); // 不选中的话，保持原来状态
                topics.get(position).save();
                Toasty.info(TopicViewPageActivity.this,R.string.collect_cancel,Toast.LENGTH_SHORT,true).show();
            }
        });
    }

    /**
     * 图片角标--一题多图情况
     * @param positionPager 当前页
     */
    private void setTopicImagesByNum(int positionPager){

        TopicViewPageItem item = viewPageAdapter.getTopicViewPageItem(positionPager);

        int imagesSum = item.getImagesSum();

        int currentImage = item.getCurrentImage();

        if (imagesSum < 2){
            topicImagesByNum.setVisibility(View.GONE);
        }else{
            topicImagesByNum.setVisibility(View.VISIBLE);
        }

        Log.d( "TopicViewPage","positionPager--"+positionPager+"；sum--"+imagesSum+";current--"+currentImage);

        List<Boolean> imagesNum = new ArrayList<>();

        for (int i = 0; i < imagesSum; i++) {
            if (i == currentImage){
                imagesNum.add(true);
                whichImage = i;
            }else{
                imagesNum.add(false);
            }
        }

        topicImagesByNum.setAdapter(new TagAdapter<Boolean>(imagesNum) {
            @Override
            public View getView(FlowLayout parent, int position, Boolean isCheck) {
                CheckBox checkBox = (CheckBox) LayoutInflater.from(TopicViewPageActivity.this)
                        .inflate(R.layout.flow_item_images_num_on_topic,topicImagesByNum, false);

                checkBox.setChecked(isCheck);

                checkBox.setText(String.valueOf(position+1));

                return checkBox;
            }
        });

        topicImagesByNum.setOnTagClickListener((view, position, parent) -> {

            boolean isCheck = imagesNum.get(position);

            if (!isCheck){
                for (int i = 0; i < imagesNum.size(); i++) {
                    if (i==position){
                        imagesNum.set(i,true);
                        whichImage = position;
                        viewPageAdapter.setCurrentImage(positionPager,position);
                        setViewPageTopicAgain(positionPager);
                    }else{
                        imagesNum.set(i,false);
                    }
                }
                topicImagesByNum.onChanged();
            }
            return true;
        });
    }

    /**
     * 标签
     * @param position 当前页
     */
    private void setTagFlowLayout(final int position){
        nowTopic = LitePal.find(Topic.class,topics.get(position).getId());
        List<Tag> tagList = TagDaoImpl.findTagByTopic(nowTopic.getTopic_tag());
        final List<Tag> tagsStandby = new ArrayList<>();

        for (Tag tag1:tagsAll){
            for (Tag tag2 : tagList){
                if (tag1.getId()==tag2.getId()){
                    tagsStandby.add(tag1);
                }
            }
        }

        tagFlowLayout.setAdapter(new TagAdapter<Tag>(tagsAll) {
            @Override
            public View getView(FlowLayout parent, int position, Tag tag) {

                CheckBox checkBox = (CheckBox) LayoutInflater.from(TopicViewPageActivity.this)
                        .inflate(R.layout.flow_item_tag_on_topic,tagFlowLayout, false);

                if (position == tagsAll.size() - 1) {
                    Button moreTag = (Button) LayoutInflater.from(TopicViewPageActivity.this)
                            .inflate(R.layout.tag_add,tagFlowLayout, false);
                    return moreTag;
                }else{
                    if (tagsStandby.contains(tag)){
                        checkBox.setText(tag.getTag_name());
                        checkBox.setChecked(true);
                    }else{
                        checkBox.setText(tag.getTag_name());
                        checkBox.setChecked(false);
                    }
                }
                checkBox.setTextColor(getColor(R.color.white));

                return checkBox;
            }
        });

        tagFlowLayout.setOnTagClickListener((view, position1, parent) -> {
            if (position1 < tagsAll.size() - 1) {
                if (!tagsStandby.contains(tagsAll.get(position1))) {
                    tagsStandby.add(tagsAll.get(position1));
                    nowTopic = TagDaoImpl.topicAddTag(nowTopic.getId(), tagsAll.get(position1).getId());
                } else {
                    tagsStandby.remove(tagsAll.get(position1));
                    nowTopic = TagDaoImpl.topicDeleteTag(nowTopic.getId(), tagsAll.get(position1).getId());
                }
                topics.get(position).setTopic_tag(nowTopic.getTopic_tag());
                tagFlowLayout.onChanged();
            } else {
                //最后一个为“更多”
                Intent intent = new Intent(TopicViewPageActivity.this, TagActivity.class);
                intent.putExtra(ConstantsUtil.TOPIC_ID, nowTopic.getId());
                intent.putExtra(ConstantsUtil.WHICH_ACTIVITY,BookDetailActivity.TAG);
                startActivity(intent);
            }

            return true;
        });
    }

    /**
     * 展示错题下面按钮的切换
     * @param position radioButton的位置, 第一个一定是显示全部
     */
    private void setFocus(int position) {
        String[] display_tips = {ConstantsUtil.PAINT_RIGHT, ConstantsUtil.PAINT_ERROR, ConstantsUtil.PAINT_POINT, ConstantsUtil.PAINT_REASON};
        show_tips_states.set(position, !show_tips_states.get(position));
        reverseButtonDrawable(displayRadioGroup.getChildAt(position));

        //全部显示状态下， 把其他的按钮变为显示
        if(show_tips_states.get(0) && position == 0) {
            for(int i = 1;i < show_tips_states.size();++i){
                if(!show_tips_states.get(i))
                    reverseButtonDrawable(displayRadioGroup.getChildAt(i));
                show_tips_states.set(i, true);
            }
            //变为全不显示
        }else if(!show_tips_states.get(0) && position == 0){
            for(int i = 1;i < show_tips_states.size();++i){
                if(show_tips_states.get(i))
                    reverseButtonDrawable(displayRadioGroup.getChildAt(i));
                show_tips_states.set(i, false);
            }
            //其他的按钮被按：更新‘全部显示’按钮的状态
        }else {
            if(show_tips_states.get(0)) {
                show_tips_states.set(0, false);
                reverseButtonDrawable(displayRadioGroup.getChildAt(0));
            }
            boolean is_all_show = true;
            for(int i = 1;i < show_tips_states.size();++i){
                is_all_show &= show_tips_states.get(i);
            }
            if(is_all_show){
                show_tips_states.set(0, true);
                reverseButtonDrawable(displayRadioGroup.getChildAt(0));
            }
        }

        display_image_which.clear();
        for(int i = 1;i < show_tips_states.size();++i){
            if(show_tips_states.get(i))
                display_image_which.add(display_tips[i-1]);
        }

    }

    private void reverseButtonDrawable(View v){
        ((TransitionDrawable)v.getBackground()).reverseTransition(50);
        ((RadioButton)v).setTextColor(((RadioButton)v).getCurrentTextColor() == getColor(R.color.black) ? getColor(R.color.white) : getColor(R.color.black));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(0, R.anim.activity_close);
        finish();
    }

    private void setFullScreen(boolean isFullScreen) {
        if (!isFullScreen){
            Animation layoutIn = AnimationUtils.loadAnimation(this, R.anim.layout_in_above);
            Animation tips_layoutIn = AnimationUtils.loadAnimation(this, R.anim.layout_in_below);
            layoutIn.setDuration(300);
            tips_layoutIn.setDuration(300);

            storeButton.setVisibility(View.VISIBLE);
            storeButton.setAnimation(layoutIn);

            moreButton.setVisibility(View.VISIBLE);
            moreButton.setAnimation(layoutIn);

            editBtn.setVisibility(View.VISIBLE);
            editBtn.setAnimation(layoutIn);

            if (viewSmearBy != 1) {

                displayRadioGroup.setVisibility(View.VISIBLE);
                displayRadioGroup.setAnimation(tips_layoutIn);
            }


            fullScreenBtn.setBackground(getDrawable(R.drawable.ic_fullscreen_24dp));
        }else{
            Animation layoutOut = AnimationUtils.loadAnimation(this, R.anim.layout_out_above);
            Animation tips_layoutOut = AnimationUtils.loadAnimation(this, R.anim.layout_out_below);
            layoutOut.setDuration(300);
            tips_layoutOut.setDuration(300);

            storeButton.setVisibility(View.GONE);
            storeButton.setAnimation(layoutOut);

            moreButton.setVisibility(View.GONE);
            moreButton.setAnimation(layoutOut);

            editBtn.setVisibility(View.GONE);
            editBtn.setAnimation(layoutOut);

            if (viewSmearBy != 1) {
                displayRadioGroup.setVisibility(View.GONE);
                displayRadioGroup.setAnimation(tips_layoutOut);
            }

            fullScreenBtn.setBackground(getDrawable(R.drawable.ic_fullscreen_exit_24dp));
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.full_screen_btn:
                isFullScreen = !isFullScreen;
                setFullScreen(isFullScreen);
                break;
            //点击列表按钮显示细节和删除
            case R.id.more_btn:

                final FloatMenu menu = new FloatMenu(this);
                menu.items(300,this.getString(R.string.delete), this.getString(R.string.details));
                menu.showAsDropDown(moreButton,0,0,Gravity.END);
                menu.show();

                menu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
                    @Override
                    public void onClick(View v, int position) {
                        switch (position) {
                            case 0:
                                new AlertDialog.Builder(TopicViewPageActivity.this)
                                        .setTitle(R.string.confirm_delete)
                                        .setIcon(R.drawable.ic_delete_red_24dp)
                                        .setMessage(R.string.confirm_delete_topic)
                                        .setPositiveButton(R.string.ensure, (dialog, which) -> {
                                            //删除topic图片
                                            SDCardUtil.cascadeDeleteTopic(topics.get(viewPagerTopics.getCurrentItem()).getId(), TopicViewPageActivity.this);
                                            CorrectionLab.deleteTopic(topics.get(viewPagerTopics.getCurrentItem()).getId());
                                            if (topics.size()-1 > 0){
                                                Intent intent = new Intent(TopicViewPageActivity.this,TopicViewPageActivity.class);
                                                intent.putExtra(ConstantsUtil.BOOK_ID, book_id);
                                                intent.putExtra(ConstantsUtil.TOPIC_POSITION, Math.max((positionPager - 1), 0));
                                                startActivity(intent);
                                                finish();
                                            }else{
                                                onBackPressed();
                                            }

                                        })
                                        .setNegativeButton(R.string.cancel,null).show();
                                menu.dismiss();
                                break;

                            case 1:
                                Intent intent = new Intent(TopicViewPageActivity.this, TopicActivity.class);
                                intent.putExtra(ConstantsUtil.TOPIC_ID, topics.get(viewPagerTopics.getCurrentItem()).getId());
                                intent.putExtra(ConstantsUtil.WHICH_ACTIVITY, BookDetailActivity.TAG);
                                intent.putExtra(ConstantsUtil.TOOLBAR_NAME,LitePal.find(Book.class,topics.get(viewPagerTopics.getCurrentItem()).getBook_id()).getBook_name());
                                startActivity(intent);
                                menu.dismiss();
                                break;

                            default:
                                break;
                        }
                    }
                });
                break;

            case R.id.edit_image_btn:
                Intent intent = new Intent(TopicViewPageActivity.this, EditPhotoActivity.class);
                intent.putExtra(ConstantsUtil.TOPIC_ID, topics.get(viewPagerTopics.getCurrentItem()).getId());
                intent.putExtra(ConstantsUtil.WHICH_ACTIVITY, TAG);
                intent.putExtra(ConstantsUtil.IMAGE_POSITION,whichImage);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

}
