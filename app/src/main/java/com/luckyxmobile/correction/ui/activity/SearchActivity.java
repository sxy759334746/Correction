package com.luckyxmobile.correction.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.ui.searchview.ICallBack;
import com.luckyxmobile.correction.ui.searchview.SearchView;
import com.luckyxmobile.correction.ui.searchview.bCallBack;

/**
 * 搜索错题本、错题卷
 *
 * @created by Android Studio
 * @author DongErHeng
 * DATA: 2019/7/24
 */
public class SearchActivity extends AppCompatActivity {

    // 1. 初始化搜索框变量
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 3. 绑定组件
        searchView = (SearchView) findViewById(R.id.search_view);

        // 4. 设置点击搜索按键后的操作（通过回调接口）
        // 参数 = 搜索框输入的内容
        searchView.setOnClickSearch(new ICallBack() {
            @Override
            public void SearchAciton(String string) {
                System.out.println("我收到了" + string);

            }
        });

        // 5. 设置点击返回按键后的操作（通过回调接口）
        searchView.setOnClickBack(new bCallBack() {
            @Override
            public void BackAciton() {
                finish();
            }
        });

    }
}
