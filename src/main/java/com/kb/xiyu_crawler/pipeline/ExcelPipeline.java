package com.kb.xiyu_crawler.pipeline;

import com.kb.controller.FileController;
import com.kb.websocket.MyHandler;
import com.kb.xiyu_crawler.pojo.Product;
import com.kb.xiyu_crawler.pojo.ProductExcel;
import com.kb.xiyu_crawler.pojo.ResultExcel;
import com.kb.xiyu_crawler.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @Description: 实现写入Excel文件的Pipeline
* @Date: 2023/10/31
*/

public class ExcelPipeline implements Pipeline {
//    @Autowired
//    private MyHandler xiyuHandler;
    @Override
    public void process(ResultItems resultItems, Task task) {
        // 存储爬取结果的list
        List<Product> productList=new ArrayList<>();

        // 存储excel文件的实体类list
        List<ProductExcel> productExcelList = new ArrayList<>();

        // 存放结果的excel文件地址
        // 添加时间戳，用于区别不同文件 一次爬虫的结果放在一个文件中
//        LocalDateTime localDateTime = LocalDateTime.now();

        // 测试用
        //DateTimeFormatter fileTime = DateTimeFormatter.ofPattern("yyMMddHHmm");
        // 正式用
//        DateTimeFormatter fileTime = DateTimeFormatter.ofPattern("yyMMddHH");
//        String time = localDateTime.format(fileTime);
//        String path = "D:\\CrawlerResult\\XiYuCrawlerResult"+time+".xlsx";
        String path = FileController.xiyuDownloadPath;

        // 暂时存放结果的excel文件地址
        String pathOfTemp = "D:\\CrawlerResult\\XiYuCrawlerTemp.xlsx";

        writeToExcel(resultItems, productExcelList, path, pathOfTemp);

        // 更新西域进度
        FileController.xiyuProgress++;
        System.out.println("总数："+FileController.total+"\n"+"已爬取："+FileController.xiyuProgress);
        //messagingTemplate.convertAndSend("/topic/process", FileController.xiyuProgress);
        try {
            FileController.myHandler.sendDataToClient(FileController.xiyuProgress, FileController.total);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
    * @Description: 将爬虫结果写入Excel文件
    * @Param: [resultItems, productExcelList, path, pathOfTemp]
    * @return: void
    * @Date: 2023/11/14
    */
    private static void writeToExcel(ResultItems resultItems, List<ProductExcel> productExcelList, String path, String pathOfTemp) {
        List<Product> productList;

        // 用于判断文件夹是否存在的临时文件
        File tmpFile = new File(path);
        // 判断文件夹是否存在
        if (!tmpFile.getParentFile().exists()){
            // 不存在则创建文件夹
            tmpFile.getParentFile().mkdirs();
        }

        // 爬虫有结果时 结果为List
        if(resultItems.get("productList") instanceof List){
            productList = resultItems.get("productList");

            //  只取结果的前三个
            if (productList.size() <= 3) {
                for (Product product : productList) {
                    Date date = new Date();
                    // 创建excel内容的实体类
                    ProductExcel productExcel = new ProductExcel(date, date, product);
                    productExcelList.add(productExcel);
                }
            }else{
                for (Product product : productList.subList(0, 3)) {
                    Date date = new Date();
                    // 创建excel内容的实体类
                    ProductExcel productExcel = new ProductExcel(date, date, product);
                    productExcelList.add(productExcel);
                }
            }

            // 更新excel表格结构，以追加写入的方式，写入excel文件
            String projectName = productExcelList.get(0).getProjectName();
            ResultExcel resultExcel;
            List<Float> prices = new ArrayList<Float>();
            for (int i = 0; i < productExcelList.size(); i++) {
                prices.add(productExcelList.get(i).getProjectPrice());
            }
            // 根据得到的数据量动态生成ResultExcel
            if (prices.size() == 1) {
                resultExcel=new ResultExcel(projectName, prices.get(0));
            }else if (prices.size() == 2){
                float max = Math.max(prices.get(0), prices.get(1));
                float min = Math.min(prices.get(0), prices.get(1));
                float avg=(prices.get(0)+prices.get(1))/2.0f;
                resultExcel=new ResultExcel(projectName, prices.get(0), prices.get(1));
                resultExcel.setMaxPrice(max);
                resultExcel.setMinPrice(min);
                resultExcel.setAvgPrice(avg);
            }else if(prices.size() == 3){
                float max = Math.max(prices.get(2), Math.max(prices.get(0), prices.get(1)));
                float min = Math.min(prices.get(2), Math.min(prices.get(0), prices.get(1)));
                float avg=(prices.get(0)+prices.get(1)+prices.get(2))/3.0f;
                resultExcel=new ResultExcel(projectName, prices.get(0), prices.get(1), prices.get(2), max, min, avg);
            }else{
                resultExcel=new ResultExcel(projectName);
            }
            ArrayList<ResultExcel> resultExcels = new ArrayList<>();
            resultExcels.add(resultExcel);
            FileUtils.resultExcelAppend(resultExcels, path, pathOfTemp);
        }
        // 无结果时 结果为String 此时实例化一个新的ProductExcel
        else if (resultItems.get("productList") instanceof String){
            // 更新excel表格结构，以追加写入的方式，写入excel文件
            ResultExcel resultExcel = new ResultExcel(resultItems.get("productList"));
            ArrayList<ResultExcel> resultExcels = new ArrayList<>();
            resultExcels.add(resultExcel);
            FileUtils.resultExcelAppend(resultExcels, path, pathOfTemp);
        }
        // 以上情况都不是
        else{
            throw new RuntimeException("爬取的结果为空或者异常");
        }
    }


}
