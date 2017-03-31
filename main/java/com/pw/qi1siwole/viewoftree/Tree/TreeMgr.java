package com.pw.qi1siwole.viewoftree.Tree;

import com.pw.qi1siwole.viewoftree.MyDatabaseHelper;
import com.pw.qi1siwole.viewoftree.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by user on 2017/3/22.
 */

public class TreeMgr {
    private List<TreeNode> mTreeNodes = null;
    private TreeNode mRootNode = null;
    private List<Integer> mIndexes = null;
    private int mTableTreeID;

    public TreeMgr() {
        this(0);
    }

    public TreeMgr(int initLevel) {
        mTreeNodes = new ArrayList<>();
        mIndexes = new ArrayList<>();

        mRootNode = new TreeNode();
        mRootNode.setShowChilds(true);
        mRootNode.setLevel(initLevel - 1);

        mTreeNodes.add(mRootNode);
        mTableTreeID = -1;
    }

    public TreeNode getDeepCopyOfTree(TreeNode root) {
        if (null == root) {
            return null;
        }

        TreeNode copiedRoot = new TreeNode(root.getText());
        copiedRoot.setLevel(root.getLevel());
        copiedRoot.setShowChilds(root.isShowChilds());

        if (root.hasChilds()) {
            for (TreeNode child : root.getChilds()) {
                TreeNode copiedChild = getDeepCopyOfTree(child);
                copiedRoot.addChild(copiedChild);
            }
        }

        return copiedRoot;
    }



    public List<TreeNode> getRealList() {
        return mTreeNodes;
    }

    public List<TreeNode> getShowList() {
        List<TreeNode> showList = new ArrayList<>();

        mIndexes.clear();
        for (int i = 0; i < mTreeNodes.size(); ++i) {
            TreeNode node = mTreeNodes.get(i);
            if (node.isShow()) {
                showList.add(node);
                mIndexes.add(Integer.valueOf(i));
            }
        }

        return showList;
    }

    public TreeNode add(String text) {
        TreeNode node = new TreeNode(text);
        return add(node);
    }

    public TreeNode add(TreeNode node) {
        return add(node, mRootNode);
    }

    public TreeNode add(String text, TreeNode parent) {
        TreeNode node = new TreeNode(text);
        return add(node, parent);
    }

    public List<TreeNode> add(List<String> list) {
        List<TreeNode> res = new ArrayList<>();
        for (String text: list) {
            res.add(add(text));
        }
        return res;
    }

    public List<TreeNode> add(List<String> list, TreeNode parent) {
        List<TreeNode> res = new ArrayList<>();
        for (String text: list) {
            res.add(add(text, parent));
        }
        return res;
    }

    public TreeNode add(int pos, TreeNode node, TreeNode parent) {
        if (null == node || null == parent) {
            return null;
        }

        if (pos < 0 || pos > (parent.hasChilds() ? parent.getChilds().size() : 0)) {
            return null;
        }

        int index = 0;

        if (0 == pos) {
            index = mTreeNodes.indexOf(parent);
            if (-1 == index) {
                return null;
            }
            ++index;
        }
        else if (parent.hasChilds()) {
            if (pos == parent.getChilds().size()) {
                TreeNode lastChild = parent;
                while (lastChild.hasChilds()) {
                    List<TreeNode> childs = lastChild.getChilds();
                    lastChild = childs.get(childs.size() - 1);
                }
                index = mTreeNodes.indexOf(lastChild);
                if (-1 == index) {
                    return null;
                }
                ++index;
            }
            else {
                index = mTreeNodes.indexOf(parent.getChilds().get(pos));
                if (-1 == index) {
                    return null;
                }
            }
        }

        List<TreeNode> list = getListFromTree(node);

        // 更新新添加子树的各个节点的Level值
        int deltaLevel = parent.getLevel() + 1 - node.getLevel();
        if (0 != deltaLevel) {
            for (TreeNode e: list) {
                e.setLevel(e.getLevel() + deltaLevel);
            }
        }

        for (int i = list.size() - 1; i >= 0; --i) {
            TreeNode e = list.get(i);
            mTreeNodes.add(index, e);
        }

        parent.addChild(pos, node);

        return node;
    }

    public TreeNode add(TreeNode node, TreeNode parent) {
        if (null == node || null == parent) {
            return null;
        }
        if (parent.hasChilds()) {
            return add(parent.getChilds().size(), node, parent);
        }
        return add(0, node, parent);
    }

    public List<TreeNode> getListFromTree(TreeNode root) {
        if (null == root) {
            return null;
        }
        List<TreeNode> list = new ArrayList<>();

        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode top = stack.pop();
            list.add(top);
            if (top.hasChilds()) {
                List<TreeNode> children = top.getChilds();
                for (int i = children.size() - 1; i >= 0; --i) {
                    TreeNode child = children.get(i);
                    stack.push(child);
                }
            }
        }

