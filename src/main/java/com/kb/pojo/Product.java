package com.kb.pojo;

/**
* @Description: 存放爬取的商品数据，商品的实体类
* @Date: 2023/10/24
*/
public class Product {
    private String pName;
    private Float pPrice;
    private String pImg;

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public Float getpPrice() {
        return pPrice;
    }

    public void setpPrice(Float pPrice) {
        this.pPrice = pPrice;
    }

    public String getpImg() {
        return pImg;
    }

    public void setpImg(String pImg) {
        this.pImg = pImg;
    }

    public Product() {

    }

    public Product(String pName, Float pPrice, String pImg) {
        this.pName = pName;
        this.pPrice = pPrice;
        this.pImg = pImg;
    }


    @Override
    public String toString() {
        if (pPrice<0){
            return "Product{" +
                    "pName='" + pName + '\'' +
                    ", pPrice=" + "暂时无法获取价格" +
                    ", pImg='" + pImg + '\'' +
                    '}';
        }else {
            return "Product{" +
                    "pName='" + pName + '\'' +
                    ", pPrice=" + pPrice +
                    ", pImg='" + pImg + '\'' +
                    '}';
        }
    }
}
