package com.luckyxmobile.correction.adapter;

/**
 * @author ChangHao
 * 实现recycleview item长按拖动需要用到的回调接口
 *
 */
public interface  ItemTouchAdapter {

    /**
     * Item已经移动的足够远的时候调用
     *
     * @param fromPosition
     * @param toPosition
     * @return
     */
    boolean onItemMove(int fromPosition, int toPosition);

    /**
     * 当Item滑动取消的时候调用
     *
     * @param position
     */
    void onItemDismiss(int position);
}
