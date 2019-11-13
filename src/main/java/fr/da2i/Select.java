package fr.da2i;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Servlet permettant d'afficer le contenu d'une table
 * en renseignant le nom de la table en parametre (table=nomDeLaTable)
 * Avec la possibilité de modifier ou supprimer ses valeurs
 * mais aussi l'insertion de nouvelles données.
 * @author DELGRANGE Pierre
 */
@WebServlet("/servlet-Select")
public class Select extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res ) {

        //On récupere les variables du fichier web.xml
        String url = getServletContext().getInitParameter("url");
        String nom = getServletContext().getInitParameter("login");
        String mdp = getServletContext().getInitParameter("mdp");
        String driver = getServletContext().getInitParameter("driver");
        String contextPath = req.getContextPath();

        //Session
        HttpSession session = req.getSession(true);

        //On récupere la valeur des parametres
        String table = req.getParameter("table");
        if(table == null || table.isEmpty()) {
            table = (String) session.getAttribute("table");
        }else{
            session.setAttribute("table",table);
        }

        String requete = (String) session.getAttribute("requete");

        String numeroDeLigne = req.getParameter("numeroDeLigneUpdate");
        int numeroDeLigneUpdate = 0;
        if(numeroDeLigne != null) {
            numeroDeLigneUpdate = Integer.parseInt(numeroDeLigne);
        }
        session.setAttribute("numeroDeLigneUpdate",numeroDeLigneUpdate);

        try{
            //On charge le driver
            Class.forName(driver);
        }catch(ClassNotFoundException e){
            System.err.println(e.getMessage());
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
                session.setAttribute("nombreColonne",nombreColonne);
            }

            //On initialise le Writer
            PrintWriter out = res.getWriter();

            //On ajoute le type de contenu
            res.setContentType("text/html;charset=UTF-8");

