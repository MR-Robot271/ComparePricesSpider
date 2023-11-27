package com.kb.pojo;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * @Description: Excel文件内容的最终结果实体类，主要被EasyExcel使用 将所有价格放在一行 添加最高价、最低价、平均价
 * @Date: 2023/11/14
 */
public class ResultExcel {
    // 产品名称
    @ExcelProperty("产品名称")
//     com.alibaba.excel.util.FieldUtils#resolveCglibFieldName，
//     此方法其中有个策略：如果字段名称第一个是字母是小写第二个是大写，则此方法会将第一个字母变成大写
//     char firstChar = fieldName.charAt(0);
//     char secondChar = fieldName.charAt(1);
//     if (Character.isUpperCase(firstChar) == Character.isUpperCase(secondChar)) {
//         return fieldName;
//     }
//     if (Character.isUpperCase(firstChar)) {
//         return buildFieldName(Character.toLowerCase(firstChar), fieldName);
//     }
    //private String pName;
    private String projectName;

    // 第一个产品价格
    @ExcelProperty("产品价格1")
    private Float projectPrice1;

    // 第二个产品价格
    @ExcelProperty("产品价格2")
    private Float projectPrice2;

    // 第三个产品价格
    @ExcelProperty("产品价格3")
    private Float projectPrice3;

    // 最高价
    @ExcelProperty("最高价")
    private Float maxPrice;

    // 最低价
    @ExcelProperty("最低价")
    private Float minPrice;

    // 平均价
    @ExcelProperty("平均价")
    private Float avgPrice;

    public ResultExcel(String projectName, Float projectPrice1, Float projectPrice2, Float projectPrice3, Float maxPrice, Float minPrice, Float avgPrice) {
        this.projectName = projectName;
        this.projectPrice1 = projectPrice1;
        this.projectPrice2 = projectPrice2;
        this.projectPrice3 = projectPrice3;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.avgPrice = avgPrice;
    }

    public ResultExcel(String projectName, Float projectPrice1, Float projectPrice2, Float projectPrice3) {
        this.projectName = projectName;
        this.projectPrice1 = projectPrice1;
        this.projectPrice2 = projectPrice2;
        this.projectPrice3 = projectPrice3;
    }

    // 没有结果时
    public ResultExcel(String projectName) {
        this.projectName = projectName;
        this.projectPrice1 = -1.0f;
        this.projectPrice2 = -1.0f;
        this.projectPrice3 = -1.0f;
        this.avgPrice=-1.0f;
        this.maxPrice=-1.0f;
        this.minPrice=-1.0f;
    }

    // 只有一个结果时
    public ResultExcel(String projectName, Float projectPrice1) {
        this.projectName = projectName;
        this.projectPrice1 = projectPrice1;
        this.projectPrice2 = -1.0f;
        this.projectPrice3 = -1.0f;
        this.avgPrice=projectPrice1;
        this.maxPrice=projectPrice1;
        this.minPrice=projectPrice1;
    }

    // 有两个结果时
    public ResultExcel(String projectName, Float projectPrice1, Float projectPrice2) {
        this.projectName = projectName;
        this.projectPrice1 = projectPrice1;
        this.projectPrice2 = projectPrice2;
        this.projectPrice3 = -1.0f;
    }

    public ResultExcel() {
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Float getProjectPrice1() {
        return projectPrice1;
    }

    public void setProjectPrice1(Float projectPrice1) {
        this.projectPrice1 = projectPrice1;
    }

    public Float getProjectPrice2() {
        return projectPrice2;
    }

    public void setProjectPrice2(Float projectPrice2) {
        this.projectPrice2 = projectPrice2;
    }

    public Float getProjectPrice3() {
        return projectPrice3;
    }

    public void setProjectPrice3(Float projectPrice3) {
        this.projectPrice3 = projectPrice3;
    }

    public Float getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Float maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Float getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Float minPrice) {
        this.minPrice = minPrice;
    }

    public Float getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(Float avgPrice) {
        this.avgPrice = avgPrice;
    }
}
