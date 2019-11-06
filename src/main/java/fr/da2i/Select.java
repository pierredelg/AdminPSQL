package fr.da2i;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.sql.*;

@WebServlet("/servlet-Select")
public class Select extends HttpServlet
{
  public void service( HttpServletRequest req, HttpServletResponse res ) 
       throws ServletException, IOException
  {

		try{
			String driver = getServletContext().getInitParameter("driver");
			//On charge le driver
			Class.forName(driver);
		
		}catch(ClassNotFoundException e){
			e.getMessage();

		}

		Connection con = null;
		
		try{
			String url = getServletContext().getInitParameter("url");
			String nom = getServletContext().getInitParameter("login");
			String mdp = getServletContext().getInitParameter("mdp");
			//On ouvre la connexion

			con = DriverManager.getConnection(url,nom,mdp);

		}catch(Exception e){
			e.getMessage();
		}

		try{
			String table = req.getParameter("table");

			String query = "select * from " + table + ";";
			PreparedStatement ps = con.prepareStatement(query);;

			ResultSet rs = ps.executeQuery();

			PrintWriter out = res.getWriter();

			res.setContentType("text/html;charset=UTF-8");

			out.println("<!doctype html>");
			out.println("<head><title>Liste des vols:</title></head><body>");
			out.println("<h1>Table "+ table +" :</h1>");


			ResultSetMetaData metaData = rs.getMetaData();
			int nombreColonne = metaData.getColumnCount();
			System.out.println("nombre de colonne " + nombreColonne);

			while(rs.next()){
				for(int i = 1;i <= nombreColonne;i++){
					out.println(metaData.getColumnName(i));

					out.println(rs.getObject(i));
				}
			}

			out.println("</body></html>");

			/*out.println("<table><thead>");
		    out.println("<tr>");
			out.println("<th>ano</th>");
			out.println("<th>pno</th>");
			out.println("<th>lno</th>");
			out.println("<th>hdep</th>");
			out.println("<th>harr</th>");
		    out.println("</tr>");
			out.println("</thead>");
			out.println("<tbody>");*/
		   
		    

			while (rs.next())
			{
	 		/*	out.println("<tr>");
				String ano = rs.getString("ano");
				out.println("<td>"+ano +"</td>");
				String pno = rs.getString("pno");
				out.println("<td>"+pno +"</td>");
				String lno = rs.getString("lno");
				out.println("<td>"+lno +"</td>");
				String hdep = rs.getString("hdep");
				out.println("<td>"+hdep +"</td>");
				String harr = rs.getString("harr");
				out.println("<td>"+harr +"</td>");
	 			out.println("</tr>");
				out.println(" </tbody>");
				out.println("</table>");
				*/

			}
		}catch(Exception e){
			e.getMessage();
		}

		try{
			
			//On ferme la connexion
			con.close();
		
		}catch(Exception e){
			e.getMessage();
		}
		
   
	
	}
}