        return list;
    }

    public boolean remove(TreeNode node, boolean containsChilds) {
        if (node == mRootNode) {
            return false;
        }

        TreeNode parent = node.getParent();
        if (null == parent) {
            return false;
        }

        // 包含的分支全删掉
        if (containsChilds) {
            if (node.hasChilds()) {
                for (int i = node.getChilds().size() - 1; i >= 0; --i) {
                    TreeNode child = node.getChilds().get(i);
                    remove(child, true);
                }
            }
        }
        // 只删一个
        else {
            if (node.hasChilds()) {
                int index = parent.getChilds().indexOf(node);
                for (int i = node.getChilds().size() - 1; i >= 0; --i) {
                    TreeNode child = node.getChilds().get(i);
                    parent.addChild(index, child);
                }
            }
        }

        mTreeNodes.remove(node);
        parent.removeChild(node);

        return true;
    }

    // 注：暂时不考虑父子关系互换的情况
    public TreeNode changeParentTo(TreeNode node, TreeNode parent, int pos) {
        if (null == node || null == parent) {
            return null;
        }

        if (-1 == mTreeNodes.indexOf(parent)) {
            return null;
        }

        if (pos < 0 || pos > (parent.hasChilds() ? parent.getChilds().size() : 0)) {
            return null;
        }

        if (parent == node.getParent()) {
            int index = parent.getChilds().indexOf(node);
            if (index == pos) {
                return node;
            }
            if (pos > index) {
                --pos;
            }
        }

        if (-1 != mTreeNodes.indexOf(node)) {
            TreeNode tmp = getDeepCopyOfTree(node);
            remove(node, true);
            node = tmp;
        }

        add(pos, node, parent);

        return node;
    }

    // 注：如果节点未展开，则不去主动展开它；只能平级互换，且上级需提前展开
    public TreeNode changePositionTo(TreeNode node, TreeNode targetPos, boolean isDown) {
        if (null == node || null == targetPos) {
            return null;
        }
        if (null == node.getParent() || null == targetPos.getParent()) {
            return null;
        }

        if (targetPos == mRootNode) {
            return null;
        }

        int curLevel = node.getLevel();
        int nextLevel = targetPos.getLevel();
        if (curLevel == nextLevel) {
            int pos = targetPos.indexOfParent();
            if (isDown) {
                ++pos;
            }
            return changeParentTo(node, targetPos.getParent(), pos);
        }
        // TODO 还有不同Level的情况

        return null;
    }

    // 上级保持原样；显示出来的、与给定等级同级的，都折叠，即下级均不显示
    public void setLevelForChangePosition(int level) {
        for (TreeNode node: mTreeNodes) {
            if (node.getLevel() == level) {
                node.setShowChilds(false);
            }
        }
    }

    public void refreshTree() {
        mTreeNodes = getListFromTree(mRootNode);
    }

    public TreeNode getNodeByShowIndex(int i) {
        if (i >= 0 && i < mIndexes.size()) {
            int index = mIndexes.get(i);
            if (index >= 0 && index < mTreeNodes.size()) {
                return mTreeNodes.get(index);
            }
        }
        return null;
    }

    public static int getFlag(TreeNode node) {
        int flag = 0;
        if (!node.hasChilds()) {
            flag = R.string.flag_no_child;
        }
        else {
            flag = node.isShowChilds() ? R.string.flag_unfold : R.string.flag_fold;
        }

        return flag;
    }

    /*
     * 【数据库操作】
     */
    public void setCurTreeID(int id) {
        mTableTreeID = id;
    }

    public boolean isTreeNewCreated() {
        return -1 == mTableTreeID;
    }

    public boolean saveNewTableTree(int[] outTreeId) {
        return MyDatabaseHelper.saveNewTableTree(mRootNode, outTreeId);
    }

    public boolean updateTableTree() {
        if (isTreeNewCreated()) {
            return false;
        }
        return MyDatabaseHelper.updateTableTree(mTableTreeID, mRootNode);
    }

    public void saveDataToTableTreeNode() {
        if (isTreeNewCreated()) {
            return;
        }
        MyDatabaseHelper.saveDataToTableTreeNode(mTreeNodes, mTableTreeID);
    }

    public static List<String> queryTableTree() {
        return MyDatabaseHelper.queryTableTree();
    }

    public List<TreeNode> queryTableTreeNode() {
        if (isTreeNewCreated()) {
            return null;
        }
        return MyDatabaseHelper.queryTableTreeNode(mTableTreeID);
    }

    public static int queryTableTree(String name, int[] outLevel) {
        return MyDatabaseHelper.queryTableTree(name, outLevel);
    }

    public void openTableTree(String name) {
        mTableTreeID = queryTableTree(name, null);
        mTreeNodes = queryTableTreeNode();
        mRootNode = mTreeNodes.get(0);
    }
}
