/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Servlets;

import java.io.IOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author De Almeida Luis
 */
public class DBAccess extends HttpServlet 
{

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        try 
        {
        /*------------------DEMARRAGE CONNEXION A LA DATABASE-----------------*/

            String username ="root"; 
            String password ="1234";
            
            //Données pour la connexion
            String database ="Dicom";
            String port = "3306";
            String host = "localhost";
            
            String url = "jdbc:mysql://"+host+ ":" + port + "/" + database;
            
            try
            {
                try
                { 
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                } catch (ClassNotFoundException ex)
                {
                    Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            catch (InstantiationException ex)
            {
                Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IllegalAccessException ex)
            {
                Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Connection conn;           
            conn = DriverManager.getConnection (url,username,password);
  
            System.out.println("Connexion à la base de données réussie.");
            /*-----------------FIN CONNEXION A LA DATABASE--------------------*/

            /*------EXTRACTION DU NOM DES COLONNES DE LA TABLE 'PATIENT'------*/
            ArrayList<String> patientCol = this.getColumnNames("Patient", conn);

            /*-----------------------FIN--------------------------------------*/
            
            //Extraction de données de la database
            try
            {
                /*------------EXTRACTION DONNEES TABLE 'PATIENT'--------------*/
                String colNames="";
                for(int i =0 ; i< patientCol.size() ; i++)
                {
                    if(i+1<patientCol.size())
                        colNames = colNames + patientCol.get(i)+",";
                    else
                        colNames = colNames + patientCol.get(i);
                }
                
                String identifiant = request.getParameter("id");

                String Query = "SELECT "+colNames+" FROM Patient WHERE Nom = '" + identifiant + "'";
                Statement s = conn.createStatement();
                
                s.executeQuery(Query);
                ResultSet rs = s.getResultSet();
                rs.first();
                                
                ArrayList<String> attributes = new ArrayList<>();
                for(int i=0; i < patientCol.size(); i++)
                {
                    attributes.add(rs.getString(patientCol.get(i)));
                }
                rs.close();
                
                request.setAttribute("Patient_column_list", patientCol);
                request.setAttribute("Patient_attributes_list",attributes );
                
                System.out.println("Query 1 réussi");
            /*-----------------------FIN--------------------------------------*/
                
                ArrayList<String> EtudeCol  = this.getColumnNames("Etude", conn);

                String colNamesEtude="";
                for(int i =0 ; i< EtudeCol.size() ; i++)
                {
                    if(i+1<EtudeCol.size())
                        colNamesEtude = colNamesEtude + EtudeCol.get(i)+",";
                    else
                        colNamesEtude = colNamesEtude + EtudeCol.get(i);
                }
                String Query2 = "SELECT "+colNamesEtude+" FROM Etude WHERE Patient_idPatient = " + attributes.get(0); //id
                System.out.println(Query2);
                
                s.executeQuery(Query2);
                ResultSet rs2 = s.getResultSet();
                rs2.first();
                
                System.out.println("Query 2 en cours");

                ArrayList<String> attributes_etude = new ArrayList<>();
                for(int i=0; i < EtudeCol.size(); i++)
                {
                    attributes_etude.add(rs2.getString(EtudeCol.get(i)));
                }
                rs2.close();
                
                request.setAttribute("Etude_column_list", EtudeCol);
                request.setAttribute("Etude_attributes_list",attributes_etude );
                
                System.out.println("Query 2 réussi");
                
                ArrayList<String> SerieCol  = this.getColumnNames("Serie", conn);
                
                String colNamesSerie="";
                for(int i =0 ; i< SerieCol.size() ; i++)
                {
                    if(i+1<SerieCol.size())
                        colNamesSerie = colNamesSerie + SerieCol.get(i)+",";
                    else
                        colNamesSerie = colNamesSerie + SerieCol.get(i);
                }
                String Query3 = "SELECT "+colNamesSerie+" FROM Serie WHERE Etude_idEtude = " + attributes_etude.get(0); //id
                System.out.println(Query3);
                
                s.executeQuery(Query3);
                ResultSet rs3 = s.getResultSet();
                rs3.first();
                
                System.out.println("Query 3 en cours");
                
                ArrayList<String> attributes_serie = new ArrayList<>();
                for(int i=0; i < SerieCol.size(); i++)
                {
                    attributes_serie.add(rs3.getString(SerieCol.get(i)));
                }
                rs3.close();
                
                request.setAttribute("Serie_column_list", SerieCol);
                request.setAttribute("Serie_attributes_list",attributes_serie );
              
                System.out.println("Query 3 réussi");
                
                String Query4 = "SELECT info_im FROM CamImage WHERE Serie_idSerie = " + attributes_serie.get(0);
                
                s.executeQuery(Query4);
                ResultSet rs4 = s.getResultSet();
                
                System.out.println("Query 4 en cours");
                
                ArrayList<String> im_url = new ArrayList<String>();
                
                while(rs4.next())
                {
                     im_url.add(rs4.getString("info_im"));              
                }
                
                request.setAttribute("Im_url", im_url);
                
                System.out.println("Query 4 réussi");
                
                               
            }
            
            catch (java.sql.SQLException sql)
            {
                System.out.println("Pas ds la database");
            }
        } 
        catch (SQLException ex) 
        {
            System.out.println("Echec ommunication avec la DB");

        }
    }
    
    protected ArrayList<String> getColumnNames(String Table, Connection conn) 
    {
            ArrayList<String> columns  = new ArrayList<>();

            try{
            
            Statement sCol = conn.createStatement();
            
            String colQuery = "SELECT column_name FROM information_schema.columns WHERE table_name='"+Table+"'";
            
            sCol.executeQuery(colQuery);
            
            ResultSet rsCol = sCol.getResultSet();
            
            for(int i=0;rsCol.next();i++)
            {
                if(!rsCol.getString("column_name").contains("_id"))
                 columns.add(rsCol.getString("column_name"));              
            }
            sCol.close();
            rsCol.close();
            }
            catch(Exception e)
            {
                System.out.println("Echec de l'extraction des colonnes de la table " + Table);
            }
            return columns;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {

        this.processRequest(request, response);
        this.getServletContext().getRequestDispatcher("/WEB-INF/Viewjsp.jsp").forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
