package com.cgj.accountbook;

import com.cgj.accountbook.bean.GroupsDatabase;
import com.cgj.accountbook.dao.DatabaseUtil;

import org.junit.Test;
import org.w3c.dom.ls.LSException;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    List list =null;
    @Test
    public void addition_isCorrect() {
        DatabaseUtil util = DatabaseUtil.getInstance();
        boolean month = util.isNameExist(GroupsDatabase.class, "_month");
        System.out.println("month:"+month);
    }
}