package com.cgj.accountbook.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.cgj.accountbook.bean.AccountDatabase;
import com.cgj.accountbook.bean.GroupsDatabase;
import com.cgj.accountbook.bean.IncomeRecordDatabase;
import com.cgj.accountbook.bean.LimitsDatabase;
import com.cgj.accountbook.bean.MyStringUtils;
import com.cgj.accountbook.bean.SrzcsDatabase;
import com.cgj.accountbook.util.LogUtil;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cgj.accountbook.dao.MyDataBase.TABLE_NAME_LIMIT;

/**
 * @author onono
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */
public class DatabaseUtil {

    private static final int VERSION = 1;
    private static final String DBNAME = "myAccountTest.db";
    private static final String TAG = "DatabaseUtil_Exception";
    private static DatabaseUtil mDatabaseUtil;
    private static DbManager db;
    public static final int SRZCS_INCOME = 0;
    public static final int SRZCS_OUTPUT = 1;
    public static final int SRZCS_OTHER = 2;

    private DatabaseUtil() {
    }

    public synchronized static DatabaseUtil getInstance() {
        if (mDatabaseUtil == null) {
            mDatabaseUtil = new DatabaseUtil();
            db = mDatabaseUtil.getDbManager();
        }
        return mDatabaseUtil;
    }

    private DbManager getDbManager() {
        if (db == null) {
            initDbManager();
        }
        return db;
    }

