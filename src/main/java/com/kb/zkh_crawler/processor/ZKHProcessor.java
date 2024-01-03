package com.kb.zkh_crawler.processor;

import com.kb.zkh_crawler.pojo.Keyword;
import com.kb.zkh_crawler.pojo.Product;
import com.kb.zkh_crawler.utils.CrawlerUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class ZKHProcessor implements PageProcessor {
    // 随机间隔时间
//    int randomTime=(int)(Math.random()*8000);
    private Site site = Site.me()
            // 设置编码
            .setCharset("utf-8")
            // 添加用户代理
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36")
            // 重试的次数
            .setRetryTimes(3)
            // 间隔时间 稍微长一点可以减小被发现的几率
//            .setSleepTime(randomTime);
            .setSleepTime(500);
    @Override
    public void process(Page page) {
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
    }

    @Override
    public Site getSite() {
        return site;
    }
}
