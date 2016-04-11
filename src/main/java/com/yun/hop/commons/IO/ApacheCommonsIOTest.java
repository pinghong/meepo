package com.yun.hop.commons.IO;

import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Created by hongping on 2016/4/8.
 * description：
 */
public class ApacheCommonsIOTest {

    public void fileUtilsTest(){
        System.out.println(FileUtils.byteCountToDisplaySize(102402400l));
    }

    public static void main(String[] args) {
        ApacheCommonsIOTest apacheCommonsIOTest= new ApacheCommonsIOTest();
        apacheCommonsIOTest.fileUtilsTest();
    }
}
