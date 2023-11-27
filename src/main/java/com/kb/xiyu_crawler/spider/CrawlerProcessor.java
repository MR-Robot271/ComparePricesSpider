package com.kb.xiyu_crawler.spider;

import com.kb.xiyu_crawler.pojo.Keyword;
import com.kb.xiyu_crawler.pojo.Product;
import com.kb.xiyu_crawler.utils.CrawlerUtils;
import org.apache.log4j.Logger;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.List;

public class CrawlerProcessor implements PageProcessor {
    public void process(Page page) {
//        String products = page.getHtml().css("div.layout-product-list").toString();
//        System.out.println(products);

        // 存储结果
        List<Product> productList=new ArrayList<>();

        List<String> products = page.getHtml().css("div.layout-product-list>div.product-list >div.product").all();
        for (String product:products){
            Html html = new Html(product);
            // 获取图片地址
            String imgUrl = html.css("a>div.p-image>div.image-div").xpath("//img/@src").get();
            // 获取价格
            String price = html.css("div.price.ell.list-type-status>span.now_money>span.yen").get();
            // 用正则表达式拿到价格
            String regex="\\d+\\.\\d+";
            price = CrawlerUtils.splitWords(regex, price);
            // String空校验
            if (price==null||price.equals("")){
                price="-1";
            }
            float pPrice = Float.parseFloat(price);
            // 获取产品名称
            String name = html.css("div.p-name>a", "title").get();
            // 生成product对象
            Product productTemp = new Product();
            productTemp.setpImg(imgUrl);
            productTemp.setpName(name);
            productTemp.setpPrice(pPrice);
            productList.add(productTemp);
//            System.out.println(productTemp);
        }

        // 过滤数据
        // 从url中获取keyword
        String url = page.getUrl().toString();
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

    // 随机间隔时间
    int randomTime=(int)(Math.random()*8000);
    private Site site = Site.me()
            // 添加用户代理
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36")
            // 重试的次数
            .setRetryTimes(5)
            // 间隔时间 稍微长一点可以减小被发现的几率
            .setSleepTime(randomTime);

    @Override
    public Site getSite() {
        return site;
    }
}
