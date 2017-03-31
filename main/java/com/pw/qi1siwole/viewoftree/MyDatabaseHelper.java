package com.pw.qi1siwole.viewoftree;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pw.qi1siwole.viewoftree.Tree.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qi1siwole on 2017/3/22.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final int VERSION_1_0 = 1;

    private static final String CREATE_TABLE_TREE_NODE = "create table TreeNode ("
            + "_id integer primary key autoincrement, "
            + "id integer, "
            + "pid integer, "   // parent_id
            + "name text, "
            + "is_show_child integer, "
            + "tid integer)";   // tree_id

    private static final String CREATE_TABLE_TREE = "create table Tree ("
            + "id integer primary key autoincrement, "
            + "level integer, "
            + "name text)";

    private MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TREE);
        db.execSQL(CREATE_TABLE_TREE_NODE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static MyDatabaseHelper mHelper;

    public static void initDatabaseHelper(Context ctx) {
        if (null == mHelper) {
            mHelper = new MyDatabaseHelper(ctx, "Tree.db", null, MyDatabaseHelper.VERSION_1_0);
        }
    }

    public static MyDatabaseHelper getInstance() {
        return mHelper;
    }

    /****************************** 为TreeMgr服务 ******************************/
    /*
     * 查询Tree表
     * @return:  List<String>
     */
    public static List<String> queryTableTree() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(String.format("select name from Tree"), null);

        List<String> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));

                list.add(name);
            } while (cursor.moveToNext());
        }

        return list;
    }

    /*
     * 查询Tree表
     * @param:   <Tree name>
     * @return:  <Tree ID>. If not exists, return -1.
     */
    public static int queryTableTree(String name, int[] outLevel) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(String.format("select * from Tree where name = '%s'", name), null);
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex("id"));

            if (null != outLevel) {
                outLevel[0] = cursor.getInt(cursor.getColumnIndex("level"));
            }
        }

        return id;
    }

    /*
     * 查询Tree表
     * @param:   <Tree ID>
     */
    public static boolean queryTableTree(int id, int[] outLevel) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(String.format("select * from Tree where id = %d", id), null);
        if (cursor.moveToFirst()) {
            if (null != outLevel) {
                outLevel[0] = cursor.getInt(cursor.getColumnIndex("level"));
            }
            return true;
        }
        return false;
    }


    /*
     * 插入数据到Tree表
     */
    public static void insertIntoTableTree(TreeNode root) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL(String.format("insert into Tree (name, level) values ('%s', %d)", root.getText(), root.getLevel()));
    }

    /*
     * 保存新数据到Tree表
     */
    public static boolean saveNewTableTree(TreeNode root, int[] outTreeId) {
        String name = root.getText();
        if (-1 != queryTableTree(name, null)) {
            return false;
        }

        insertIntoTableTree(root);

        int id = queryTableTree(name, null);
        if (-1 == id) {
            return false;
        }
        if (null != outTreeId) {
            outTreeId[0] = id;
        }

        return true;
    }

    /*
     * 更新Tree表的数据
     */
    public static boolean updateTableTree(int treeId, TreeNode root) {
        if (!queryTableTree(treeId, null)) {
            return false;
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL(String.format("update Tree set name = '%s', level = %d where id = %d", root.getText(), root.getLevel(), treeId));
        return true;
    }

    /*
     * 删除TreeNode表中数据
     * @param: <Tree ID>
     */
    public static void deleteFromTableTreeNode(int treeId) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL(String.format("delete from TreeNode where tid = %d", treeId));
    }

    /*
     * 插入数据到TreeNode表
     */
    public static void insertIntoTableTreeNode(List<TreeNode> list, int treeId) {
        if (null == list || 0 == list.size()) {
            return;
        }
        final String SQL_FORMAT = "insert into TreeNode (id, pid, name, is_show_child, tid)"
                + " values (%d, %d, '%s', %d, %d)";
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int id = 0; id < list.size(); ++id) {
                TreeNode node = list.get(id);
                int pid = -1;
                if (null != node.getParent()) {
                    pid = list.indexOf(node.getParent());
                }
                String name = node.getText();
                int is_show_child = node.isShowChilds() ? 1 : 0;
                db.execSQL(String.format(SQL_FORMAT, id, pid, name, is_show_child, treeId));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /*
     * 保存数据到TreeNode表
     */
    public static void saveDataToTableTreeNode(List<TreeNode> list, int treeId) {
        deleteFromTableTreeNode(treeId);
        insertIntoTableTreeNode(list, treeId);
    }

    /*
     * 查询TreeNode表
     * @param:  <Tree ID>
     * @return: List<TreeNode>
     */
    public static List<TreeNode> queryTableTreeNode(int treeId) {
        int[] levels = new int[1];
        if (!queryTableTree(treeId, levels)) {
            return null;
        }
        int rootLevel = levels[0];

        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(String.format("select * from TreeNode where tid = %d order by id asc", treeId), null);

        List<TreeNode> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                if (id != list.size()) {
                    return null;
                }

                int pid = cursor.getInt(cursor.getColumnIndex("pid"));
                if (pid >= id) {
                    return null;
                }

                String name = cursor.getString(cursor.getColumnIndex("name"));
                boolean is_show_child = 1 == cursor.getInt(cursor.getColumnIndex("is_show_child"));

                TreeNode node = new TreeNode(name);
                node.setShowChilds(is_show_child);
                if (-1 == pid) {
                    node.setParent(null);
                    node.setLevel(rootLevel);
                }
                else {
                    list.get(pid).addChild(node);
                }

                list.add(node);
            } while (cursor.moveToNext());
        }

        return list;
    }
}
