package fr.da2i;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Servlet permettant d'ajouter une ligne dans une le contenu d'une table
 * en renseignant le nom de la table en parametre (table=nomDeLaTable)
 * @author DELGRANGE Pierre
 */
@WebServlet("/servlet-Insert")
public class Insert extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse res) {

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
        if (table == null || table.isEmpty()) {
            table = (String) session.getAttribute("table");

        } else {
            session.setAttribute("table", table);
        }

        Integer nombreColonne = (Integer) session.getAttribute("nombreColonne");
        try {
            //On charge le driver
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            session.setAttribute("erreur",e.getMessage());
        }

        Connection con = null;

        try {
            //On ouvre la connexion
            con = DriverManager.getConnection(url, nom, mdp);
        } catch (SQLException e) {
            session.setAttribute("erreur",e.getMessage());
        }

        try {
            //On constitue la requete insert
            String requeteSQL = "insert into " + table + " values( ";
            for (int i = 1; i <= nombreColonne; i++) {
                String valeur = req.getParameter("valeur" + i);
                requeteSQL += "\'" + valeur + "\'";
                if (i != nombreColonne) {
                    requeteSQL += " , ";
                }
            }
            requeteSQL += ");";

            //On ajoute la requete en session
            session.setAttribute("requete",requeteSQL);

            //On exécute la requete
            PreparedStatement ps = null;
            if (con != null) {
                ps = con.prepareStatement(requeteSQL);
            }

            //On récupere le résultat
            if (ps != null) {
                ps.executeUpdate();
            }


        } catch (SQLException e) {
            session.setAttribute("erreur",e.getMessage());
        }

        try {
            //On ferme la connexion
            if (con != null) {
                con.close();
            }

        } catch (SQLException e) {
            session.setAttribute("erreur",e.getMessage());
        }
        try {
            res.sendRedirect(contextPath + "/servlet-Select?table=" + table + " ");
        } catch (IOException e) {
            session.setAttribute("erreur",e.getMessage());
        }
    }
}
