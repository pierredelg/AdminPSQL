package fr.da2i;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * Servlet permettant d'afficher le contenu d'une table
 * en renseignant le nom de la table en parametre (table=nomDeLaTable)
 */
@WebServlet("/servlet-Insert")
public class Insert extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res) {

        //On récupere les variables du fichier web.xml
        String url = getServletContext().getInitParameter("url");
        String nom = getServletContext().getInitParameter("login");
        String mdp = getServletContext().getInitParameter("mdp");
        String driver = getServletContext().getInitParameter("driver");

        //On récupere la valeur du parametre table
        String table = req.getParameter("table");

        try {
            //On charge le driver
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.getMessage();
        }

        Connection con = null;

        try {
            //On ouvre la connexion
            con = DriverManager.getConnection(url, nom, mdp);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        try {

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
            out.println("<head>");
            out.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
            out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
            out.println("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css\">");
            out.println("<title>Résultat de l'insertion dans la table " + table + "</title>");
            out.println("</head>");
            out.println("<body>");

            out.println("<h1>Insérez vos données pour la table " + table + " :</h1>");

            out.println("<form action=\"http://localhost:8080/projet7/servlet-Insert\" method=\"post\">");

            //On affiche le titre des colonnes de la table
            for (int i = 1; i <= nombreColonne; i++) {

                out.println("<label for=\"value" + i + "\">" + metaData.getColumnName(i).toUpperCase() + "</label>");
                out.println("<input class=\"form-group row\" type=\"text\" id=\"value" + i + "\" name=\"valeur" + i + "\"><br><br>");

            }
            out.println("<input id=\"table\" name=\"table\" type=\"hidden\" value=\"" + table + "\">");
            out.println("<input id=\"nombreColonne\" name=\"nombreColonne\" type=\"hidden\" value=\"" +
                    nombreColonne + "\">");

            out.println("<input type=\"submit\" value=\"Valider\">\n");

            out.println("</form>");
            out.println("</body>");
            out.println("</html>");

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

    public void doPost(HttpServletRequest req, HttpServletResponse res) {


        //On récupere les variables du fichier web.xml
        String url = getServletContext().getInitParameter("url");
        String nom = getServletContext().getInitParameter("login");
        String mdp = getServletContext().getInitParameter("mdp");
        String driver = getServletContext().getInitParameter("driver");


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

            //On exécute la requete
            PreparedStatement ps1 = null;
            if (con != null) {
                ps1 = con.prepareStatement(queryInsert);
            }

            //On récupere le résultat
            int resultatInsert = 0;
            if (ps1 != null) {
                resultatInsert = ps1.executeUpdate();
            }

            //On ajoute le type de contenu
            res.setContentType("text/html;charset=UTF-8");

            //On construit la page html
            out.println("<!doctype html>");
            out.println("<head>");
            out.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
            out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
            out.println("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css\">");
            out.println("<title>Résultat de l'insertion dans la table " + table + "</title>");
            out.println("</head>");
            out.println("<body>");

            out.println("<h1>Résultat de l'insertion dans la table  " + table + " :</h1>");

            out.println("<div class=\"alert alert-warning\">Requête exécutée : " + queryInsert + "</div>");

            out.println("<h2>Table " + table + " mise à jour:</h2>");

            if (resultatInsert != 0) {

                //On constitue la requete select
                String query = "select * from " + table + ";";

                //On prépare la requete
                PreparedStatement ps = null;
                if (con != null) {
                    ps = con.prepareStatement(query);
                }

                //On exécute la requete
                ResultSet rs = null;
                if (ps != null) {
                    rs = ps.executeQuery();
                }

                //On récupere le résultat des métadatas
                ResultSetMetaData metaData = null;
                if (rs != null) {
                    metaData = rs.getMetaData();
                }

                out.println("<table class=\"table table-hover\">");
                out.println("<thead>");
                out.println("<tr>");

                //On affiche le titre des colonnes de la table
                for (int i = 1; i <= nombreColonne; i++) {
                    out.println("<th>" + metaData.getColumnName(i).toUpperCase() + "</th>");
                }

                out.println("</tr>");
                out.println("</thead>");
                out.println("<tbody>");

                while (rs != null && rs.next()) {
                    out.println("<tr>");
                    for (int i = 1; i <= nombreColonne; i++) {
                        out.println("<td>" + rs.getObject(i) + "</td>");
                    }
                    out.println("</tr>");

                }
                out.println(" </tbody>");
                out.println("</table>");


            } else {
                out.println("<p>Erreur dans l'insertion des données</p>");
            }
            out.println("</body>");
            out.println("</html>");

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
}