            if(requete != null) {
                //On construit la page html
                introHtml(out, "Information sur la table", table, requete, rs, nombreColonne, metaData, contextPath,session);
            }
            else{
                introHtml(out, "Information sur la table", table, query, rs, nombreColonne, metaData, contextPath,session);
            }

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
        doGet(req,res);
    }

    public void introHtml(PrintWriter out, String title, String table, String query, ResultSet rs, int nombreColonne, ResultSetMetaData metaData, String contextPath, HttpSession session){

        String valeurDel = null, colonneId = null;
        int numeroDeLigne = 0;
        boolean updateOk = false;
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
            out.println("<div class=\"alert alert-warning\">Dernière requête exécutée : " + query + "</div>");

            /*TABLEAU*/
            out.println("<table class=\"table table-hover table-bordered table-striped card card-cascade narrower\">");

            //TITRE DES COLONNES DU TABLEAU
            out.println("<tr class=\"thead-dark\" scope=\"row\">");

            Map<Integer,String> nomColonneMap = new HashMap<>();

            for(int i = 1;i <= nombreColonne;i++) {
                String nomColonne = metaData.getColumnName(i);
                if(i == 1){
                    colonneId = nomColonne;
                }
                out.println("<th class=\"text-center align-middle\">" + nomColonne.toUpperCase() + "</th>");
                nomColonneMap.put(i,nomColonne);
            }
            session.setAttribute("nomColonneMap",nomColonneMap);

            out.println("<th class=\"text-center align-middle\"> ACTIONS</th>");
            out.println("</tr>");


            Map<Integer,String> ancienneValeurMap = new HashMap<>();

            //CONTENU DU TABLEAU
            while(rs != null && rs.next()){
                out.println("<tr scope=\"row\">");
                numeroDeLigne++;
                for(int i = 1;i <= nombreColonne;i++) {
                    Object valeur = rs.getObject(i);
                    if (numeroDeLigne == (int) session.getAttribute("numeroDeLigneUpdate")) {

                        out.println("<form class=\"align-items-center\" action=\"" + contextPath + "/servlet-Update\" method=\"post\">");

                        /*FORMULAIRE D'UPDATE*/
                        out.println("<td class=\"text-center align-middle\" scope=\"col\">");
                        out.println("<input class=\"form-control\" type=\"text\" id=\"nouvelleValeur" + i + "\"");
                        out.println("name=\"nouvelleValeur" + i + "\" value=" + valeur + " />");
                        out.println("</td>");
                        if(valeur != null) {
                            ancienneValeurMap.put(i, valeur.toString());
                        }
                        updateOk = true;

                    } else {
                        if (i == 1) {
                            if(valeur != null) {
                                valeurDel = valeur.toString();
                            }
                        }
                        out.println("<td class=\"text-center align-middle\" scope=\"col\">" + valeur + "</td>");
                    }
                }
                session.setAttribute("ancienneValeurMap",ancienneValeurMap);
                /*DERNIERE COLONNE DU TABLEAU (BOUTTONS) */
                out.println("<td class=\"d-flex flex-row\" scope=\"col\">");

                if(updateOk) {
                    /*BOUTON ENREGISTRER UPDATE*/
                    out.println("<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
                    out.println("<input type=\"hidden\" id=\"cle\" name=\"cle\" value=\"" + colonneId + "\"/>");
                    out.println("<input class=\"btn btn-success m-1 waves-effect btn-rounded\" type=\"submit\" value=\"Enregistrer\"/>");
                    out.println("</form>");
                    /*BOUTON ANNULER*/
                    out.println("<form action=\""+ contextPath +"/servlet-Select\" method=\"get\">" );
                    out.println(  "<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
                    out.println(  "<input class=\"btn btn-danger m-1\" type=\"submit\" value=\"Annuler\"/>");
                    out.println("</form>");
                    out.println("</td>");
                }else{
                    /*BOUTON MODIFIER*/
                    out.println("<form action=\"" + contextPath + "/servlet-Select\" method=\"get\">");
                    out.println("<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
                    out.println("<input type=\"hidden\" id=\"numeroDeLigneUpdate\" name=\"numeroDeLigneUpdate\" value=\"" + numeroDeLigne + "\" />");
                    out.println("<input class=\"btn btn-warning m-1 waves-effect btn-rounded\" type=\"submit\" value=\"Modifier\"/>");
                    out.println("</form>");

                    /*BOUTON SUPPRIMER*/
                    out.println("<form action=\""+ contextPath +"/servlet-Delete\" method=\"post\">" );
                    out.println(  "<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
                    out.println( "<input type=\"hidden\" id=\"colonne\" name=\"colonne\" value=\"" + colonneId + "\"/>");
                    out.println(  "<input type=\"hidden\" id=\"valeur\" name=\"valeur\" value=\"" + valeurDel + "\"/>");
                    out.println(  "<input class=\"btn btn-danger m-1\" type=\"submit\" value=\"Supprimer\"/>");
                    out.println("</form>");
                    out.println("</td>");
                }
                out.println("</tr>");
            }

            /*DERNIERE LIGNE DU TABLEAU (FORMULAIRE D'INSERTION)*/
            out.println("<tr scope=\"row\">");
            out.println("<form class=\"align-items-center\" action=\""+ contextPath +"/servlet-Insert\" method=\"post\">");
            for (int i = 1; i <= nombreColonne; i++) {
                out.println("<td class=\"text-center align-middle\" scope=\"col\">");
                out.println("<input class=\"form-control\" type=\"text\" id=\"value" + i +"\" name=\"valeur" + i + "\"/>");
                out.println("</td>");
            }
            out.println("<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
            out.println("<input type=\"hidden\" id=\"nombreColonne\" name=\"nombreColonne\" value=\""+ nombreColonne + "\"/>");

            /*BOUTON ENREGISTRER*/
            out.println("<td class=\"text-center align-middle\" scope=\"col\">");
            out.println("<input class=\"btn btn-primary\" type=\"submit\" value=\"Enregistrer\"/>");
            out.println( "</td>\n");


            /*BALISES DE FERMETURE*/
            out.println("</form>");
            out.println("</tr>");
            out.println("</table>");
            out.println("</body>");
            out.println("</html>");

            updateOk = false;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
