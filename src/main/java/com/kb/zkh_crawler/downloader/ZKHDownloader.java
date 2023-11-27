package com.kb.zkh_crawler.downloader;

import com.kb.zkh_crawler.utils.CrawlerUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.PlainText;

public class ZKHDownloader implements Downloader {
    private RemoteWebDriver webDriver;

    public ZKHDownloader(){
        // 如果出现找不到chromedriver的异常 可以使用以下代码
        // System.setProperty("webdriver.chrome.driver", "c:\\driver\\chromedriver.exe");

        ChromeOptions option = new ChromeOptions();
        // 设置无头浏览器
        //option.setHeadless(true);
//        option.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        option.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");

        // 创建driver
        webDriver = new ChromeDriver(option);
    }

    @Override
    public Page download(Request request, Task task) {
        String url = request.getUrl();
        System.out.println("begin:" + url);
        // 如果url为空 则失败
        if (StringUtils.isBlank(url)) {
            return Page.fail();
        }

        // 打开网页
        webDriver.get(url);

        // 随机等待一段时间
        try {
            Thread.sleep((int) (Math.random() * 3000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 随机向下滑动页面
        CrawlerUtils.slidePage(webDriver);

        String theUrl = webDriver.getCurrentUrl();
        String pageSource = webDriver.getPageSource();
        Page page = createPage(theUrl, pageSource);

        // 关闭浏览器
//        webDriver.quit();

        return page;
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

    @Override
    public void setThread(int threadNum) {

    }

    /**
     * @Description: 因为downloader会由spider代管，所以一个spider一般只会管理一个downloader，
     *              如果直接在download方法里调用webDriver.quit()，会导致只有spider的第一个url爬取成功，
     *              后面的都不会成功，因为webDriver已经被quit销毁了
     * @Param: []
     * @return: void
     * @Date: 2023/11/7
     */
    public void closeWebDriver() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

}
