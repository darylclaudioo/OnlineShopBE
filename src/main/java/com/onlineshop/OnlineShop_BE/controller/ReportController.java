package com.onlineshop.OnlineShop_BE.controller;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onlineshop.OnlineShop_BE.dto.response.MessageResponse;
import com.onlineshop.OnlineShop_BE.service.ReportService;

import net.sf.jasperreports.engine.JRException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/ordersReport")
public class ReportController {

   @Autowired
   private ReportService reportService;

   @GetMapping("{format}")
   public MessageResponse exportReport(@PathVariable String format) throws FileNotFoundException, JRException {
      return reportService.generateReport(format);
   }
}
