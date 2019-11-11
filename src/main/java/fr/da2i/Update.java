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

@WebServlet("/servlet-Update")
public class Update extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {

        System.out.println("SERVLET UPDATE");

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

        int nombreColonne = (int) session.getAttribute("nombreColonne");
        System.out.println("Nombre de colonne " + nombreColonne);

        Map<Integer, String> nomColonneMap = (Map<Integer, String>) session.getAttribute("nomColonneMap");
        System.out.println(nomColonneMap);
        Map<Integer, String> ancienneValeurMap = (Map<Integer, String>) session.getAttribute("ancienneValeurMap");
        System.out.println(ancienneValeurMap);

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
            e.getMessage();
        }

        try {
            String where = "";
            String set = "";
            for (int i = 1; i <= nombreColonne; i++) {
                set += nomColonneMap.get(i) + " = " + "\'" + req.getParameter("nouvelleValeur" + i) + "\' ";
                where += nomColonneMap.get(i) + " = " + "\'" + ancienneValeurMap.get(i) + "\' ";
                if (i != nombreColonne) {
                    set += " , ";
                    where += " and ";
                }
            }

            //On constitue la requete select
            String query = "update " + table + " set " + set + " where " + where + ";";

            session.setAttribute("requete", query);

            System.out.println(query);

            //On exécute la requete
            PreparedStatement ps = null;
            if (con != null) {
                ps = con.prepareStatement(query);
            }

            //On récupere le résultat
            if (ps != null) {
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.getMessage();
        }

        try {
            //On ferme la connexion
            if (con != null) {
                con.close();
            }

        } catch (SQLException e) {
            e.getMessage();
        }
        try {
            System.out.println("fin update   table = " + table);
            resp.sendRedirect(contextPath + "/servlet-Select?table=" + table + " ");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
