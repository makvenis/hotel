package com.makvenis.hotel.xutils;

/* 数据库的增删改查 */

import org.xutils.DbManager;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.db.table.DbModel;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppHelpMethod {

    /**
     * try {
     * db.findById(person.class, 1);//通过主键的值来进行查找表里面的数据
     * db.findFirst(person.class);//返回当前表里面的第一条数据
     * List<person> findAll = db.findAll(person.class);//返回当前表里面的所有数据
     * db.findDbModelAll(new SqlInfo("select * from person where age > 25"));
     * DbModel model = db.findDbModelFirst(new SqlInfo("select * from person where age > 25"));
     * model.getString("age");//model相当于游标
     * List<person> findAll2 = db.selector(person.class).expr("age >10").findAll();//主要是用来进行一些特定条件的查找
     * } catch (DbException e) {
     * }
     */
    /* 通过SQLite语句查询，并且给定查询的字段----->通用方法(查询类型为string) */
    public static List<Map<String,Object>>  queryBySql(String sql,String[] condation){
        try {
            DbManager db = DBHelp.initDb();
            //获取条件
            List<Map<String,Object>> mData=new ArrayList<>();
            List<DbModel> model = db.findDbModelAll(new SqlInfo(sql));
            for (int i = 0; i < model.size(); i++) {
                DbModel dbModel = model.get(i);
                Map<String,Object> map = new HashMap<>();
                for (int j = 0; j < condation.length; j++) {
                    map.put(condation[j],dbModel.getString(condation[j]));
                }
                mData.add(map);
            }
            return mData;
        } catch (DbException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    /* 判断当前数据是否存在 */
    public static boolean isExistBool(String sql,String[] condation){
        List<Map<String, Object>> maps = AppHelpMethod.queryBySql(sql, condation);
        if(maps.size() > 0){
            return true;
        }else {
            return false;
        }
    }

    /* 添加数据到数据库 */
    public static boolean add(String mUserKey,String mUserData){
        try {
            DbManager db = DBHelp.initDb();
            JavaXutilsUser e=new JavaXutilsUser();
            e.setKey(mUserKey);
            e.setData(mUserData);
            db.save(e);
            //检查是否插入了数据库
            boolean existBool = AppHelpMethod.isExistBool("select data from user where key = 'localuser'"
                    , new String[]{"data"});
            if(existBool){
                return true;
            }else {
                return false;
            }
        } catch (DbException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    /* 更新数据 */
    public static void update(String mUserKey,String mUserData){
        try {
            DbManager db = DBHelp.initDb();
            List<JavaXutilsUser> list = db.findAll(JavaXutilsUser.class);
            if(list.size() == 1){
                JavaXutilsUser user = list.get(0);
                user.setData(mUserData);
                db.update(user);
            }else {
                new IllegalArgumentException("用户数据库出现多条数据 "+list.size());
            }
        } catch (DbException e1) {
            e1.printStackTrace();
        }
    }
}
