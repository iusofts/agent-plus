package com.iusofts.agentplus.web.common.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author 
 * @date 2019/11/2
 */
@Tag(name = "AsyncTest", description = "AsyncTest")
@WebServlet(asyncSupported = true, urlPatterns = "/async")
public class SyncServletTest extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext asyncContext = null;
        PrintWriter writer = null;
        try {
            resp.setHeader("Cache-Control", "private");
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Connection", "keep-alive");
            resp.setHeader("Proxy-Connection", "keep-alive");
            resp.setContentType("text/html;charset=UTF-8");

            asyncContext = req.startAsync();
            writer = asyncContext.getResponse().getWriter();
            for (int i = 0; i <= 100; i++) {
                Thread.sleep(300);
                writer.write(i + "%" + "\n");
                System.out.println(i + "%");
                writer.flush();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (null != writer) {
                writer.close();
            }
            if (null != asyncContext) {
                asyncContext.complete();
            }
        }
    }
}
