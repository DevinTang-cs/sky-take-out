package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;
    /**
     * 指定时间内的营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //用于存放从begin到end每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        //指定日期计算
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //查询该日期对应的营业额
            //select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //用于存放从begin到end每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        //指定日期计算
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //每天新增用户
        //select count(id) from user where create_time<? and create_time>?
        List<Integer> newUserList = new ArrayList<>();
        //总用户数量
        //select count(id) from user where create_time<?
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);

            Map map = new HashMap();
            map.put("end",endTime);

            //总用户数量
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin",beginTime);

            //新增用户数量
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //用于存放从begin到end每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        //指定日期计算
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //每日有效订单数和订单总数
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //查询每日有效订单数和订单总数
            //select count(id) from orders where order_time <? and order_time>?
            //select count(id) from orders where order_time <? and order_time>? and status=5
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //时间区间内的总数量和有效订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //完成率
        Double orderCompletionRate = 0.0;
        if(totalOrderCount!=0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .validOrderCount(validOrderCount)
                .totalOrderCount(totalOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        //select od.name,sum(od.number) number from order_detail od,orders o
        // where od.order_id=o.id and status=5 and o.order_time>? and o.order_time<?
        //group by od.name order by number desc limit 0,10
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //获取营业数据
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDate dateBegin = LocalDate.now().minusDays(30);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(
                LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX)
        );
        //写入excel文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/chartTemplate.xlsx");

        try {
            //基于模板文件创建一个新的文件
            XSSFWorkbook excel = new XSSFWorkbook(in);
            //表格文件的标签页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间："+dateBegin+"至"+dateEnd);
            //获得第四行
            XSSFRow row = sheet.getRow(3);
            //营业额
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            //订单完成率
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            //新增用户数
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            //获得第五行
            row = sheet.getRow(4);
            //有效订单数
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            //平均单价
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());
            //明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            out.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据条件统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status){
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);

        return orderMapper.countByMap(map);
    }
}
