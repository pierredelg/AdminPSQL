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
@WebServlet("/servlet-SelectUnique")
public class SelectUnique extends HttpServlet
{
    public void doGet( HttpServletRequest req, HttpServletResponse res ) {

        //On récupere les variables du fichier web.xml
        String url = getServletContext().getInitParameter("url");
        String nom = getServletContext().getInitParameter("login");
        String mdp = getServletContext().getInitParameter("mdp");
        String driver = getServletContext().getInitParameter("driver");
        String contextPath = req.getContextPath();

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
            System.err.println(e.getMessage());
        }

        try{

            //On constitue la requete select
            String query = "select * from " + table + ";";

            System.out.println(query);

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
            introHtml(out,"Information sur la table",table,query,rs,nombreColonne,metaData,contextPath);

        }catch(SQLException | IOException e){
            System.err.println(e.getMessage());
        }

        try{
            //On ferme la connexion
            if (con != null) {
                con.close();
            }

        }catch(SQLException e){
            System.err.println(e.getMessage());
        }
    }
    public void doPost(HttpServletRequest req, HttpServletResponse res) {


        //On récupere les variables du fichier web.xml
        String url = getServletContext().getInitParameter("url");
        String nom = getServletContext().getInitParameter("login");
        String mdp = getServletContext().getInitParameter("mdp");
        String driver = getServletContext().getInitParameter("driver");
        String contextPath = req.getContextPath();


        //On récupere la valeur des parametres
        String table = req.getParameter("table");
        String nombreColonneS = req.getParameter("nombreColonne");
        int nombreColonne = Integer.parseInt(nombreColonneS);


        try {
            //On charge le driver
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }

        Connection con = null;

        try {
            //On ouvre la connexion
            con = DriverManager.getConnection(url, nom, mdp);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        try {

            //On initialise le Writer
            PrintWriter out = res.getWriter();

            //On constitue la requete select
            String queryInsert = "insert into " + table + " values( ";
            for (int i = 1; i <= nombreColonne; i++) {
                String valeur = req.getParameter("valeur" + i);
                queryInsert += "\'" + valeur + "\'";
                if (i != nombreColonne) {
                    queryInsert += " , ";
                }
            }
            queryInsert += ");";

            System.out.println(queryInsert);


            //On prépare la requete
            PreparedStatement ps1 = null;
            if (con != null) {
                ps1 = con.prepareStatement(queryInsert);
            }

            //On execute la requete
            if (ps1 != null) {
                ps1.executeUpdate();
            }


            //On constitue la requete select
            String query = "select * from " + table + ";";
            ResultSet rs = null;
            System.out.println(query);

            //On prépare la requete
            PreparedStatement ps2 = null;
            if (con != null) {
                ps2 = con.prepareStatement(query);
            }

            if (ps2 != null) {
                rs = ps2.executeQuery();
            }

            //On récupere le résultat des métadatas
            ResultSetMetaData metaData = null;
            if (rs != null) {
                metaData = rs.getMetaData();
            }

            //On ajoute le type de contenu
            res.setContentType("text/html;charset=UTF-8");

            //On construit la page html
            introHtml(out,"Information sur la table",table,queryInsert,rs,nombreColonne,metaData,contextPath);

        } catch (SQLException | IOException e) {
            System.err.println(e.getMessage());
        }


        try {
            //On ferme la connexion
            if (con != null) {
                con.close();
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void introHtml(PrintWriter out,String title,String table, String query ,ResultSet rs,int nombreColonne,ResultSetMetaData metaData,String contextPath){

        try{
            //On construit la page html
            out.println("<!doctype html>");

            out.println("<head>");
            out.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
            out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
            out.println("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css\">");
            out.println("<title>"+ title + " " + table + "</title>");
            out.println("</head>");

            out.println("<body class=\"container\">");

            out.println("<h1 class=\"text-secondary text-center\">Affichage de la table "+ table.toUpperCase() +" :</h1>");

            out.println("<div class=\"alert alert-warning\">Requête exécutée : " + query + "</div>");

            out.println("<table class=\"table table-hover table-bordered table-striped card card-cascade narrower\">");
            out.println("<tr class=\"thead-dark\" scope=\"row\">");

            String valeur = null, colonne = null;

            //On affiche le titre des colonnes de la table
            for(int i = 1;i <= nombreColonne;i++) {
                if(i == 1){
                    colonne = metaData.getColumnName(i).toUpperCase();
                }
                out.println("<th class=\"text-center align-middle\">" + metaData.getColumnName(i).toUpperCase() + "</th>");
            }
            out.println("<th class=\"text-center align-middle\"> ACTIONS</th>");

            out.println("</tr>");
            while(rs != null && rs.next()){
                out.println("<tr scope=\"row\">");
                for(int i = 1;i <= nombreColonne;i++){
                    if(i == 1){
                        valeur = rs.getObject(i).toString();
                    }
                    out.println("<td class=\"text-center align-middle\" scope=\"col\">"+ rs.getObject(i) +"</td>");
                }
                out.println("<td class=\"d-flex flex-row\" scope=\"col\"><form action=\""+ contextPath + "/UpdateForm.html\" method=\"post\">" +
                        "<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>"+
                        "<input type=\"hidden\" id=\"colonne\" name=\"colonne\" value=\"" + colonne + "\"/>"+
                        "<input type=\"hidden\" id=\"valeur\" name=\"valeur\" value=\"" + valeur + "\"/>"+
                        "<input class=\"btn btn-warning m-1 waves-effect btn-rounded\" type=\"submit\" value=\"Modifier\"/></form>" +
                        "<form action=\""+ contextPath +"/servlet-Delete\" method=\"post\">" +
                        "<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>"+
                        "<input type=\"hidden\" id=\"colonne\" name=\"colonne\" value=\"" + colonne + "\"/>"+
                        "<input type=\"hidden\" id=\"valeur\" name=\"valeur\" value=\"" + valeur + "\"/>"+
                        "<input class=\"btn btn-danger m-1\" type=\"submit\" value=\"Supprimer\"/></form></td>");
                out.println("</tr>");
            }
            out.println("<tr scope=\"row\">");
            out.println("<form class=\"align-items-center\" action=\"/servlet-SelectUnique\" method=\"post\">");


            for (int i = 1; i <= nombreColonne; i++) {

                out.println("<td class=\"text-center align-middle\" scope=\"col\"><input class=\"form-control\" type=\"text\" id=\"value" + i +
                        "\" name=\"valeur" + i + "\"/></td>");

            }
            out.println("<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
            out.println("<input type=\"hidden\" id=\"nombreColonne\" name=\"nombreColonne\" value=\"" +
                    nombreColonne + "\"/>");
            out.println("<td class=\"text-center align-middle\" scope=\"col\"><input class=\"btn btn-primary\" type=\"submit\" value=\"Enregistrer\"/></td>\n");
            out.println("</form>");
            out.println("</tr>");
            out.println("</table>");
            out.println("</body>");
            out.println("</html>");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
