package com.pw.qi1siwole.viewoftree;

import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pw.qi1siwole.viewoftree.Tree.TreeMgr;
import com.pw.qi1siwole.viewoftree.Tree.TreeNode;
import com.pw.qi1siwole.viewoftree.Tree.TreeNodeAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity
        implements View.OnTouchListener, TreeNodeAdapter.OnTreeItemListener {

    public static String TAG = "[Main Activity]";

    //private DisplayMetrics mDisplayMetrics;

    private MyDatabaseHelper dbHelper;

    ListView mTreeListView;

    private TreeNodeAdapter mTreeNodeAdapter;
    private TreeMgr mTreeMgr;
    private List<TreeNode> mTreeShowList = null;

    private RelativeLayout mMainLayout;
    private RelativeLayout mTempLayout;
    private RelativeLayout mDragView;

    private Rect mRect;

    private float mMoveStartX;
    private float mMoveStartY;

    private TreeNode mDragNode;

    //private float mMoveLastX;
    //private float mMoveLastY;

    //private boolean mIsMove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        MyDatabaseHelper.initDatabaseHelper(this);

        mMainLayout = (RelativeLayout)findViewById(R.id.activity_main);
        mMainLayout.setBackgroundColor(Color.RED);
        mMainLayout.getBackground().setAlpha(128);
        mMainLayout.setOnTouchListener(this);

        mTreeMgr = new TreeMgr();
        mTreeShowList = mTreeMgr.getShowList();

        mTreeNodeAdapter = new TreeNodeAdapter(MainActivity.this, R.layout.list_view_tree_item,
                mTreeShowList);
        mTreeNodeAdapter.setOnTreeItemListener(this);
        //mTreeNodeAdapter.setOnTouchListener(this);

        mTreeListView = (ListView)findViewById(R.id.lv_tree);
        mTreeListView.setBackgroundColor(Color.GREEN);
        mTreeListView.getBackground().setAlpha(128);
        mTreeListView.setAdapter(mTreeNodeAdapter);
        mTreeListView.setOnTouchListener(this);

        if (null == mTempLayout) {
            mTempLayout = new RelativeLayout(this);
            mTempLayout.setBackgroundColor(Color.YELLOW);
            mTempLayout.getBackground().setAlpha(128);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            mMainLayout.addView(mTempLayout, lp);
        }

        mRect = new Rect();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO

        // 有问题：可点击的子层控件，还是会拦截已注册OnTouch事件的mMainLayout、mTreeNodeListView等控件。
        if (v != mDragView) {
            this.clearTempLayout();
            return false;
        }

        int[] posInScreen = new int[2];
        int pos = 0;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //mIsMove = false;

                mMoveStartX = event.getRawX();
                mMoveStartY = event.getRawY();

                v.getHitRect(mRect);

                pos = this.getPositionInListView((int)mMoveStartX, (int)mMoveStartY);
                if (-1 == pos) {
                    break;
                }

                mTreeNodeAdapter.setMoveState(true);
                mTreeNodeAdapter.setOperatingIndex(-1);

                mDragNode = mTreeMgr.getNodeByShowIndex(pos);
                mTreeNodeAdapter.setMoveNode(mDragNode);

                if (mDragNode.hasChilds() && mDragNode.isShowChilds()) {
                    mDragNode.setShowChilds(false);
                }
                this.resetTreeShowList();

                ViewGroup subView = Qi1siwole.getTreeItemView(this, R.layout.list_view_tree_item, mDragNode);
                Qi1siwole.addViewForTabs(this, subView, mDragNode);
                mDragView.addView(subView);
                
                break;
            case MotionEvent.ACTION_MOVE:
                //mIsMove = true;

                float curX = event.getRawX();
                float curY = event.getRawY();
                // TODO 先不让X方向移动
                int deltaX = 0; //(int)(curX - mMoveStartX);
                int deltaY = (int)(curY - mMoveStartY);

                int l = mRect.left + deltaX;
                int t = mRect.top + deltaY;
                int r = mRect.right + deltaX;
                int b = mRect.bottom + deltaY;

                // Y轴方向上，控制可移动的区域不超过ListView的范围
                int[] rangeY = rangeCoveredVertically(mDragView, mTreeListView);
                if (t > rangeY[1]) {
                    b -= t - rangeY[1];
                    t = rangeY[1];
                }
                if (t < rangeY[0]) {
                    b += rangeY[0] - t;
                    t = rangeY[0];
                }

                v.layout(l, t, r, b);


                // 检查位置是否有实质性变动
                posInScreen = getCenterPointOfView(v);
                pos = this.getPositionInListView(posInScreen[0], posInScreen[1]);
                if (-1 == pos) {
                    break;
                }

                TreeNode newNode = mTreeMgr.getNodeByShowIndex(pos);
                if (newNode == mDragNode) {
                    break;
                }

                // TODO 现在只能测试同Level相连的情况

                TreeNode tmp = mTreeMgr.changePositionTo(mDragNode, newNode, deltaY > 0);
                if (null == tmp) {
                    break;
                }

                mDragNode = tmp;
                mTreeNodeAdapter.setMoveNode(mDragNode);

                this.resetTreeShowList();

                mMoveStartX = curX;
                mMoveStartY = curY;

                v.getHitRect(mRect);

                /*
                mDragView.removeAllViews();
                subView = Qi1siwole.getTreeItemView(this, R.layout.list_view_tree_item, mDragNode);
                Qi1siwole.addViewForTabs(this, subView, mDragNode);
                mDragView.addView(subView);
                */

                break;
            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:
                mTreeNodeAdapter.setMoveState(false);
                mTreeNodeAdapter.setMoveNode(null);
                this.resetTreeShowList();
                this.clearTempLayout();
                break;
            default:
                break;
        }

        return true; // mIsMove;
    }

    /*
     * 将控件布局的Y轴方向控制在另一个控件布局范围内, 所得到该控件在Y轴方向上的坐标的范围
     */
    private int[] rangeCoveredVertically(View innerView, View outerView) {
        int[] res = new int[2];

        int[] outerGlobalPosition = getPositionOfView(outerView);
        int outerGlobalTop = outerGlobalPosition[1];

        View innerParentView = (View) innerView.getParent();
        int[] innerParentGlobalPosition = getPositionOfView(innerParentView);
        int innerParentGlobalTop = innerParentGlobalPosition[1];

        res[0] = outerGlobalTop - innerParentGlobalTop;

        res[1] = res[0] + outerView.getHeight() - innerView.getHeight();

        return res;
    }

    /*
     * 获得控件左上角的全局坐标
     */
    private int[] getPositionOfView(View view) {
        int[] point = new int[2];
        view.getLocationOnScreen(point);
        return point;
    }

    /*
     * 获得控件中心的全局坐标
     */
    private int[] getCenterPointOfView(View view) {
        int[] point = getPositionOfView(view);
        point[0] += view.getWidth() / 2;
        point[1] += view.getHeight() / 2;
        return point;
    }

    /*
     * 判断点在ListView中的position
     * @param x, y: global coordination
     */
    private int getPositionInListView(int x, int y) {
        int[] position = new int[2];
        mTreeListView.getLocationOnScreen(position);
        int pos = mTreeListView.pointToPosition(x - position[0], y - position[1]);
        if (0 > pos || pos >= mTreeShowList.size()) {
            return -1;
        }
        return pos;
    }

    // 清空 mTempLayout
    private void clearTempLayout() {
        if (null != mDragView) {
            mTempLayout.removeAllViews();
            mDragView = null;
        }
    }

    @Override
    public void onTreeItemNewClick() {
        mTreeMgr = new TreeMgr();
        this.resetTreeShowList();
        mTreeNodeAdapter.setOperatingIndex(-1);
        Qi1siwole.showToast(this, R.string.toast_tip_new_tree);
    }

    @Override
    public void onTreeItemOpenClick() {
        final List<String> nameList = mTreeMgr.queryTableTree();
        final String[] names = nameList.toArray(new String[nameList.size()]);
        if (nameList.isEmpty()) {
            Qi1siwole.showToast(this, R.string.toast_no_tree_data);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_tree_name_dialog_title);
        builder.setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = names[which];
                mTreeMgr.openTableTree(name);
                resetTreeShowList();
                mTreeNodeAdapter.setOperatingIndex(-1);
            }
        });
        builder.setNegativeButton(R.string.dialog_negative, null);
        builder.show();
    }

    @Override
    public void onTreeItemSaveClick() {
        if (mTreeMgr.isTreeNewCreated()) {
            int[] outTreeId = new int[1];
            if (!mTreeMgr.saveNewTableTree(outTreeId)) {
                Qi1siwole.showToast(this, R.string.toast_name_exists);
                return;
            }
            mTreeMgr.setCurTreeID(outTreeId[0]);
        }
        else {
            mTreeMgr.updateTableTree();
        }

        mTreeMgr.saveDataToTableTreeNode();
        mTreeNodeAdapter.setOperatingIndex(-1);
        Qi1siwole.showToast(this, R.string.toast_tip_save_tree);
    }

    @Override
    public void onTreeItemRefreshClick() {
        mTreeMgr.refreshTree();
        mTreeNodeAdapter.setOperatingIndex(-1);
        this.resetTreeShowList();
        mTreeNodeAdapter.setOperatingIndex(-1);
    }

    @Override
    public void onTreeItemFoldClick(int position) {
        TreeNode node = mTreeMgr.getNodeByShowIndex(position);
        if (null == node || !node.hasChilds()) {
            return;
        }

        node.setShowChilds(!node.isShowChilds());
        mTreeNodeAdapter.setOperatingIndex(-1);

        this.resetTreeShowList();
    }

    @Override
    public void onTreeItemNameClick(int position) {
        mTreeNodeAdapter.setOperatingIndex(position);
        mTreeNodeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTreeItemDragClick(ViewGroup viewGroup, int position) {
        this.clearTempLayout();
        this.addDragView(viewGroup, position);
    }

    private void addDragView(ViewGroup viewGroup, int position) {
        TreeNode node = mTreeMgr.getNodeByShowIndex(position);
        mTreeMgr.setLevelForChangePosition(node.getLevel());

        mTreeShowList.clear();
        mTreeShowList.addAll(mTreeMgr.getShowList());

        int index = mTreeShowList.indexOf(node);
        mTreeNodeAdapter.setOperatingIndex(index);

        mTreeNodeAdapter.notifyDataSetChanged();


        if (-1 == index) {
            return;
        }
        viewGroup = (ViewGroup) mTreeListView.getChildAt(index);

        View view = null;
        for (int i = viewGroup.getChildCount() - 1; i >= 0; --i) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView && ((TextView) child).getText().toString().equals(getApplicationContext().getString(R.string.drag))) {
                view = child;
                break;
            }
        }
        if (null == view) {
            return;
        }
        view = (ViewGroup)view.getParent();

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(view.getWidth(), view.getHeight());
        int left = 0;
        int top = 0;
        while (view.getParent() != mMainLayout) {
            left += view.getLeft();
            top += view.getTop();
            view = (ViewGroup)view.getParent();
        }
        lp.leftMargin = left;
        lp.topMargin = top;

        if (null == mDragView) {
            mDragView = new RelativeLayout(this);
        }
        mDragView.setBackgroundColor(Color.BLUE);
        mDragView.getBackground().setAlpha(64);

        mDragView.setOnTouchListener(this);

        mTempLayout.addView(mDragView, lp);
    }

    @Override
    public void onTreeItemAddClick(int position) {
        TreeNode node = mTreeMgr.getNodeByShowIndex(position);
        if (null == node) {
            return;
        }

        this.showDialogAddNode(node, true);
    }

    @Override
    public void onTreeItemDeleteClick(int position) {
        TreeNode node = mTreeMgr.getNodeByShowIndex(position);
        if (null == node) {
            return;
        }

        this.showDialogDeleteNode(node);
    }

    @Override
    public void onTreeItemEditClick(int position) {
        TreeNode node = mTreeMgr.getNodeByShowIndex(position);
        if (null == node) {
            return;
        }

        this.showDialogEditNode(node, true);
    }

    // 更新ListView的显示
    private void resetTreeShowList() {
        mTreeShowList.clear();
        mTreeShowList.addAll(mTreeMgr.getShowList());
        mTreeNodeAdapter.notifyDataSetChanged();
    }

    // 弹出对话框：删除Node
    private void showDialogDeleteNode(final TreeNode node) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(R.string.delete_node_dialog_message);
        builder.setPositiveButton(R.string.delete_node_dialog_positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTreeMgr.remove(node, true);
                        mTreeNodeAdapter.setOperatingIndex(-1);
                        resetTreeShowList();
                    }
                });
        builder.setNeutralButton(R.string.delete_node_dialog_neutral,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTreeMgr.remove(node, false);
                        mTreeNodeAdapter.setOperatingIndex(-1);
                        resetTreeShowList();
                    }
                });
        builder.setNegativeButton(R.string.dialog_negative, null);
        builder.show();
    }

    // 弹出对话框（输入框）：添加Node
    private void showDialogAddNode(final TreeNode node, final boolean isFirst) {
        if (null == node) {
            return;
        }
        final String parentName = node.getText();

        LinearLayout view = (LinearLayout)getLayoutInflater()
                .inflate(R.layout.rename_dialog, null);

        TextView tvOriLabel = (TextView)view.findViewById(R.id.tv_ori_label);
        TextView tvNewLabel = (TextView)view.findViewById(R.id.tv_new_label);
        TextView tvOriName = (TextView)view.findViewById(R.id.tv_ori_name);
        final EditText etNewName = (EditText)view.findViewById(R.id.et_new_name);
        etNewName.setFocusable(true);
        etNewName.setFocusableInTouchMode(true);
        etNewName.requestFocus();

        tvOriLabel.setText(R.string.parent_node_name);
        tvNewLabel.setText(R.string.child_node_name);

        tvOriName.setText(String.format("%s", parentName));
        tvOriName.setTextColor(Color.BLUE);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.add_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(isFirst ? R.string.add_dialog_positive : R.string.add_continue_dialog_positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputText = etNewName.getText().toString().trim();
                        if (inputText.isEmpty()) {
                            Qi1siwole.showToast(MainActivity.this, R.string.rename_toast_invalid_text);
                            showDialogAddNode(node, isFirst);
                            return;
                        }

                        mTreeMgr.add(inputText, node);
                        node.setShowChilds(true);

                        mTreeNodeAdapter.setOperatingIndex(-1);
                        resetTreeShowList();

                        showDialogAddNode(node, false);
                    }
                });
        builder.setNegativeButton(isFirst ? R.string.dialog_negative : R.string.add_complete_dialog_negative,
                null);
        builder.show();
    }

    // 弹出对话框（输入框）：编辑Node
    private void showDialogEditNode(final TreeNode node, boolean isFirst) {
        if (null == node) {
            return;
        }
        final String oriName = node.getText();

        LinearLayout view = (LinearLayout)getLayoutInflater()
                .inflate(R.layout.rename_dialog, null);

        TextView tvOriName = (TextView)view.findViewById(R.id.tv_ori_name);
        final EditText etNewName = (EditText)view.findViewById(R.id.et_new_name);
        etNewName.setFocusable(true);
        etNewName.setFocusableInTouchMode(true);
        etNewName.requestFocus();

        tvOriName.setText(String.format("%s", oriName));
        tvOriName.setTextColor(Color.RED);

        if (isFirst) {
            etNewName.setText(oriName);
            etNewName.selectAll();
        }
        else {
            etNewName.setText("");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.rename_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.dialog_positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputText = etNewName.getText().toString().trim();
                        if (inputText.isEmpty()) {
                            Qi1siwole.showToast(MainActivity.this, R.string.rename_toast_invalid_text);
                            showDialogEditNode(node, false);
                            return;
                        }
                        if (oriName.equals(inputText)) {
                            Qi1siwole.showToast(MainActivity.this, R.string.rename_toast_same_text);
                            showDialogEditNode(node, false);
                            return;
                        }

                        node.setText(inputText);

                        mTreeNodeAdapter.setOperatingIndex(-1);
                        resetTreeShowList();
                    }
                });
        builder.setNegativeButton(R.string.dialog_negative, null);
        builder.show();
    }


    private void initTreeNodesData() {
        TreeNode fruit = mTreeMgr.add("Fruit");
        TreeNode animal = mTreeMgr.add("Animal");
        mTreeMgr.add(Arrays.asList("apple", "banana"), fruit);
        TreeNode dog = mTreeMgr.add("dog", animal);
        mTreeMgr.add("yellow dog", dog);
    }

    private void initTreeNodesData2() {
        Stack<TreeNode> nodeStack = new Stack<>();
        Stack<View> viewStack = new Stack<>();
        viewStack.push(mMainLayout);
        nodeStack.push(mTreeMgr.add(mMainLayout.getClass().getName()));
        while (!viewStack.isEmpty()) {
            View top = viewStack.pop();
            TreeNode node = nodeStack.pop();
            if (top instanceof ViewGroup && ((ViewGroup) top).getChildCount() > 0) {
                for (int i = ((ViewGroup) top).getChildCount() - 1; i >= 0; --i) {
                    View child = ((ViewGroup) top).getChildAt(i);
                    viewStack.push(child);
                    nodeStack.push(mTreeMgr.add(child.getClass().getName(), node));
                }
            }
        }
    }

}
