package fr.da2i;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;

@WebServlet("/servlet-HelloWorld")
public class HelloWorld extends HttpServlet
{
  public void service( HttpServletRequest req, HttpServletResponse res ) 
       throws ServletException, IOException
  {
    res.setContentType("text/html;charset=UTF-8");
    PrintWriter out = res.getWriter();
  	out.println("<!doctype html>");
    out.println("<head><title>servlet Hello</title></head><body><center> ");
    out.println("<h1>Hello World Perso</h1> ");
	
	}
}
