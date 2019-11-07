package fr.da2i;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.sql.*;

/**
 * Servlet permettant d'afficher le contenu d'une table
 * en renseignant le nom de la table en parametre (table=nomDeLaTable)
 */
@WebServlet("/servlet-Select")
public class Select extends HttpServlet
{
  public void service( HttpServletRequest req, HttpServletResponse res ) {

  	//On récupere les variables du fichier web.xml
	  String url = getServletContext().getInitParameter("url");
	  String nom = getServletContext().getInitParameter("login");
	  String mdp = getServletContext().getInitParameter("mdp");
	  String driver = getServletContext().getInitParameter("driver");

	  //On récupere la valeur du parametre table
	  String table = req.getParameter("table");

	  try{
			//On charge le driver
			Class.forName(driver);
		}catch(ClassNotFoundException e){
			e.getMessage();
		}

		Connection con = null;
		
		try{
			//On ouvre la connexion
			con = DriverManager.getConnection(url,nom,mdp);
		}catch(SQLException e){
			e.getMessage();
		}

		try{

			//On constitue la requete select
			String query = "select * from " + table + ";";

			//On exécute la requete
			PreparedStatement ps = null;
			if (con != null) {
				ps = con.prepareStatement(query);
			}

			//On récupere le résultat
			ResultSet rs = null;
			if (ps != null) {
				rs = ps.executeQuery();
			}

			//On récupere le résultat des métadatas
			ResultSetMetaData metaData = null;
			if (rs != null) {
				metaData = rs.getMetaData();
			}

			//On récupere le nombre de colonne de la table
			int nombreColonne = 0;
			if (metaData != null) {
				nombreColonne = metaData.getColumnCount();
			}

			//On initialise le Writer
			PrintWriter out = res.getWriter();

			//On ajoute le type de contenu
			res.setContentType("text/html;charset=UTF-8");

			//On construit la page html
			out.println("<!doctype html>");
			out.println("<head><title>Liste des vols:</title></head><body>");

			out.println("<h1>Table "+ table +" :</h1>");

			out.println("<table>");
			out.println("<thead>");
			out.println("<tr>");

			//On affiche le titre des colonnes de la table
			for(int i = 1;i <= nombreColonne;i++) {
				out.println("<th>" + metaData.getColumnName(i).toUpperCase() + "</th>");
			}

			out.println("</tr>");
			out.println("</thead>");
			out.println("<tbody>");

			while(rs != null && rs.next()){
				out.println("<tr>");
				for(int i = 1;i <= nombreColonne;i++){
					out.println("<td>"+ rs.getObject(i) +"</td>");
				}
				out.println("</tr>");

			}
			out.println(" </tbody>");
			out.println("</table>");
			out.println("</body>");
			out.println("</html>");

		}catch(SQLException | IOException e){
			e.getMessage();
		}

	  try{
			//On ferme la connexion
			if (con != null) {
				con.close();
			}

		}catch(SQLException e){
			e.getMessage();
		}
		
   
	
	}
}
