package fr.da2i;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

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

        //On récupere la valeur des parametres et des valeurs misent en session
        String table = req.getParameter("table");
        if (table == null || table.isEmpty()) {
            table = (String) session.getAttribute("table");

        } else {
            session.setAttribute("table", table);
        }

        int nombreColonne = (int) session.getAttribute("nombreColonne");
        Map<Integer, String> nomColonneMap = (Map<Integer, String>) session.getAttribute("nomColonneMap");

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

            //On constitue la clause where
            String where = "";
            for (int i = 1; i <= nombreColonne; i++) {
                //Pour chaque colonne on ajoute la vérification de la valeur dans la clause
                where += nomColonneMap.get(i) + " = " + "\'" + req.getParameter("valeur"+ i ) + "\' ";
                if (i != nombreColonne) {
                    where += " and ";
                }
            }

            //On constitue la requete delete
            String query = "delete from " + table + " where "+ where + " ;";

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
            session.setAttribute("erreur",e.getMessage());
        }

        try{
            //On ferme la connexion
            if (con != null) {
                con.close();
            }

        }catch(SQLException e){
            session.setAttribute("erreur",e.getMessage());
        }

        try {
            resp.sendRedirect(contextPath + "/servlet-Select?table=" + table + " ");
        } catch (IOException e) {
            session.setAttribute("erreur",e.getMessage());
        }
    }
}
