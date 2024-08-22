package com.luckyxmobile.correction.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Search;

import java.util.List;
/**
 * 搜索的适配器
 *
 * @created by Android Studio
 * @author DongErHeng
 * @date 2019/7/27
 * */

public class SearchAdapter extends ArrayAdapter<Search> {

    private int resourceId;

    public SearchAdapter(Context context, int resource, List<Search> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Search search = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.searchImage = view.findViewById(R.id.search_image);
            viewHolder.searchName = view.findViewById(R.id.search_name);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.searchName.setText(search.getName());
        viewHolder.searchImage.setImageResource(search.getImageId());
        return view;
    }

    class ViewHolder {
        ImageView searchImage;
        TextView searchName;
    }

}
