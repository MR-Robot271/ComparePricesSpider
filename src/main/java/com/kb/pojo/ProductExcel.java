package com.kb.pojo;

import com.alibaba.excel.annotation.ExcelProperty;

import java.util.Date;

/**
* @Description: Excel文件内容的实体类，主要被EasyExcel使用
* @Date: 2023/10/27
*/
public class ProductExcel {
    // 创建时间
    @ExcelProperty("创建时间")
    private Date createTime;

    // 更改时间
    @ExcelProperty("更改时间")
    private Date updateTime;

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

    // 产品价格
    @ExcelProperty("产品价格")
    //private Float pPrice;
    private Float projectPrice;

    // 产品图片
    @ExcelProperty("产品图片")
    //private String pImg;
    private String projectImg;

    public ProductExcel() {

    }

    public ProductExcel(Date createTime, Date updateTime, String pName, Float pPrice, String pImg) {
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.projectName = pName;
        this.projectPrice = pPrice;
        this.projectImg = pImg;
    }

    // 直接传入Product
    public ProductExcel(Date createTime, Date updateTime, Product product) {
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.projectName = product.getpName();
        this.projectPrice = product.getpPrice();
        this.projectImg = product.getpImg();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Float getProjectPrice() {
        return projectPrice;
    }

    public void setProjectPrice(Float projectPrice) {
        this.projectPrice = projectPrice;
    }

    public String getProjectImg() {
        return projectImg;
    }

    public void setProjectImg(String projectImg) {
        this.projectImg = projectImg;
    }
}
