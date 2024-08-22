package com.luckyxmobile.correction.adapter;

/**
 * @author ChangHao
 * 实现recycleview item长按拖动需要用到的回调接口
 *
 */
public interface ItemTouchHelperViewHolder {

    /**
     * 当Item开始拖拽或者滑动的时候调用
     */
    void onItemSelected();

    /**
     * 当Item完成拖拽或者滑动的时候调用
     */
    void onItemClear();
}
