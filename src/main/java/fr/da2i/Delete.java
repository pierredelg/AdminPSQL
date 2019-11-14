package fr.da2i;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;

/**
 * Servlet permettant de supprimer une ligne dans une table
 * en renseignant le nom de la table en parametre (table=nomDeLaTable)
 * @author DELGRANGE Pierre
 */
@WebServlet("/servlet-Delete")
public class Delete extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp){

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
        if (table == null || table.isEmpty()) {
            table = (String) session.getAttribute("table");

        } else {
            session.setAttribute("table", table);
        }

        String colonne = req.getParameter("colonne");
        String valeur = req.getParameter("valeur");
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

            //On constitue la requete delete
            String query = "delete from " + table + " where "+ colonne + " = \'" + valeur + "\' ;";

            //On ajoute la requete en session
            session.setAttribute("requete",query);

            //On exécute la requete
            PreparedStatement ps = null;
            if (con != null) {
                ps = con.prepareStatement(query);
            }

            //On récupere le résultat
            if (ps != null) {
                ps.executeUpdate();
            }

        }catch(SQLException e){
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

        try {
            resp.sendRedirect(contextPath + "/servlet-Select?table=" + table + " ");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }


    }
}
