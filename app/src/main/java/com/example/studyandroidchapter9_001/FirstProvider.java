package com.example.studyandroidchapter9_001;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.Locale;

public class FirstProvider extends ContentProvider {

    private static UriMatcher matcher
            = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int WORDS = 1;
    private static final int WORD = 2;
    private MyDatabaseHelper dbOpenHelper;
    static {
        // 为UriMather注册两个Uri
        matcher.addURI(Words.AUTHORITY, "words", WORDS);
        matcher.addURI(Words.AUTHORITY, "word", WORD);
    }

    // 第一次创建该ContentProvider时调用该方法
    @Override
    public boolean onCreate(){
        Log.e("liujianDebug", "onCreate");
        dbOpenHelper = new MyDatabaseHelper(this.getContext(), "myDict.db3", 1);
        return true;
    }
    // 该方法的返回值代表了该COntentProvider所提供数据的MIME类型
    @Override
    public String getType(Uri uri){
        switch (matcher.match(uri)){
            // 如果操作的数据是多项记录
            case WORDS:
                return "vnd.android.cursor.dir/com.example.studyandroidchapter9_001";
            case WORD:
                return "vnd.android.cursor.item/com.example.studyandroidchapter9_001";
            default:
                throw new IllegalArgumentException("未知Uri:" +uri);
        }
    }
    // 实现查询方法，该方法应该返回查询到的Cursor
    @Override
    public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sortOrder){
        Log.e("liujianDebug", "query:"+where);
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        switch (matcher.match(uri)){
            // 如果Uri参数代表操作全部数据项
            case WORDS:
                // 执行查询
                return db.query("dict", projection, where, whereArgs, null, null, sortOrder);
            case WORD:
                // 解析出想查询的记录id
                long id = ContentUris.parseId(uri);
                String whereClause = Words.Word._ID + "="+id;
                // 如果原来的where子句存在，拼接where子句
                if (where != null && !"".equals(where)){
                    whereClause = whereClause+" and " + where;
                }
                return db.query("dict", projection, whereClause, whereArgs, null, null, sortOrder);
            default:
                throw new IllegalArgumentException("未知Uri:" + uri);
        }
    }
    // 实现插入的方法，该方法应该返回新插入的记录的Uri
    @Override
    public Uri insert(Uri uri, ContentValues values){
        Log.e("liujianDebug", uri+"insert被调用==="+"   values参数为:"+values);
        //获得数据库实例
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        switch (matcher.match(uri)){
            case WORDS:
                // 插入数据，返回插入记录的ID
                long rowId = db.insert("dict", Words.Word._ID, values);
                // 如果插入成功返回uri
                if (rowId > 0){
                    // 在已有的uri后面加上id
                    Uri wordUri = ContentUris.withAppendedId(uri, rowId);
                    // 通知数据已经改变
                    getContext().getContentResolver().notifyChange(wordUri, null);
                    return wordUri;
                }
                break;
            default:
                throw new IllegalArgumentException("未知Uri:" + uri);
        }
        return null;
    }
    // 实现删除方法， 该方法应该返回被更新的记录条数
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs){
        Log.e("liujianDebug", uri + "update被调用====" + "   values:"+values+"   where:"+where);

        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        // 记录所修改的记录数
        int num = 0;
            switch (matcher.match(uri)){
            // 如果Uri参数代表操作全部数据项
            case WORDS:
                num = db.update("dict", values, where, whereArgs);
                break;
            case WORD:
                // 解析出想修改的记录id
                long id = ContentUris.parseId(uri);
                String whereClause = Words.Word._ID + "=" + id;
                // 如果原来的where子句存在，拼接where子句
                if (where != null && !where.equals("")){
                    whereClause = whereClause + "and" + where;
                }
                num = db.update("dict", values, whereClause, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("未知uri:" + uri);
        }
        // 通知数据已经改变
        getContext().getContentResolver().notifyChange(uri, null);

        return num;
    }
    // 实现删除方法， 该方法应该返回被删除的记录条数
    @Override
    public int delete(Uri uri, String where, String[] whereArgs){
        Log.e("liujianDebug", uri+"delete方法被调用==="+"   where:"+where);
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        // 记录所删除的记录数
        int num = 0;
        // 对uri进行匹配
        switch (matcher.match(uri)){
            case WORDS:
                num = db.delete("dict", where, whereArgs);
                break;
            case WORD:
                // 解析出所需删除的记录id
                long id = ContentUris.parseId(uri);
                String whereClause = Words.Word._ID + "=" +id;
                // 如果原来的where子句存在， 拼接where子句
                if (where != null && !where.equals("")){
                    whereClause = whereClause + " and " +where;
                }
                num = db.delete("dict", whereClause, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("未知uri:" + uri);
        }
        // 通知数据已经改变
        getContext().getContentResolver().notifyChange(uri, null);
        return num;
    }
}
