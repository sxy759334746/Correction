package com.luckyxmobile.correction.ui.callback;

import android.graphics.Canvas;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.luckyxmobile.correction.adapter.ItemTouchAdapter;
import com.luckyxmobile.correction.adapter.ItemTouchHelperViewHolder;

/**
 * @author ChangHao
 * 实现recycleview item长按拖动需要用到的回调接口
 */
public class ItemTouchCallback extends ItemTouchHelper.Callback {
    public static final float ALPHA_FULL = 1.0f;
    private ItemTouchAdapter itemTouchAdapter;

    public ItemTouchCallback(ItemTouchAdapter itemTouchAdapter) {
        this.itemTouchAdapter = itemTouchAdapter;
    }

    /**
     * 指定可以支持的拖放和滑动的方向
     * List类型的RecyclerView，拖拽只有UP、DOWN
     * Grid类型的则有UP、DOWN、LEFT、RIGHT四个方向
     * <p/>
     * dragFlags 是拖拽标志，swipeFlags是滑动标志，在Grid中把swipeFlags设置为0，表示不处理滑动操作。
     *
     * @param recyclerView
     * @param viewHolder
     * @return
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        //判断recyclerView传入的LayoutManager是不是GridLayoutManager
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            //Grid的拖拽方向，上、下、左、右
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            //设置为0，表示不支持滑动
            final int swipeFlags = 0;
            //创建拖拽或者滑动标志的快速方式
            return makeMovementFlags(dragFlags, swipeFlags);
        } else {
            //List类型的拖拽方向，上或者下
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            //List类型支持滑动，左或者右
            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            //创建拖拽或者滑动标志的快速方式
            return makeMovementFlags(dragFlags, swipeFlags);
        }
    }

    /**
     * 告诉ItemTouchHelper是否需要RecyclerView支持长按拖拽,返回true是支持，false是不支持
     *
     * @return
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    /**
     * 告诉ItemTouchHelper是否需要RecyclerView支持滑动,返回true是支持，false是不支持
     *
     * @return
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    /**
     * 当拖拽开始的时候调用
     */
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        //拖拽的时候改变一下选中Item的颜色
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof ItemTouchHelperViewHolder) {
                //让ViewHolder知道Item开始选中
                ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
                //回调ItemTouchHelperVIewHolder的方法
                itemViewHolder.onItemSelected();
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    /**
     * 当拖拽结束的时候调用
     */
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setAlpha(ALPHA_FULL);
        if (viewHolder instanceof ItemTouchHelperViewHolder) {
            ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
            //Item移动完成之后的回调
            itemViewHolder.onItemClear();
        }
    }

    /**
     * 当ItemTouchHelper要拖动的Item从原来位置拖动到新的位置的时候调用
     * 当我们拖拽的时候调用这个方法
     *
     * @return boolean
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        //如果两个item不是一个类型的，我们让他不可以拖拽
        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }

        //得到拖动ViewHolder的position
        int fromPosition = viewHolder.getAdapterPosition();
        //得到目标ViewHolder的position
        int toPosition = target.getAdapterPosition();
        itemTouchAdapter.onItemMove(fromPosition, toPosition);

        return true;
    }

    /**
     * 当ViewHolder滑动的时候调用
     * 当滑动Item的时候调用此方法
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        itemTouchAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    /**
     * 由 ItemTouchHelper 在 RecyclerView 的 onDraw方法中回调调用
     */
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}