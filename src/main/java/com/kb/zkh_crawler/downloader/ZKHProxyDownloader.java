package com.kb.zkh_crawler.downloader;

import com.kb.zkh_crawler.utils.CrawlerUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.selector.PlainText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * @Description: 使用IP代理的downloader
 * @Date: 2023/11/30
 */
public class ZKHProxyDownloader extends HttpClientDownloader implements Downloader {
    private RemoteWebDriver webDriver;
    private Task task;

    @Override
    public Page download(Request request, Task task) {
        this.task = task;

        // 如果出现找不到chromedriver的异常 可以使用以下代码
         System.setProperty("webdriver.chrome.driver", "c:\\driver\\chromedriver.exe");

        ChromeOptions option = new ChromeOptions();
        // 设置无头浏览器
        option.setHeadless(true);
//        option.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        option.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
        // 设置代理
        // 获取代理IP 判断IP能否使用
        Proxy proxy = getProxy();
        while (!isStillOK(proxy)) {
            proxy = getProxy();
        }
        option.addArguments("--proxy-server=http://" + proxy.getHost() + ":" + proxy.getPort());
        // 创建driver
        webDriver = new ChromeDriver(option);
        String url = request.getUrl();
        System.out.println("begin:" + url);
        // 如果url为空 则失败
        if (StringUtils.isBlank(url)) {
            return Page.fail();
        }

        // 打开网页
        try {
            webDriver.get(url);

            // 随机等待一段时间
            try {
                Thread.sleep((int) (Math.random() * 3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 隐式等待页面加载完成（直观的就是浏览器tab页上的小圈圈转完） 设置超时时间为10秒
            //webDriver.manage().timeouts().implicitlyWait(10, java.util.concurrent.TimeUnit.SECONDS);

            try {
                // 显示等待页面加载完成,如果页面加载超时,则重新加载,超时时间为10秒
                WebDriverWait webDriverWait = new WebDriverWait(webDriver, 10);
                // 设置判断条件为价格加载完成
                // 使用 and 方法结合多个条件
                webDriverWait.until(ExpectedConditions.or(
                        // 条件一：价格元素可见
                        ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a > div.goods-price > div.sku-price-wrap-new > div.wrap-flex")),

                        // 条件二：加载成功但是价格需要询价
                        ExpectedConditions.textToBe(By.cssSelector("a > div.goods-price > div.sku-price-wrap-new > span.goods-price-wait"), "企业客户可查看价格")
                ));
                // 判断是否被拦截，进入机器人验证页面
                String theUrl = webDriver.getCurrentUrl();
                if (theUrl.contains("robotVerify.html")) {
                    webDriver.quit();
                    return download(request, task);
                }

                // 随机向下滑动页面
                CrawlerUtils.slidePage(webDriver);


                String pageSource = webDriver.getPageSource();
                Page page = createPage(theUrl, pageSource);

                // 关闭浏览器
                webDriver.quit();
                return page;
            }catch (Exception e) {
                System.out.println("页面加载超时");
                webDriver.quit();
                return download(request, task);
            }
//            // 判断是否被拦截，进入机器人验证页面
//            String theUrl = webDriver.getCurrentUrl();
////            System.out.println("当前的url："+theUrl);
//            if (theUrl.contains("robotVerify.html")) {
//                webDriver.quit();
//                return download(request, task);
//            }
//
//            // 随机向下滑动页面
//            CrawlerUtils.slidePage(webDriver);
//
//
//            String pageSource = webDriver.getPageSource();
//            Page page = createPage(theUrl, pageSource);
//
//            // 关闭浏览器
//            webDriver.quit();
//            return page;
        } catch (WebDriverException e) {
            System.out.println("Timeout occurred, 正在重试...");
            webDriver.quit();
            //onError(request);
            return download(request, task);
        }
    }

    @Override
    protected void onError(Request request) {
        Page download = download(request, task);
        // 重试写入
        CrawlerUtils.retryCrawler(download);
    }

    /**
     * @Description: 根据url和页面源代码生成page
     * @Param: [url, content]
     * @return: us.codecraft.webmagic.Page
     * @Date: 2023/11/7
     */
    private Page createPage(String url, String content) {
        Page page = new Page();
        page.setRawText(content);
        page.setUrl(new PlainText(url));
        page.setRequest(new Request(url));
        page.setDownloadSuccess(true);
        return page;
    }

    /**
     * @Description: 用api获取数据无忧代理的代理IP
     * @Param: []
     * @return: us.codecraft.webmagic.proxy.Proxy
     * @Date: 2023/11/28
     */
    public static Proxy getProxy() {
        try {
            // 设置第三方接口的URL
            URL url = new URL("http://api.ip.data5u.com/dynamic/get.html?order=75a4be1b08855c0cf21dc796a4bf911f&random=1&sep=3");

            // 创建一个HttpURLConnection对象来发送HTTP请求
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为GET
            connection.setRequestMethod("GET");

            // 获取响应状态码
            int responseCode = connection.getResponseCode();

            // 如果响应成功（状态码为200），则读取响应数据
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String result = response.toString();
                // 处理响应数据
                System.out.println("获取的代理IP：" + result);
                String[] split = result.split(":");
                String ip = split[0];
                String port = split[1];
                return new Proxy(ip, Integer.parseInt(port));
            } else {
                System.out.println("请求失败，错误码：" + responseCode);
                return null;
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @Description: 判断代理IP能否使用
     * @Param: [proxy]
     * @return: boolean
     * @Date: 2023/11/28
     */
    public static boolean isStillOK(Proxy proxy) {
        if (proxy == null) {
            return false;
        }
        try {
            Jsoup.connect("http://www.baidu.com")
                    .timeout(1000)
                    .proxy(proxy.getHost(), proxy.getPort())
                    .get();
            return true;
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }
    }

    @Override
    public void setThread(int threadNum) {

    }

}
