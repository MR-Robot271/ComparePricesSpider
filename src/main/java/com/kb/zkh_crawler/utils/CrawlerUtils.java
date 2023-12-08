package com.kb.zkh_crawler.utils;

import com.kb.zkh_crawler.pipeline.ExcelPipeline;
import com.kb.zkh_crawler.pojo.Keyword;
import com.kb.zkh_crawler.pojo.Product;
import com.kb.zkh_crawler.pojo.ProductExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
     * @Description: 向下随机滑动页面直到页面底部
     * @Param: [WebDriver]
     * @return: void
     * @Date: 2023/10/24
     */
    public static void slidePage(WebDriver webDriver){
        // 创建js执行器
        JavascriptExecutor js = (JavascriptExecutor) webDriver;

        // 随机向下滑动页面
        // 定义滚动目标的高度
        long scrollHeight = (long) js.executeScript("return Math.max(document.body.scrollHeight,document.body.offsetHeight,document.documentElement.clientHeight,document.documentElement.scrollHeight,document.documentElement.offsetHeight)");

        // 缓慢滚动页面
        for (long i = 0; i < scrollHeight; i += (int) (Math.random() * 100)) {
            js.executeScript("window.scrollTo(0," + i + ")");
            try {
                // 随机等待一段时间
                Thread.sleep((int) (Math.random() * 600));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long scrollHeightTmp = scrollHeight;
        scrollHeight = (long) js.executeScript("return Math.max(document.body.scrollHeight,document.body.offsetHeight,document.documentElement.clientHeight,document.documentElement.scrollHeight,document.documentElement.offsetHeight)");
        for (long i = scrollHeightTmp; i < scrollHeight; ) {
//            int a=(int)(Math.random()*100);
            js.executeScript("window.scrollTo(" + i + "," + (i += (int) (Math.random() * 100)) + ")");
            try {
                // 随机等待一段时间
                Thread.sleep((int) (Math.random() * 500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

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
            // group(1) 会返回第一个捕获组的内容 即第一个括号所匹配的内容
            result= matcher.group(1);
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
    * @Description: 用正则表达式获取震坤行真实产品号，拼接得到图片地址
    * @Param: [number]
    * @return: java.lang.String
    * @Date: 2023/11/7
    */
    public static String getImgUrl (String number) {
        // 定义正则表达式
        String regex = "：(\\w+)";

        // 创建pattern
        Pattern pattern = Pattern.compile(regex);

        // 创建Matcher
        Matcher matcher = pattern.matcher(number);

        // 图片的基本地址
        String baseImgUrl = "https://private.zkh.com/PRODUCT/BIG/BIG_";
        String endImgUrl = "_01.jpg?x-oss-process=style/WEBPCOM_style_350";
        String imgUrl;
        // 获取订货编码
        if (matcher.find()) {
            // 订货编码 唯一
            String realNumber = matcher.group(1);
            // 图片地址
            imgUrl = baseImgUrl + realNumber + endImgUrl;
            // System.out.println(imgUrl);
        } else {
            imgUrl = "暂时没有照片";
        }
        return imgUrl;
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

    /**
     * @Description: 爬取失败后重试，过滤数据并写入文件
     * @Param: [page]
     * @return: void
     * @Date: 2023/11/29
     */
    public static void retryCrawler(Page page){
        // 存储结果
        List<Product> productList=new ArrayList<>();
        // 获取相应的内容 产品名 价格 图片
        List<String> products = page.getHtml().css("#app>div.content>div.catalog-wrap>div.list-inner>div>div.goods-item-box-new>div.goods-item-wrap-new").all();
        // 滑动后刷新的数据
        List<String> productsInfinite = page.getHtml().css("#app>div.content>div.catalog-wrap>div.list-inner>div.infiniteScroll>div.goods-wrap>div>div.goods-item-box-new>div.goods-item-wrap-new").all();
        if (productsInfinite.size()>0) {
            products.addAll(productsInfinite);
        }
        for (String product:products){
            Html html = new Html(product);
            // 获取图片地址
            String number = html.css("a>div.clearfix.metertitle>span").get();
            // 正则表达式
            String regex = ">(\\w+)";
            number=CrawlerUtils.splitWords(regex,number);
            // 图片的基本地址
            String baseImgUrl = "https://private.zkh.com/PRODUCT/BIG/BIG_";
            String endImgUrl = "_01.jpg?x-oss-process=style/WEBPCOM_style_350";
            String imgUrl =baseImgUrl+number+endImgUrl;

            // 获取价格
            String price = "";
            String regexPrice = ">(\\.?\\d+)";
            String priceInteger = html.css("a > div.goods-price > div.sku-price-wrap-new>div.wrap-flex>div>span.integer").get();
//            priceInteger=CrawlerUtils.splitWords(regexPrice,priceInteger);
            String priceDecimal = html.css("a > div.goods-price > div.sku-price-wrap-new>div.wrap-flex>div>span.decimal").get();
            // 判断价格是否存在
            if (StringUtils.isNotBlank(priceDecimal)&&StringUtils.isNotBlank(priceInteger)){
                priceInteger=CrawlerUtils.splitWords(regexPrice,priceInteger);
                priceDecimal=CrawlerUtils.splitWords(regexPrice,priceDecimal);
                price=priceInteger+priceDecimal;
            }
//            priceDecimal=CrawlerUtils.splitWords(regexPrice,priceDecimal);


            String pPrice="";
            if (StringUtils.isBlank(price)){
                pPrice="-1";
            }else{
                pPrice=price;
            }
            Float pPriceFloat = Float.parseFloat(pPrice);

            // 获取产品名称
            String name = html.css("a > div.goods-name.clamp2", "title").get();

            // 生成product对象
            Product productTemp = new Product();
            productTemp.setpImg(imgUrl);
            productTemp.setpName(name);
            productTemp.setpPrice(pPriceFloat);
            productList.add(productTemp);
//            System.out.println(productTemp);
        }

        // 过滤数据
        // 从url中获取keyword
        String url = page.getUrl().toString();
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String regex="(?<==)(.*)";
        String key = CrawlerUtils.splitWords(regex, url);
        String[] keyw = key.split(" ");
        int length = keyw.length;
        Keyword keyword = new Keyword();
        if (length>=3){
            keyword = new Keyword(keyw[0],keyw[1],keyw[2]);
        }else if (length==2){
            keyword = new Keyword(keyw[0],keyw[1]);
        }

        // 过滤
        productList = CrawlerUtils.filter(productList,keyword);

        // 将结果导入productList中
        // log4j的实例
        Logger log=Logger.getLogger(Logger.class);
        if (productList.size() == 0) {
            String result=keyword.toString()+" ：没有匹配的信息";
            page.putField("productList",result);
            log.info(result);
        }else {
            page.putField("productList", productList);
            for (Product product : productList) {
//                System.out.println(product);
                log.info(product.toString());
            }
        }

        // 写入文件
        // 存储excel文件的实体类list
        List<ProductExcel> productExcelList = new ArrayList<>();

        // 存放结果的excel文件地址
        // 添加时间戳，用于区别不同文件 一次爬虫的结果放在一个文件中
        LocalDateTime localDateTime = LocalDateTime.now();

        // 测试用
//        DateTimeFormatter fileTime = DateTimeFormatter.ofPattern("yyMMddHHmm");
        // 正式用
        DateTimeFormatter fileTime = DateTimeFormatter.ofPattern("yyMMddHH");

        String time = localDateTime.format(fileTime);
        String path = "D:\\CrawlerResult\\ZKHwmCrawlerResult"+time+".xlsx";

        // 暂时存放结果的excel文件地址
        String pathOfTemp = "D:\\CrawlerResult\\ZKHCrawlerTemp.xlsx";

        ExcelPipeline.writeToExcel(page.getResultItems(), productExcelList, path, pathOfTemp);
    }
}
