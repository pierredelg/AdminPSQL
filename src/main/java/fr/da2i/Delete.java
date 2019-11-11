package fr.da2i;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;

@WebServlet("/servlet-Delete")
public class Delete extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp){

        System.out.println("SERVLET DELETE");

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
        System.out.println("table parameter = " + table);
        if (table == null || table.isEmpty()) {
            table = (String) session.getAttribute("table");
            System.out.println("table session = " + table);

        } else {
            session.setAttribute("table", table);
        }
        System.out.println(" parametre table = " + table);
        String colonne = req.getParameter("colonne");
        System.out.println(" parametre colonne = " + colonne);
        String valeur = req.getParameter("valeur");
        System.out.println(" parametre valeur = " + valeur);


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
            String query = "delete from " + table + " where "+ colonne + " = \'" + valeur + "\' ;";

            session.setAttribute("requete",query);

            System.out.println(query);

            //On exécute la requete
            PreparedStatement ps = null;
            if (con != null) {
                ps = con.prepareStatement(query);
            }

            //On récupere le résultat
            if (ps != null) {
                ps.executeQuery();
            }

        }catch(SQLException e){
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

        try {
            resp.sendRedirect(contextPath + "/servlet-Select?table=" + table + " ");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
