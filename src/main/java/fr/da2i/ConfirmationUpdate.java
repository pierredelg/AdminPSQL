package fr.da2i;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class ConfirmationUpdate extends HttpServlet {


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {

        System.out.println("SERVLET CONFIRMATION UPDATE");

        //On récupere les variables du fichier web.xml
        String url = getServletContext().getInitParameter("url");
        String nom = getServletContext().getInitParameter("login");
        String mdp = getServletContext().getInitParameter("mdp");
        String driver = getServletContext().getInitParameter("driver");
        String contextPath = req.getContextPath();

        //Session
        HttpSession session = req.getSession(true);

        //On récupere la valeur du parametre table
        String table = req.getParameter("table");
        System.out.println(" parametre table = " + table);
        String colonne = req.getParameter("colonne");
        System.out.println(" parametre colonne = " + colonne);
        String nouvelleValeur = req.getParameter("nouvelleValeur");
        System.out.println(" parametre valeur = " + nouvelleValeur);
        String ancienneValeur = req.getParameter("valeur");
        System.out.println(" parametre valeurCondition = " + ancienneValeur);

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

            //On récupere le nombre de colonne de la table
            int nombreDeColonne = (int) session.getAttribute("nombreColonne");

            //On initialise le Writer
            PrintWriter out = resp.getWriter();

            //On ajoute le type de contenu
            resp.setContentType("text/html;charset=UTF-8");

          //  introHtml(out,"Information sur la table",table,query,rs,nombreDeColonne,metaData,contextPath,session);

        }catch(IOException e){
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

    public void introHtml(PrintWriter out, String title, String table, String query , ResultSet rs, int nombreColonne, ResultSetMetaData metaData, String contextPath, HttpSession session){

        String valeur = null, colonne = null;
        try{

            /*HEAD HTML*/
            out.println("<!doctype html>");
            out.println("<head>");
            out.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
            out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
            out.println("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css\">");
            out.println("<title>"+ title + " " + table + "</title>");
            out.println("</head>");
            out.println("<body class=\"container\">");

            /*H1*/
            out.println("<h1 class=\"text-secondary text-center\">Affichage de la table "+ table.toUpperCase() +" :</h1>");

            /*AFFICHAGE REQUETE*/
            out.println("<div class=\"alert alert-warning\">Requête exécutée : " + query + "</div>");

            /*TABLEAU*/
            out.println("<table class=\"table table-hover table-bordered table-striped card card-cascade narrower\">");

            out.println();
            //TITRE DES COLONNES DU TABLEAU
            out.println("<tr class=\"thead-dark\" scope=\"row\">");
            for(int i = 1;i <= nombreColonne;i++) {
                if(i == 1){
                    colonne = metaData.getColumnName(i).toUpperCase();
                }
                out.println("<th class=\"text-center align-middle\">" + metaData.getColumnName(i).toUpperCase() + "</th>");
            }
            out.println("<th class=\"text-center align-middle\"> ACTIONS</th>");
            out.println("</tr>");


            //CONTENU DU TABLEAU
            while(rs != null && rs.next()){
                out.println("<tr scope=\"row\">");
                for(int i = 1;i <= nombreColonne;i++){
                    if(i == 1){
                        valeur = rs.getObject(i).toString();
                    }
                    out.println("<td class=\"text-center align-middle\" scope=\"col\">"+ rs.getObject(i) +"</td>");
                }
                /*DERNIERE COLONNE DU TABLEAU (BOUTTONS) */
                out.println("<td class=\"d-flex flex-row\" scope=\"col\">"+

                /*BOUTON MODIFIER*/
                "<form action=\""+ contextPath + "/UpdateForm.html\" method=\"post\">"+
                "<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>"+
                "<input class=\"btn btn-warning m-1 waves-effect btn-rounded\" type=\"submit\" value=\"Modifier\"/></form>" +

                /*BOUTON SUPPRIMER*/
                "<form action=\""+ contextPath +"/servlet-Delete\" method=\"post\">" +
                "<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>"+
                "<input type=\"hidden\" id=\"colonne\" name=\"colonne\" value=\"" + colonne + "\"/>"+
                "<input type=\"hidden\" id=\"valeur\" name=\"valeur\" value=\"" + valeur + "\"/>"+
                "<input class=\"btn btn-danger m-1\" type=\"submit\" value=\"Supprimer\"/></form></td>");
                out.println("</tr>");
            }

            /*DERNIERE LIGNE DU TABLEAU (FORMULAIRE D'INSERTION)*/
            out.println("<tr scope=\"row\">");
            out.println("<form class=\"align-items-center\" action=\""+ contextPath +"/servlet-Insert\" method=\"post\">");
            for (int i = 1; i <= nombreColonne; i++) {
                out.println("<td class=\"text-center align-middle\" scope=\"col\"><input class=\"form-control\" type=\"text\" id=\"value" + i +
                        "\" name=\"valeur" + i + "\"/></td>");
            }
            out.println("<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
            out.println("<input type=\"hidden\" id=\"nombreColonne\" name=\"nombreColonne\" value=\"" +
                    nombreColonne + "\"/>");

            /*BOUTON ENREGISTRER*/
            out.println("<td class=\"text-center align-middle\" scope=\"col\"><input class=\"btn btn-primary\" type=\"submit\" value=\"Enregistrer\"/></td>\n");


            /*BALISES DE FERMETURE*/
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
