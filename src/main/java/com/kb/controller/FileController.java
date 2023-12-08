package com.kb.controller;

import com.kb.websocket.MyHandler;
import com.kb.xiyu_crawler.downloader.ProxyDownloader;
import com.kb.zkh_crawler.downloader.ZKHDownloader;
import com.kb.zkh_crawler.downloader.ZKHProxyDownloader;
import com.kb.zkh_crawler.processor.ZKHProcessor;
import com.kb.xiyu_crawler.pipeline.ExcelPipeline;
import com.kb.xiyu_crawler.pojo.Keyword;
import com.kb.xiyu_crawler.spider.CrawlerProcessor;
import com.kb.xiyu_crawler.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import us.codecraft.webmagic.Spider;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("file")
public class FileController {
    @Value("D:/GitProjects/ComparePricesSpider/upload")
    private String uploadFilePath;
    @Value("D:/GitProjects/ComparePricesSpider/download")
    private String downloadFilePath;

    // 其他类可以访问，用于记录结果文件的路径
//    public static String path;
    // 其他类可以访问，用于记录震坤行结果文件的路径
    public static String zkhDownloadPath;

    // 其他类可以访问，用于记录西域结果文件的路径
    public static String xiyuDownloadPath;

    // 其他类可以访问，用于传递上传文件在服务器的路径
    public static String fileName;

    // 总数
    public static int total = 0;

    // 西域处理进度
    public static int xiyuProgress = 0;

    // 震坤行处理进度
    public static int zkhProgress = 0;

    // 进度条websocket
    public static MyHandler myHandler=new MyHandler();



    /**
     * @Description: 上传文件的接口
     * @Param: [MultipartFile：files]
     * @return: java.lang.String
     * @Date: 2023/11/9
     */
    @PostMapping("/upload")
    public boolean fileUpload(@RequestParam(value = "files",required = true) MultipartFile files[]) {
        // 遍历接收的文件
        for (int i = 0; i < files.length; i++) {
            // 获取文件名 比较价格
            String originalFilename = files[i].getOriginalFilename();
            // 创建文件对象
            fileName=uploadFilePath + "/" + originalFilename;
            File uploadFile = new File(fileName);
            // 判断文件夹是否存在
            if (!uploadFile.getParentFile().exists()){
                // 不存在则创建文件夹
                uploadFile.getParentFile().mkdirs();
            }
            // 将上传的文件导入本地 比价
            try {
                // 上传重复的文件不会报错，后上传的文件会直接覆盖已经上传的文件
                files[i].transferTo(uploadFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * @Description: 下载震坤行结果文件的接口
     * @Param: [response]
     * @return: java.lang.String
     * @Date: 2023/11/9
     */
    @RequestMapping("/zkhDownload")
    public String zkhDownload(HttpServletResponse response){
        // 判断文件是否存在
        //String path=uploadFilePath+"/"+fileName;
        File file = new File(zkhDownloadPath);
        if (!file.exists()){
            return "文件不存在";
        }
        // 用response设置返回文件的格式 以文件流的方式返回
        response.setContentType("application/octet-stream");
        // 设置编码方式为utf-8
        response.setCharacterEncoding("utf-8");
        // 设置文件流长度
        response.setContentLength((int) file.length());
        // 设置返回头 设置下载后的文件名
        response.setHeader("Content-Disposition","attachment;filename="+file.getName());


        // 将文件转化为文件输出流
        try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));) {
            byte[] bytes = new byte[1024];
            OutputStream os=response.getOutputStream();
            int i=0;
            while ((i=bufferedInputStream.read(bytes))!=-1){
                os.write(bytes,0,i);
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "下载失败";
        }
        return "下载成功";
    }

    /**
     * @Description: 下载西域结果文件的接口
     * @Param: [response]
     * @return: java.lang.String
     * @Date: 2023/11/9
     */
    @RequestMapping("/xiyuDownload")
    public String xiyuDownload(HttpServletResponse response){
        // 判断文件是否存在
        //String path=uploadFilePath+"/"+fileName;
        File file = new File(xiyuDownloadPath);
        if (!file.exists()){
            return "文件不存在";
        }
        // 用response设置返回文件的格式 以文件流的方式返回
        response.setContentType("application/octet-stream");
        // 设置编码方式为utf-8
        response.setCharacterEncoding("utf-8");
        // 设置文件流长度
        response.setContentLength((int) file.length());
        // 设置返回头 设置下载后的文件名
        response.setHeader("Content-Disposition","attachment;filename="+file.getName());


        // 将文件转化为文件输出流
        try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));) {
            byte[] bytes = new byte[1024];
            OutputStream os=response.getOutputStream();
            int i=0;
            while ((i=bufferedInputStream.read(bytes))!=-1){
                os.write(bytes,0,i);
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "下载失败";
        }
        return "下载成功";
    }

