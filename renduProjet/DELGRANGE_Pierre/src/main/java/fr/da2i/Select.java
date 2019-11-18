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
import java.util.Iterator;
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

        //On récupere la valeur des parametres et des valeurs misent en session
        String table = req.getParameter("table");
        if(table == null || table.isEmpty()) {
            table = (String) session.getAttribute("table");
        }else{
            session.setAttribute("table",table);
        }

        String numeroDeLigne = req.getParameter("numeroDeLigneUpdate");
        int numeroDeLigneUpdate = 0;
        if(numeroDeLigne != null) {
            numeroDeLigneUpdate = Integer.parseInt(numeroDeLigne);
        }
        session.setAttribute("numeroDeLigneUpdate",numeroDeLigneUpdate);

        String requete = (String) session.getAttribute("requete");

        //On initialise le Writer
        PrintWriter out = null;
        try {
            out = res.getWriter();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        //On ajoute le type de contenu
        res.setContentType("text/html;charset=UTF-8");

        introHTML(out,"Informations sur la table",table);

        try{
            //On charge le driver
            Class.forName(driver);
        }catch(ClassNotFoundException e){
            session.setAttribute("erreur",e.getMessage());
        }

        Connection con = null;

        try{
            //On ouvre la connexion
            con = DriverManager.getConnection(url,nom,mdp);
        }catch(SQLException e){
            session.setAttribute("erreur",e.getMessage());
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

            //On construit la page html
            if(requete != null) {
                corpsHtml(out, table, requete, rs, nombreColonne, metaData, contextPath,session);
            }
            else{
                corpsHtml(out, table, query, rs, nombreColonne, metaData, contextPath,session);
            }

        }catch(SQLException e){
            session.setAttribute("erreur",e.getMessage());
            afficheErreur(out,session,e.getMessage(),contextPath);
        }

        try{
            //On ferme la connexion
            if (con != null) {
                con.close();
            }

        }catch(SQLException e){
            session.setAttribute("erreur",e.getMessage());
            afficheErreur(out,session,e.getMessage(),contextPath);
        }
        finHTML(out);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) {
        doGet(req,res);
    }

    public void introHTML(PrintWriter out, String title,String table){
        /*HEAD HTML*/
        out.println("<!doctype html>");
        out.println("<head>");
        out.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        out.println("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css\">");
        if(table != null) {
            out.println("<title>" + title + " " + table.toUpperCase() + "</title>");
        }
        out.println("</head>");
        out.println("<body class=\"container\">");
    }
    public void finHTML(PrintWriter out){
        out.println("</body>");
        out.println("</html>");
    }
    public void afficheErreur(PrintWriter out,HttpSession session,String erreur,String contextPath){
        out.println("<div class=\"alert alert-danger\">" + erreur + "</div>");
        out.println("<a href=\""+ contextPath +"/index.jsp\"><input class=\"btn btn-primary m-1 waves-effect btn-rounded\" type=\"button\" value=\"Retour à l'accueil\"/>");
        session.removeAttribute("erreur");
    }

    public void corpsHtml(PrintWriter out, String table, String query, ResultSet rs, int nombreColonne, ResultSetMetaData metaData, String contextPath, HttpSession session){

        String colonneId = null;
        int numeroDeLigne = 0;
        boolean updateOk = false;
        try{
            /*H1*/
            if(table != null) {
                out.println("<h1 class=\"text-secondary text-center\">Affichage de la table " + table.toUpperCase() + " :</h1>");
            }
            /*ERREUR*/
            String erreur = (String) session.getAttribute("erreur");
            if(erreur != null && !erreur.isEmpty()) {
               afficheErreur(out,session,erreur,contextPath);
            }else {
                /*AFFICHAGE REQUETE*/
                out.println("<div class=\"alert alert-warning\">Dernière requête exécutée : " + query + "</div>");

                /*TABLEAU*/
                out.println("<table class=\"table table-hover table-bordered table-striped card card-cascade narrower\">");

                //TITRE DES COLONNES DU TABLEAU
                out.println("<tr class=\"thead-dark\" scope=\"row\">");

                Map<Integer, String> nomColonneMap = new HashMap<>();

                for (int i = 1; i <= nombreColonne; i++) {
                    String nomColonne = metaData.getColumnName(i);
                    if (i == 1) {
                        colonneId = nomColonne;
                    }
                    out.println("<th class=\"text-center align-middle\">" + nomColonne.toUpperCase() + "</th>");
                    //On ajoute les titres de la colonne dans la hashmap
                    nomColonneMap.put(i, nomColonne);
                }
                session.setAttribute("nomColonneMap", nomColonneMap);

                out.println("<th class=\"text-center align-middle\"> ACTIONS</th>");
                out.println("</tr>");

                Map<Integer, String> ancienneValeurMap = new HashMap<>();

                //CONTENU DU TABLEAU
                while (rs != null && rs.next()) {

                    Map<Integer, String> valeurLigneMap = new HashMap<>();
                    out.println("<tr scope=\"row\">");
                    numeroDeLigne++;
                    for (int i = 1; i <= nombreColonne; i++) {
                        Object valeur = rs.getObject(i);
                        //Si le numéro de la ligne correspond à la ligne selectionnée pour l'update
                        if (numeroDeLigne == (int) session.getAttribute("numeroDeLigneUpdate")) {
                            /*FORMULAIRE D'UPDATE*/
                            out.println("<form class=\"align-items-center\" action=\"" + contextPath + "/servlet-Update\" method=\"post\">");
                            out.println("<td class=\"text-center align-middle\" scope=\"col\">");
                            out.println("<input class=\"form-control\" type=\"text\" id=\"nouvelleValeur" + i + "\"");
                            out.println("name=\"nouvelleValeur" + i + "\" value=" + valeur + " />");
                            out.println("</td>");
                            //On ajoute les valeurs de la ligne dans la hashmap
                            if (valeur != null) {
                                ancienneValeurMap.put(i, valeur.toString());
                            }
                            updateOk = true;

                        } else {
                            if (valeur != null) {
                                valeurLigneMap.put(i, valeur.toString());
                            }
                            out.println("<td class=\"text-center align-middle\" scope=\"col\">" + valeur + "</td>");
                        }
                    }
                    session.setAttribute("ancienneValeurMap", ancienneValeurMap);

                    /*DERNIERE COLONNE DU TABLEAU (BOUTTONS) */
                    out.println("<td class=\"d-flex flex-row\" scope=\"col\">");

                    //aprés le bouton modifier
                    if (updateOk) {
                        /*BOUTON ENREGISTRER UPDATE*/
                        out.println("<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
                        out.println("<input type=\"hidden\" id=\"cle\" name=\"cle\" value=\"" + colonneId + "\"/>");
                        out.println("<input class=\"btn btn-success m-1 waves-effect btn-rounded\" type=\"submit\" value=\"Enregistrer\"/>");
                        out.println("</form>");
                        /*BOUTON ANNULER*/
                        out.println("<form action=\"" + contextPath + "/servlet-Select\" method=\"get\">");
                        out.println("<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
                        out.println("<input class=\"btn btn-danger m-1\" type=\"submit\" value=\"Annuler\"/>");
                        out.println("</form>");
                        out.println("</td>");
                    } else {
                        /*BOUTON MODIFIER*/
                        out.println("<form action=\"" + contextPath + "/servlet-Select\" method=\"get\">");
                        out.println("<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
                        out.println("<input type=\"hidden\" id=\"numeroDeLigneUpdate\" name=\"numeroDeLigneUpdate\" value=\"" + numeroDeLigne + "\" />");
                        out.println("<input class=\"btn btn-warning m-1 waves-effect btn-rounded\" type=\"submit\" value=\"Modifier\"/>");
                        out.println("</form>");

                        /*BOUTON SUPPRIMER*/
                        out.println("<form action=\"" + contextPath + "/servlet-Delete\" method=\"post\">");
                        out.println("<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
                        //On ajoute les valeurs de la ligne
                        Iterator<Map.Entry<Integer, String>> iterator = valeurLigneMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<Integer, String> entry = iterator.next();
                            out.println("<input type=\"hidden\" id=\"valeur" + entry.getKey() + "\" name=\"valeur" + entry.getKey() + "\" value=\"" + entry.getValue() + "\"/>");
                        }
                        out.println("<input class=\"btn btn-danger m-1\" type=\"submit\" value=\"Supprimer\"/>");
                        out.println("</form>");
                        out.println("</td>");
                    }
                    out.println("</tr>");
                }

                /*DERNIERE LIGNE DU TABLEAU (FORMULAIRE D'INSERTION)*/
                out.println("<tr scope=\"row\">");
                out.println("<form class=\"align-items-center\" action=\"" + contextPath + "/servlet-Insert\" method=\"post\">");
                for (int i = 1; i <= nombreColonne; i++) {
                    out.println("<td class=\"text-center align-middle\" scope=\"col\">");
                    out.println("<input class=\"form-control\" type=\"text\" id=\"value" + i + "\" name=\"valeur" + i + "\"/>");
                    out.println("</td>");
                }
                out.println("<input type=\"hidden\" id=\"table\" name=\"table\" value=\"" + table + "\"/>");
                out.println("<input type=\"hidden\" id=\"nombreColonne\" name=\"nombreColonne\" value=\"" + nombreColonne + "\"/>");

                /*BOUTON ENREGISTRER*/
                out.println("<td class=\"text-center align-middle\" scope=\"col\">");
                out.println("<input class=\"btn btn-primary\" type=\"submit\" value=\"Enregistrer\"/>");
                out.println("</td>\n");


                /*BALISES DE FERMETURE*/
                out.println("</form>");
                out.println("</tr>");
                out.println("</table>");
            }

        } catch (SQLException e) {
            out.println(e.getMessage());
        }
    }
}