    private void initDbManager() {
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName(DBNAME)
                .setDbVersion(VERSION)
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) throws DbException {
                        db.getDatabase().enableWriteAheadLogging();
                    }
                })
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) throws DbException {
                        // TODO: 2019-12-25 升级数据库操作
                    }
                });
        try {
            db = x.getDb(daoConfig);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public int save(Object obj) {
        try {
            db.save(obj);
            return 1;
        } catch (DbException e) {
            LogUtil.logw(TAG, TAG + ":保存失败");
            return -1;
        }
    }

    /**
     * 查找：entityType表内type列是否含有condition的值
     *
     * @param entityType 表的类
     * @param type       需要查找的列名
     * @param condition  需要查找的type列内的值
     * @return true 找到，false 其他
     */
    public boolean isNameExist(Class<?> entityType, String type, String condition) {
        List<?> all = null;
        try {
            all = db.selector(entityType).where(type, "=", condition).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (all != null && all.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 修改：limits表中某一列（condition）列的limit和progress的值
     *
     * @param condition 用于查找需要修改的行，（type的值）
     * @param limit     预算
     * @param used      金额
     * @return 1成功 0失败
     */
    public int updataDataToLimitsLimit(String condition, String limit, String used) {
        float l = MyStringUtils.getString2Float(limit);
        float u = MyStringUtils.getString2Float(used);
        LimitsDatabase data = null;
        try {
            data = db.selector(LimitsDatabase.class).where("_type", "=", condition).findFirst();
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (data != null) {
            if (l != 0) {
                int pro = (int) ((u / l) * 100);
                data.setProgress(Integer.toString(pro));
                data.setLimit(limit);
                try {
                    db.update(data, "_limit", "_progress");
                    return 1;
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        } else {
            LogUtil.logi(TAG, TAG + ":更新limit表的limit失败");
        }
        return 0;
    }

    /**
     * 修改：更新limits表的use值（更新使用的钱）
     *
     * @param condition 用于查找需要修改的行，（type的值）
     * @param used      金额
     */
    public void updataDataToLimitsUsed(String condition, float used) {
        LimitsDatabase data = null;
        try {
            data = db.selector(LimitsDatabase.class).where("_type", "=", condition).findFirst();
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (data != null) {
            String used1 = MyStringUtils.get2dotFloat(used);
            //            obj.setType(condition);
            data.setUsed(used1);
            try {
                db.update(data, "_used");
            } catch (DbException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.logi(TAG, TAG + ":更新limit表的used失败");
        }

    }

    /**
     * 获得srzcr表的month月a列的值
     *
     * @param a     哪一列
     * @param month 哪个月
     * @return
     */
    public String getSRGL(int a, String month) {
        String s = "0";
        List<?> list = null;
        try {
            list = db.selector(SrzcsDatabase.class).where("_month", "=", month).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (list != null) {
            SrzcsDatabase o = (SrzcsDatabase) list.get(0);
            switch (a) {
                case SRZCS_INCOME:
                    s = o.getSr();
                    break;
                case SRZCS_OUTPUT:
                    s = o.getZc();
                    break;
                case SRZCS_OTHER:
                    s = o.getOther();
                    break;
                default:
                    break;
            }
            return s;
        } else {
        }
        if (s == null) {
            return "0";
        } else {
            return s;
        }

    }

    /**
     * 更新srzcr表
     *
     * @param month 月份
     * @param t     哪一列
     * @param value 值
     */
    public void updateGLSR(String month, int t, String value) {

        List<SrzcsDatabase> all = null;
        try {
            all = db.selector(SrzcsDatabase.class).where("_month", "=", month).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (all != null) {
            SrzcsDatabase obj = all.get(0);
            switch (t) {
                case SRZCS_INCOME:
                    obj.setSr(value);
                    try {
                        db.update(obj, "_sr");
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    break;
                case SRZCS_OUTPUT:
                    obj.setZc(value);
                    try {
                        db.update(obj, "_zc ");
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    break;
                case SRZCS_OTHER:
                    obj.setOther(value);
                    try {
                        db.update(obj, "_other");
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 获取limits表的数据
     *
     * @return arraylist, 内含hashmap内“String，String”
     */
    public ArrayList<HashMap<String, String>> getPartLimitsDatas() {
        //查找limits表下所有内容
        List<LimitsDatabase> all = null;
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        try {
            all = db.findAll(LimitsDatabase.class);

            if (all != null) {
                for (int i = 0; i < all.size(); i++) {
                    HashMap<String, String> stringStringHashMap = new HashMap<>();
                    String type = all.get(i).getType();
                    String color = all.get(i).getColor();
                    String used = all.get(i).getUsed();
                    String progress = all.get(i).getProgress();
                    String limit = all.get(i).getLimit();
                    int id = all.get(i).getId();
                    stringStringHashMap.put("id", id + "");
                    stringStringHashMap.put("limit", limit);
                    stringStringHashMap.put("progress", progress);
                    stringStringHashMap.put("used", used);
                    stringStringHashMap.put("type", type);
                    stringStringHashMap.put("color", color);
                    list.add(stringStringHashMap);
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * 初始化limits表
     */
    public void initLimitsDatabase() {
        for (int i = 0; i < MyStringUtils.templimitsname.length; i++) {
            LimitsDatabase limitsDatabase = new LimitsDatabase();
            limitsDatabase.setType(MyStringUtils.templimitsname[i]);
            limitsDatabase.setProgress("0");
            limitsDatabase.setUsed("0");
            limitsDatabase.setColor(MyStringUtils.templimitscolor[i]);
            limitsDatabase.setLimit("0");
            try {
                db.save(limitsDatabase);
            } catch (DbException e) {
                LogUtil.logw(TAG, "M:initLimitsDatabse：" + e);
            }
        }
    }

    /**
     * 获取首页展示时的数据
     *
     * @return
     */
    public ArrayList<Map<String, Object>> getHomeData() {
        int count = 0;//当月总消费
        int pro = 0;//当月消费占比
        Map<String, Object> map;
        List<LimitsDatabase> used_nozero = null;
        //当月总消费计算
        ArrayList<Map<String, Object>> datas = new ArrayList<>();
        try {
            used_nozero = db.selector(LimitsDatabase.class).where("used", ">", "0").findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < used_nozero.size(); i++) {
            float v = Float.parseFloat(used_nozero.get(i).getUsed());
            count+=v;
        }
        for (int i = 0; i < used_nozero.size(); i++) {
            map = new HashMap<>();
            String u = used_nozero.get(i).getUsed();
            if (u != null) {
                float used = Float.parseFloat(u);
                int pro1 = (int) ((Float.parseFloat(u) / count) * 100);
                map = new HashMap<>();
                map.put("used", u);
                map.put("color", used_nozero.get(i).getColor());
                map.put("type", used_nozero.get(i).getType());
                map.put("pro", pro1);
                datas.add(map);

            }
        }
        LogUtil.logi(TAG, "：总花费:" + count);
        return datas;
    }

    /**
     * 获取limit表中某一列（type）中某一行（name）的进度或限额
     *
     * @param type 列名
     * @param type
     * @return
     */
    public String getProORLimit(String type, String name) {
        String s = "0";
        LimitsDatabase first = null;
        try {
            first = db.selector(LimitsDatabase.class).where("_type", "=", name).findFirst();
        } catch (DbException e) {
            e.printStackTrace();
        }
        switch (type) {
            case "_used":
                s = first.getUsed();
                break;
            case "_limit":
                s = first.getLimit();
                break;
            case "_progress":
                s = first.getProgress();
                break;
            default:
                break;
        }
        if (s == null) {
            return "0";
        } else {
            return s;
        }

    }

    /**
     * 查询特定表中，type=condition的数据总数
     *
     * @param entityType 表的类
     * @param type       列名
     * @param condition  条件
     * @return int总数
     */
    public int getCount(Class entityType, String type, String condition) {
        int count = 0;
        List all = null;
        try {
            all = db.selector(entityType).where(type, "=", condition).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (all != null) {
            return all.size();
        } else {
            return 0;
        }
    }

    /**
     * 查询特定表中，type=condition的对象
     *
     * @param entityType 表的类
     * @param type       列名
     * @param condition  条件
     * @return List符合条件的对象
     */
    public List getData(Class entityType, String type, String condition) {
        List all = null;
        try {
            all = db.selector(entityType).where(type, "=", condition).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (all != null) {
            return all;
        } else {
            return null;
        }
    }

    /**
     * 获取entityType类的所有数据
     *
     * @param entityType 表的类名
     * @return
     */
    public List findAll(Class entityType) {
        List all = null;
        try {
            all = db.findAll(entityType);
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (all != null) {
            return all;
        } else {
            return null;
        }
    }

    /**
     * 初始化Account表
     */
    public void initAccountDatabase() {
        AccountDatabase accountDatabase = new AccountDatabase();
        accountDatabase.setId(1);
        accountDatabase.setMonth("初始化");
        try {
            db.save(accountDatabase);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化GroupDatabase表
     */
    public void initGroupDatabase() {
        GroupsDatabase groupsDatabase = new GroupsDatabase();
        //        String month = MyStringUtils.getDate("month");
        Calendar calendar = Calendar.getInstance();
        int i = calendar.get(Calendar.MONTH);
        String month = null;
        switch (i) {
            case 0:
                month = "一月";
                break;
            case 1:
                month = "二月";
                break;
            case 2:
                month = "三月";
                break;
            case 3:
                month = "四月";
                break;
            case 4:
                month = "五月";
                break;
            case 5:
                month = "六月";
                break;
            case 6:
                month = "七月";
                break;
            case 7:
                month = "八月";
                break;
            case 8:
                month = "九月";
                break;
            case 9:
                month = "十月";
                break;
            case 10:
                month = "十一月";
                break;
            case 11:
                month = "十二月";
                break;
            default:
                break;
        }
        groupsDatabase.setOther(MyStringUtils.getSysNowTime(2));
        groupsDatabase.setMonth(month);
        try {
            db.save(groupsDatabase);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除数据库
     */
    public void dropDb() {
        try {
            db.dropDb();
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取income_record表的数据
     *
     * @return ArrayList 收入数据
     */
    public ArrayList<HashMap<String, String>> getIncomes() {
        ArrayList<HashMap<String, String>> lists = new ArrayList<>();
        List<IncomeRecordDatabase> all = null;
        try {
            all = db.findAll(IncomeRecordDatabase.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (all != null && all.size() != 0) {
            for (int i = 0; i < all.size(); i++) {
                HashMap<String, String> map = new HashMap<>();
                IncomeRecordDatabase database = all.get(i);
                map.put("_money", database.getIncome());
                map.put("_time", database.getTime());
                map.put("_detail", database.getDetail());
                lists.add(map);
            }

        }
        return lists;
    }

    /**
     * 插入：向income记录表中加入数据
     *
     * @param myData 数据map
     * @return
     */
    public long inserDataToIncomeRecord(HashMap<String, String> myData) {
        String formatTime = MyStringUtils.getSysNowTime(1);
        IncomeRecordDatabase incomeRecordDatabase = new IncomeRecordDatabase();
        incomeRecordDatabase.setTime(formatTime);
        incomeRecordDatabase.setIncome(myData.get("_income"));
        incomeRecordDatabase.setDetail(myData.get("_detail"));
        try {
            db.save(incomeRecordDatabase);
            return 1;
        } catch (DbException e) {
            return -1;
        }
    }

    /**
     * 初始化收入支出表
     *
     * @return
     */
    public void initSrzcsDatabase() {
        SrzcsDatabase srzcsDatabase;
        for (int i = 0; i < 12; i++) {
            srzcsDatabase = new SrzcsDatabase();
            srzcsDatabase.setId(i);
            srzcsDatabase.setSr("0");
            srzcsDatabase.setZc("0");
            srzcsDatabase.setMonth(MyStringUtils.monthStrings[i]);
            String nowTime = MyStringUtils.getSysNowTime(2);
            srzcsDatabase.setOther(nowTime);
            try {
                db.save(srzcsDatabase);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除accounts表中数据
     *
     * @param entityType 表的类名
     * @param type       哪一列（列名）
     * @param condition  哪一列中哪一行（值）
     * @return
     */
    public int deleteData(Class entityType, String type, String condition) {
        int i = 0;
        try {
            i = db.delete(entityType, WhereBuilder.b(type, "=", condition));

        } catch (DbException e) {
            e.printStackTrace();
        }
        return i;
    }

    /**
     * 向limits表中插入颜色值
     * @param type
     * @param value
     * @return
     */
    public void insertDataToLimitColor(String type, String value) {
        LimitsDatabase database = new LimitsDatabase();
        database.setType(type);
        database.setLimit("0");
        database.setUsed("0");
        database.setProgress("0");
        database.setColor(value);
        try {
            db.save(database);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除：将limits表中的used，progress值置零
     *
     *
     */
    public void setDataToZero() {
        try {
            db.delete(LimitsDatabase.class,WhereBuilder.b("_used","=","0"));
            db.delete(LimitsDatabase.class,WhereBuilder.b("_progress","=","0"));
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

}