    /**
     * @Description: 西域爬虫
     * @Param: []
     * @return: void
     * @Date: 2023/11/27
     */
    @GetMapping("/xiyuSpider")
    public void xiyuSpider(){
        String baseUrl="https://www.ehsy.com/search?k=";

        // 添加时间戳，用于区别不同文件 一次爬虫的结果放在一个文件中
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter fileTime = DateTimeFormatter.ofPattern("yyMMddHHmm-ss");
        String time = localDateTime.format(fileTime);
        xiyuDownloadPath="D:\\GitProjects\\ComparePricesSpider\\download\\xiyuCrawlerResult"+time+".xlsx";

        // 获取网页地址
        String keywordPath=fileName;
        List<Keyword> keywords = FileUtils.getKeywords(keywordPath);
        total=keywords.size();
        List<String> urls = new ArrayList<>();
        for (Keyword keyword:keywords){
            String noBlankModelParameters=keyword.getModelParameters().replaceAll(" ", "");
            keyword.setModelParameters(noBlankModelParameters);
            String noBlankType=keyword.getType().replaceAll(" ", "");
            keyword.setType(noBlankType);
            String url=baseUrl+keyword;
            urls.add(url);
        }
        // 需要把List转为String数组 addUrl只能用String数组才能添加多个
        String[] strings = urls.toArray(new String[0]);
        // 去除strings中的换行符 如果有换行符 放入url会出现错误
        for (int i = 0; i < strings.length; i++) {
            strings[i] = strings[i].replaceAll("\n", "");
        }


        Spider.create(new CrawlerProcessor())
                .addUrl(strings)
                .setDownloader(new ProxyDownloader())
                .addPipeline(new ExcelPipeline())
                .thread(1) // 多线程可能会触发反爬虫
                .run();
    }

    /**
     * @Description: 震坤行爬虫
     * @Param: []
     * @return: void
     * @Date: 2023/11/9
     */
    @GetMapping("/zkhSpider")
    public void zkhSpider(){
        String baseUrl="https://www.zkh.com/search.html?keywords=";

        // 添加时间戳，用于区别不同文件 一次爬虫的结果放在一个文件中
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter fileTime = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        String time = localDateTime.format(fileTime);
        zkhDownloadPath="D:\\GitProjects\\ComparePricesSpider\\download\\ZKHCrawlerResult"+time+".xlsx";

        // 获取网页地址
        String keywordPath=fileName;
        List<com.kb.zkh_crawler.pojo.Keyword> keywords = com.kb.zkh_crawler.utils.FileUtils.getKeywords(keywordPath);
        List<String> urls = new ArrayList<>();
        for (com.kb.zkh_crawler.pojo.Keyword keyword:keywords){
            String noBlankModelParameters=keyword.getModelParameters().replaceAll(" ", "");
            keyword.setModelParameters(noBlankModelParameters);
            String noBlankType=keyword.getType().replaceAll(" ", "");
            keyword.setType(noBlankType);
            String url=baseUrl+keyword;
            urls.add(url);
        }
        // 需要把List转为String数组 addUrl只能用String数组才能添加多个
        String[] strings = urls.toArray(new String[0]);


//        ZKHDownloader zkhDownloader = new ZKHDownloader();
        ZKHProxyDownloader zkhDownloader = new ZKHProxyDownloader();
        Spider spider = Spider.create(new ZKHProcessor())
                .addUrl(strings)
                .setDownloader(zkhDownloader)
                .addPipeline(new com.kb.zkh_crawler.pipeline.ExcelPipeline())
                // 多线程可能会触发反爬虫
                .thread(1);
        spider.run();
        // 关闭ChromeDriver的浏览器
        //zkhDownloader.closeWebDriver();
    }

}
