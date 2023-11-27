package com.kb.pojo;

import java.util.List;

// 存放搜索的关键字，一般以 品牌+产品类型+型号参数 搜索
// 过滤数据时，确保pName里有 品牌 和 型号（可能需要用正则表达式分词）
/**
* @Description: 存放搜索的关键字，一般以 品牌+产品类型+型号参数 搜索；
 *              过滤数据时，确保pName里有 品牌 和 型号（可能需要用正则表达式分词）
* @Date: 2023/10/24
*/
public class Keyword {
    // 品牌
    public String brand;
    // 产品类型
    public String type;
    // 型号参数
    public String modelParameters;
    // 附加参数
    public List<String> additionalParameters;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        // 防止数据为空出现错误
        if (type==null){
            this.type="";
        }else {
            this.type = type;
        }
    }

    public String getModelParameters() {
        return modelParameters;
    }

    public void setModelParameters(String modelParameters) {
        // 防止数据为空出现错误
        if (modelParameters==null){
            this.modelParameters="";
        }else {
            this.modelParameters = modelParameters;
        }
    }

    public List<String> getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(List<String> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    public Keyword(String brand, String type, String modelParameters, List<String> additionalParameters) {
        this.brand = brand;
        this.type = type;
        this.modelParameters = modelParameters;
        this.additionalParameters = additionalParameters;
    }

    public Keyword(String brand, String type, String modelParameters) {
        this.brand = brand;

        if (type==null){
            this.type="";
        }else{
            this.type = type;
        }

        if (modelParameters==null){
            this.modelParameters="";
        }else{
            this.modelParameters = modelParameters;
        }
    }

    // 用于部分参数缺少的情况
    public Keyword(String brand, String modelParameters) {
        this.brand = brand;
        this.modelParameters = modelParameters;
        this.type="";
    }

    public Keyword() {
    }

    @Override
    public String toString() {
        return brand+" "+type+" "+modelParameters;
    }
}
