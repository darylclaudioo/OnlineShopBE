package com.onlineshop.OnlineShop_BE.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import com.onlineshop.OnlineShop_BE.dto.response.MessageResponse;
import com.onlineshop.OnlineShop_BE.model.Order;
import com.onlineshop.OnlineShop_BE.repository.OrderRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private OrderRepository orderRepository;

    public MessageResponse generateReport(String format) {
        try {
            List<Order> orders = orderRepository.findAll();

            File file = ResourceUtils.getFile("classpath:Orders.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(orders);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "User");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            String date = java.time.LocalDate.now().toString();
            String path = "C:" + File.separator + "Downloads" + File.separator + date;
            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            if (format.equalsIgnoreCase("html")) {
                JasperExportManager.exportReportToHtmlFile(jasperPrint, path + ".html");
            } else if (format.equalsIgnoreCase("pdf")) {
                JasperExportManager.exportReportToPdfFile(jasperPrint, path + ".pdf");
            } else {
                return new MessageResponse("Unsupported format", HttpStatus.BAD_REQUEST.value(), "UNSUPPORTED FORMAT");
            }

            return new MessageResponse("Report has been generated", HttpStatus.OK.value(), "OK");
        } catch (FileNotFoundException e) {
            logger.error("File not found", e);
            return new MessageResponse("File not found", HttpStatus.NOT_FOUND.value(), "FILE NOT FOUND");
        } catch (JRException e) {
            logger.error("JasperReports error", e);
            return new MessageResponse("JasperReports error", HttpStatus.INTERNAL_SERVER_ERROR.value(), "JASPER REPORTS ERROR");
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            return new MessageResponse("Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL SERVER ERROR");
        }
    }
}
