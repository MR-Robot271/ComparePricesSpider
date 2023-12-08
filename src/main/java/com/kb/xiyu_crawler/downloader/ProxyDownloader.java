package com.kb.xiyu_crawler.downloader;

import org.jsoup.Jsoup;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import javax.annotation.concurrent.ThreadSafe;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@ThreadSafe
public class ProxyDownloader extends HttpClientDownloader {
    private Task task;
    @Override
    public Page download(Request request, Task task) {
        this.task = task;

        try {
            // 等待一段时间，为文件写入留一定的时间
            Thread.sleep(2000);

            // 获取代理IP 判断IP能否使用
            Proxy proxy = getProxy();
            while (!isStillOK(proxy)) {
                proxy = getProxy();
            }
            this.setProxyProvider(SimpleProxyProvider.from(proxy));

            Page downloadPage = super.download(request, task);
            String currentUrl = downloadPage.getUrl().toString();
            System.out.println("当前的URL：" + currentUrl);
            // 判断是否被拦截，进入机器人验证页面
            if (currentUrl.contains("robotVerify")) {
                return download(request, task);
            }

            return downloadPage;
        }
        // 中途出错，自动重试
        catch (Exception e){
            e.printStackTrace();
            System.out.println("-------------------------------------------正在重试中------------------------------------------------");
            return download(request, task);
        }

    }

//    @Override
//    protected void onError(Request request) {
//        System.out.println("----------------------------------------------重试中------------------------------------------------");
//        Page download = download(request, task);
//        // 重试写入
//        CrawlerUtils.retryCrawler(download);
//    }

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
                return getProxy();
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
            return getProxy();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return getProxy();
        } catch(IOException e){
            System.out.println("可能是获取代理超时");
            e.printStackTrace();
            return getProxy();
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
}
