package com.pw.qi1siwole.viewoftree;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pw.qi1siwole.viewoftree.Tree.TreeMgr;
import com.pw.qi1siwole.viewoftree.Tree.TreeNode;

/**
 * Created by user on 2017/3/23.
 */

public class Qi1siwole {

    // 提示
    public static void showToast(Context ctx, String param) {
        Toast.makeText(ctx, param, Toast.LENGTH_SHORT)
                .show();
    }
    public static void showToast(Context ctx, int param) {
        Toast.makeText(ctx, param, Toast.LENGTH_SHORT)
                .show();
    }

    // 用不同个数的TextView占位来表示缩进
    public static int addViewForTabs(Context ctx, ViewGroup viewGroup, TreeNode node) {
        int level = node.getLevel();
        for (int i = 0; i < level; ++i) {
            View subView = LayoutInflater.from(ctx).inflate(R.layout.text_view_tab, null);
            TextView tv = (TextView)subView.findViewById(R.id.tv_tab);
            tv.setTextColor(Color.GRAY);
            if (i > 0) {
                tv.setVisibility(View.INVISIBLE);
            }
            viewGroup.addView(subView, 0);
        }
        return level;
    }

    // tree的listview的item的view
    public static ViewGroup getTreeItemView(Context ctx, int resourceId, TreeNode node) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(ctx).inflate(resourceId, null);

        // 叶子节点，以及是否折叠的标识
        TextView tv_flag = (TextView)view.findViewById(R.id.tv_flag);
        tv_flag.setText(ctx.getString(TreeMgr.getFlag(node)));
        tv_flag.setTextColor(Color.BLACK);

        // Node的文本
        TextView tv_name = (TextView)view.findViewById(R.id.tv_name);
        tv_name.setText(node.getText());

        return view;
    }

}
