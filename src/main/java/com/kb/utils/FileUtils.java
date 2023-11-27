package com.kb.utils;

import com.alibaba.excel.EasyExcel;
import com.kb.pojo.Keyword;
import com.kb.pojo.ProductExcel;
import com.kb.pojo.ResultExcel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* @Description: 文件工具类，用EasyExcel从文件中提取关键字、文件读写等
* @Date: 2023/10/25
*/
public class FileUtils {
    /**
    * @Description: 根据文件类型 获取表格中的关键字
    * @Param: [filePath]
    * @return: java.util.List<com.kb.pojo.Keyword>
    * @Date: 2023/10/25
    */
    public static List<Keyword> getKeywords(String filePath) {
        List<Keyword> keywords = new ArrayList<>();
        // 获取文件流
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);

            // 创建工作簿
            Workbook workbook = null;
            // 根据文件后缀判断文件类型
            if (filePath.endsWith("xlsx")) {
                workbook = new XSSFWorkbook(fileInputStream);
            } else if (filePath.endsWith("xls")) {
                workbook = new HSSFWorkbook(fileInputStream);
            } else {
                throw new RuntimeException("文件类型有误，请确认文件以.xls或者.xlsx结尾");
            }

            // 得到表sheet
            Sheet sheet = workbook.getSheetAt(0);

            // 列名
            String brand = "品牌（中文）";
            String type = "名称";
            String parameters = "型号";

            // 列名的索引
            int brandIndex = -1;
            int typeIndex = -1;
            int parametersIndex = -1;

            // 拿到表格第一行 第一行为列名
            Row rowFirst = sheet.getRow(0);
            short lastCellNum = rowFirst.getLastCellNum();
            for (int i = 0; i < lastCellNum; i++) {
                // 如果单元格为空 返回null
                Cell firstCell = rowFirst.getCell(i, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
                String cellValue = "";

                // 如果单元格为空 就跳过本次循环
                if (firstCell != null) {
                    cellValue = firstCell.getStringCellValue();
                    // 判断单元格是否符合要求
                    if (cellValue != null) {
                        if (brand.equals(cellValue)) {
                            brandIndex = i;
                        } else if (type.equals(cellValue)) {
                            typeIndex = i;
                        } else if (parameters.equals(cellValue)) {
                            parametersIndex = i;
                        }
                    }
                }
            }

            // 从每一行中获取内容 品牌 类型 型号
            getRow(keywords, sheet, brandIndex, typeIndex, parametersIndex);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("文件丢失或不存在");
        } catch (IOException e) {
            throw new RuntimeException("工作簿创建失败");
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                new RuntimeException("文件流关闭失败");
                e.printStackTrace();
            }
        }
        return keywords;
    }

    /**
     * @Description: 从每一行中获取相应的内容，放入list中
     * @Param: [keywords, sheet, brandIndex, typeIndex, parametersIndex]
     * @return: void
     * @Date: 2023/10/25
     */
    private static void getRow(List<Keyword> keywords, Sheet sheet, int brandIndex, int typeIndex, int parametersIndex) {
        // 得到行
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i <= lastRowNum; i++) {
            // 得到列
            // 得到品牌
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            Cell brandCell = row.getCell(brandIndex, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String brand1 = null;
            if (brandCell != null) {
                brand1 = brandCell.getStringCellValue();
            }
            // 得到名称
            Cell typeCell = sheet.getRow(i).getCell(typeIndex, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String type1 = null;
            if (typeCell != null) {
                type1 = typeCell.getStringCellValue();
            }
            // 得到型号
            Cell parametersCell = sheet.getRow(i).getCell(parametersIndex, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String parameters1 = null;
            if (parametersCell != null) {
                // 再判断具体的数据类型 String Numeric Blank等
                CellType cellType = parametersCell.getCellType();
                if (cellType == CellType.STRING) {
                    parameters1 = parametersCell.getStringCellValue();
                }else if (cellType == CellType.NUMERIC) {
                    parameters1 = String.valueOf(parametersCell.getNumericCellValue());
                }else if (cellType == CellType.BLANK) {
                    parameters1 = "";
                }else {
                    parameters1 = "";
                }
            }

            // 如果所需数据都没有 就直接进入下一轮循环 防止出现数据全为null的情况
            if (brand1 == null && type1 == null && parameters1 == null) {
                continue;
            }
            Keyword keyword = new Keyword();
            keyword.setBrand(brand1);
            keyword.setType(type1);
            keyword.setModelParameters(parameters1);

            keywords.add(keyword);
        }
    }

    /**
    * @Description: 使用EasyExcel以追加的方式写入excel文件
    * @Param: [productExcelList, path, pathOfTemp]
    * @return: void
    * @Date: 2023/10/31
    */
    public static void excelAppend(List<ProductExcel> productExcelList, String path, String pathOfTemp) {
        // 以追加的形式写入excel
        File file = new File(path);
        File tempFile = new File(pathOfTemp);
        // 判断文件是否已存在
        if (file.exists()){
            // 如果已存在，按照原有格式，不需要表头，追加写入
            EasyExcel.write(file, ProductExcel.class)
                    .needHead(false)
                    .withTemplate(file)
                    .file(tempFile)
                    .sheet()
                    .doWrite(productExcelList);
        }
        // 如果不存在，会自动创建文件，第一次写入需要表头
        else{
            EasyExcel.write(path, ProductExcel.class)
                    .sheet("爬虫结果")
                    .doWrite(productExcelList);
        }

        if (tempFile.exists()){
            file.delete();
            tempFile.renameTo(file);
        }
    }

    /**
     * @Description: 使用EasyExcel以追加的方式写入excel文件,此方法更新了excel表结构
     * @Param: [productExcelList, path, pathOfTemp]
     * @return: void
     * @Date: 2023/11/14
     */
    public static void resultExcelAppend(List<ResultExcel> productExcelList, String path, String pathOfTemp) {
        // 以追加的形式写入excel
        File file = new File(path);
        File tempFile = new File(pathOfTemp);
        // 判断文件是否已存在
        if (file.exists()){
            // 如果已存在，按照原有格式，不需要表头，追加写入
            EasyExcel.write(file, ResultExcel.class)
                    .needHead(false)
                    .withTemplate(file)
                    .file(tempFile)
                    .sheet()
                    .doWrite(productExcelList);
        }
        // 如果不存在，会自动创建文件，第一次写入需要表头
        else{
            EasyExcel.write(path, ResultExcel.class)
                    .sheet("爬虫结果")
                    .doWrite(productExcelList);
        }

        if (tempFile.exists()){
            file.delete();
            tempFile.renameTo(file);
        }
    }

}
