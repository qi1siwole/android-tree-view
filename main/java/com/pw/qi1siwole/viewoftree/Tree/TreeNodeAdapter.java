package com.pw.qi1siwole.viewoftree.Tree;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pw.qi1siwole.viewoftree.Qi1siwole;
import com.pw.qi1siwole.viewoftree.R;

import java.util.List;

/**
 * Created by user on 2017/3/22.
 */

public class TreeNodeAdapter extends ArrayAdapter<TreeNode> {
    private int mResourceId;

    private int mOperatingIndex;
    private boolean mIsMoving;
    private TreeNode mMoveNode;

    public interface OnTreeItemListener {
        void onTreeItemRefreshClick();              // 点击【刷新】（只有根节点有）
        void onTreeItemSaveClick();                 // 点击【保存】（只有根节点有）
        void onTreeItemOpenClick();                 // 点击【打开】（只有根节点有）
        void onTreeItemNewClick();                  // 点击【新建】（只有根节点有）
        void onTreeItemFoldClick(int position);     // 点击【折叠/展开】
        void onTreeItemNameClick(int position);     // 点击【Node文本】：显示/隐藏以下3个
        void onTreeItemDeleteClick(int position);   // 点击【删除】（根节点无此选项）
        void onTreeItemEditClick(int position);     // 点击【修改】
        void onTreeItemAddClick(int position);      // 点击【添加】
        void onTreeItemDragClick(ViewGroup viewGroup, int position);     // 显示【拽动】（根节点无此选项）
    }

    private OnTreeItemListener mOnTreeItemListener;

    public void setOnTreeItemListener(OnTreeItemListener onTreeItemListener) {
        mOnTreeItemListener = onTreeItemListener;
    }

    /*
    private View.OnTouchListener mOnTouchListener;

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        mOnTouchListener = onTouchListener;
    }
    */

    public TreeNodeAdapter(Context context, int resourceId, List<TreeNode> objects) {
        super(context, resourceId, objects);
        mResourceId = resourceId;
        mOperatingIndex = -1;
        mIsMoving = false;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TreeNode node = getItem(position);
        ViewGroup view = Qi1siwole.getTreeItemView(getContext(), mResourceId, node);

        if (mIsMoving && node == mMoveNode) {
            view.setVisibility(View.GONE);
            return view;
        }

        // 是否显示【删除】【修改】【添加】【拽动】等选项
        if (mOperatingIndex == position) {
            addViewForEdit(view, position);
            if (0 != position) {
                addViewForDelete(view, position);
            }
            addViewForAdd(view, position);
            if (0 != position) {
                addViewForDrag(view, position);
            }
            if (0 == position) {
                addViewForRefresh(view);
                addViewForSave(view);
                addViewForOpen(view);
                addViewForNew(view);
            }
        }

        // Node的文本
        TextView tv_name = (TextView)view.findViewById(R.id.tv_name);
        tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTreeItemListener.onTreeItemNameClick(mOperatingIndex == position? -1 : position);
            }
        });

        // Node的标记：折叠 Or 展开
        TextView tv_flag = (TextView)view.findViewById(R.id.tv_flag);

        // TEST
        if (0 == position) {
            tv_flag.setVisibility(View.GONE);
            tv_name.setTextColor(Color.BLACK);

            return view;
        }

        tv_flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTreeItemListener.onTreeItemFoldClick(position);
            }
        });

        Qi1siwole.addViewForTabs(getContext(), view, node);

        return view;
    }

    // 新建
    private void addViewForNew(final ViewGroup view) {
        View subView = LayoutInflater.from(getContext()).inflate(R.layout.text_view_tab, null);
        TextView tv = (TextView)subView.findViewById(R.id.tv_tab);
        tv.setText(R.string.create_new);
        tv.setTextColor(Color.BLACK);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTreeItemListener.onTreeItemNewClick();
            }
        });
        view.addView(subView);
    }

    // 保存
    private void addViewForSave(final ViewGroup view) {
        View subView = LayoutInflater.from(getContext()).inflate(R.layout.text_view_tab, null);
        TextView tv = (TextView)subView.findViewById(R.id.tv_tab);
        tv.setText(R.string.save);
        tv.setTextColor(Color.BLACK);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTreeItemListener.onTreeItemSaveClick();
            }
        });
        view.addView(subView);
    }

    // 打开
    private void addViewForOpen(final ViewGroup view) {
        View subView = LayoutInflater.from(getContext()).inflate(R.layout.text_view_tab, null);
        TextView tv = (TextView)subView.findViewById(R.id.tv_tab);
        tv.setText(R.string.open);
        tv.setTextColor(Color.BLACK);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTreeItemListener.onTreeItemOpenClick();
            }
        });
        view.addView(subView);
    }

    // 刷新
    private void addViewForRefresh(final ViewGroup view) {
        View subView = LayoutInflater.from(getContext()).inflate(R.layout.text_view_tab, null);
        TextView tv = (TextView)subView.findViewById(R.id.tv_tab);
        tv.setText(R.string.refresh);
        tv.setTextColor(Color.YELLOW);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTreeItemListener.onTreeItemRefreshClick();
            }
        });
        view.addView(subView);
    }

    // 拽动
    private void addViewForDrag(final ViewGroup view, final int position) {
        View subView = LayoutInflater.from(getContext()).inflate(R.layout.text_view_tab, null);
        TextView tv = (TextView)subView.findViewById(R.id.tv_tab);
        tv.setText(R.string.drag);
        tv.setTextColor(Color.YELLOW);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTreeItemListener.onTreeItemDragClick(view, position);
            }
        });
        view.addView(subView);
    }

    // 增加
    private void addViewForAdd(ViewGroup view, final int position) {
        View subView = LayoutInflater.from(getContext()).inflate(R.layout.text_view_tab, null);
        TextView tv = (TextView)subView.findViewById(R.id.tv_tab);
        tv.setText(R.string.add);
        tv.setTextColor(Color.BLUE);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTreeItemListener.onTreeItemAddClick(position);
            }
        });
        view.addView(subView);
    }

    // 删除
    private void addViewForDelete(ViewGroup view, final int position) {
        View subView = LayoutInflater.from(getContext()).inflate(R.layout.text_view_tab, null);
        TextView tv = (TextView)subView.findViewById(R.id.tv_tab);
        tv.setText(R.string.delete);
        tv.setTextColor(Color.RED);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTreeItemListener.onTreeItemDeleteClick(position);
            }
        });
        view.addView(subView);
    }

    // 修改
    private void addViewForEdit(ViewGroup view, final int position) {
        View subView = LayoutInflater.from(getContext()).inflate(R.layout.text_view_tab, null);
        TextView tv = (TextView)subView.findViewById(R.id.tv_tab);
        tv.setText(R.string.edit);
        tv.setTextColor(Color.DKGRAY);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTreeItemListener.onTreeItemEditClick(position);
            }
        });
        view.addView(subView);
    }

    public void setOperatingIndex(int index) {
        mOperatingIndex = index;
    }
    public void setMoveState(boolean isMove) {
        mIsMoving = isMove;
    }
    public void setMoveNode(TreeNode node) { mMoveNode = node; }
}
