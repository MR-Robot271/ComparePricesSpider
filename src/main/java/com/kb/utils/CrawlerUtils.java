package com.kb.utils;

import com.kb.pojo.Keyword;
import com.kb.pojo.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @Description: 爬虫工具，分词、过滤等
* @Date: 2023/10/24
*/
public class CrawlerUtils {
    /**
    * @Description: 用正则表达式进行分词
    * @Param: [regex, words]
    * @return: java.lang.String
    * @Date: 2023/10/26
    */
    public static String splitWords(String regex, String words){
        String result="";
        // 用正则表达式进行分词
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(words);
        if (matcher.find()){
            result= matcher.group();
        }
        return result;
    }

    /**
     * @Description: 用正则表达式对关键词进行分词
     * @Param: String
     * @return: java.util.List<java.lang.String>
     * @Date: 2023/10/24
     */
    public static List<String> splitKeyWords(String regex,String words){
        List<String> results=new ArrayList<>();

        // 用正则表达式进行分词
        // String regex="[a-zA-Z0-9+]+";
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(words);
        while (matcher.find()){
            String word=matcher.group();
            results.add(word);
        }

        return results;
    }

    /**
     * @Description: 对抓取的数据进行一些过滤，主要通过 品牌 型号
     * @Param: [List<Product> products, Keyword keyword]
     * @return: java.util.List<com.kb.pojo.Product>
     * @Date: 2023/10/24
     */
    public static List<Product> filter(List<Product> products, Keyword keyword){
        List<Product> newProducts = new ArrayList<>();
        // 确保pName里有 品牌 和 型号（可能需要用正则表达式分词）
        for (Product product : products) {
            // 确保pName里有 品牌
            String brand=keyword.getBrand();
            if (product.getpName().contains(brand)) {
                String regex="[a-zA-Z0-9+]+";
                List<String> words = splitKeyWords(regex,keyword.getModelParameters());

                // 如果没有 型号，就直接返回数据
                if (words.size()>0) {
                    // 记录型号分词击中的次数
                    int count = 0;
                    for (String word : words) {
                        if (product.getpName().contains(word)) {
                            count++;
                        }
                    }
                    // 比较 分词数量 与 击中次数 的差距
                    // 以5为界分为两段
                    if (words.size() <= 5) {
                        // 再分为2 3 5 三段
                        if (words.size() <= 2) {
                            if (count>=1) {
                                newProducts.add(product);
                            }
                        } else if (words.size() <= 3) {
                            if (count >= 2) {
                                newProducts.add(product);
                            }
                        } else {
                            if (count >= 3) {
                                newProducts.add(product);
                            }
                        }
                    } else {
                        // 以80%为界
                        if (((float) count / (float) words.size()) >= 0.8) {
                            newProducts.add(product);
                        }
                    }
                }
                // 没有 型号，直接添加数据
                else {
                    newProducts.add(product);
                }
            }
        }
        return newProducts;
    }
}
