package fr.da2i;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * Servlet permettant d'afficher le contenu d'une table
 * en renseignant le nom de la table en parametre (table=nomDeLaTable)
 */
@WebServlet("/servlet-Insert")
public class Insert extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse res) {

        System.out.println("SERVLET INSERT POST");

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
        System.out.println("table parameter = " + table);
        if (table == null || table.isEmpty()) {
            table = (String) session.getAttribute("table");
            System.out.println("table session = " + table);

        } else {
            session.setAttribute("table", table);
        }

        Integer nombreColonne = (Integer) session.getAttribute("nombreColonne");

        System.out.println("table = " + table);
        System.out.println("nombreColonne = " + nombreColonne);


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
            String requeteSQL = "insert into " + table + " values( ";
            for (int i = 1; i <= nombreColonne; i++) {
                String valeur = req.getParameter("valeur" + i);
                requeteSQL += "\'" + valeur + "\'";
                if (i != nombreColonne) {
                    requeteSQL += " , ";
                }
            }
            requeteSQL += ");";

            session.setAttribute("requete",requeteSQL);
            System.out.println(requeteSQL);


            //On exécute la requete
            PreparedStatement ps = null;
            if (con != null) {
                ps = con.prepareStatement(requeteSQL);
            }

            //On récupere le résultat
            if (ps != null) {
                ps.executeUpdate();
            }

            res.sendRedirect(contextPath + "/servlet-Select?table=" + table + " ");

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
