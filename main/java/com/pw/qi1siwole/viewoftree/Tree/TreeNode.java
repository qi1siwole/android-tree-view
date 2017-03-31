package com.pw.qi1siwole.viewoftree.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/3/22.
 */

public class TreeNode {
    private static final String DEFAULT_TEXT = "<node>";

    private String mText;
    private int mLevel;
    private TreeNode mParent = null;
    private List<TreeNode> mChilds = null;
    private boolean mIsShowChilds;

    public TreeNode() {
        this(DEFAULT_TEXT);
    }

    public TreeNode(String text) {
        this(text, null);
    }

    public TreeNode(TreeNode parent) {
        this(DEFAULT_TEXT, parent);
    }

    public TreeNode(String text, TreeNode parent) {
        mText = text;
        setParent(parent);
        mChilds = null;
        mIsShowChilds = false;
    }

    public TreeNode addChild() {
        TreeNode node = new TreeNode();
        return addChild(node);
    }

    public TreeNode addChild(String text) {
        TreeNode node = new TreeNode(text);
        return addChild(node);
    }

    public TreeNode addChild(TreeNode child) {
        if (null == child) {
            return null;
        }

        TreeNode oriParent = child.getParent();
        if (null != oriParent) {
            oriParent.removeChild(child);
        }
        child.setParent(this);

        if (null == mChilds) {
            mChilds = new ArrayList<>();
        }
        mChilds.add(child);

        return child;
    }

    public TreeNode addChild(int pos, TreeNode child) {
        if (null == child || pos < 0 || null != mChilds && pos > mChilds.size()) {
            return null;
        }

        TreeNode oriParent = child.getParent();
        if (null != oriParent) {
            oriParent.removeChild(child);
        }
        child.setParent(this);

        if (null == mChilds) {
            mChilds = new ArrayList<>();
        }
        mChilds.add(pos, child);

        return child;
    }

    public boolean removeChild(TreeNode child) {
        if (null == child || !hasChilds()) {
            return false;
        }

        for (TreeNode node: mChilds) {
            if (node == child) {
                mChilds.remove(node);
                child.setParent(null);
                return true;
            }
        }

        return false;
    }

    // 是父节点的第几个子节点
    public int indexOfParent() {
        if (null == mParent) {
            return -1;
        }
        return mParent.getChilds().indexOf(this);
    }

    public List<TreeNode> getChilds() {
        return mChilds;
    }

    public boolean hasChilds() {
        return null != mChilds && !mChilds.isEmpty();
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public TreeNode getParent() {
        return mParent;
    }

    public void setParent(TreeNode parent) {
        mParent = parent;
        if (null == mParent) {
            mLevel = 0;
        }
        else {
            mLevel = mParent.getLevel() + 1;
        }
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public boolean isShowChilds() {
        return mIsShowChilds;
    }

    public void setShowChilds(boolean isShowChilds) {
        mIsShowChilds = isShowChilds;
    }

    public boolean isShow() {
        TreeNode parent = mParent;
        while (null != parent) {
            if (!parent.isShowChilds()) {
                return false;
            }
            parent = parent.getParent();
        }

        return true;
    }
}
