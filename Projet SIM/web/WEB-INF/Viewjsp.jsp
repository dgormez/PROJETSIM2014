<%-- 
    Document   : Viewjsp
    Created on : 17 mai 2014, 16:04:49
    Author     : Ko
--%>

<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="Formcss.css" />

        <title>Dossier</title>        
    </head>
    <body >
        <div class="basic-grey">
        <%
            //Arraylists contenant l'intitulé et la valeur des colonnes de la table PATIENT
            ArrayList<String> patient_col = (ArrayList) request.getAttribute("Patient_column_list");
            ArrayList<String> patient_attributes = (ArrayList) request.getAttribute("Patient_attributes_list");

            //Arraylists contenant l'intitulé et la valeur des colonnes de la table ETUDE
            ArrayList<String> etude_col = (ArrayList) request.getAttribute("Etude_column_list");
            ArrayList<String> etude_attributes = (ArrayList) request.getAttribute("Etude_attributes_list");            
            
            //Arraylists contenant l'intitulé et la valeur des colonnes de la table SERIE
            ArrayList<String> serie_col = (ArrayList) request.getAttribute("Serie_column_list");
            ArrayList<String> serie_attributes = (ArrayList) request.getAttribute("Serie_attributes_list");    
            
            
            ArrayList<String> im_url = (ArrayList) request.getAttribute("Im_url");
            System.out.println(im_url);
            
            
            for(int j=1; j<patient_attributes.size() ; j++)
            {    
                %>
            <%=patient_col.get(j)%> : <%=patient_attributes.get(j)%><br>
            
            <%
            }%>
            <br>
            <%for(int j=1; j<etude_attributes.size() ; j++)
            {    
                %>
            <%=etude_col.get(j)%> : <%=etude_attributes.get(j)%><br>
            
            <%
            }%>

            <br>

            <%for(int j=1; j<serie_attributes.size() ; j++)
            {    
                %>
            <%=serie_col.get(j)%> : <%=serie_attributes.get(j)%><br>
            
            <%
            }%>
            <form method="get" action="ImageServlet">
                <select name="image_list" size="5" >
                    
            <% for(int i = 0 ; i < im_url.size() ; i++)
            {
                %>
                <option value="<%=im_url.get(i)%>"><%=im_url.get(i)%> </option>
                <%
            } %>
                   
                </select>
            <label>
                <span>&nbsp;</span> 
                <input type="submit" class="button" value="Valider" /> 
            </label>  
            </form>
            
        <footer id="footer">
            Développé par Botquin Y., De Almeida L., Gerard M., Gormerz D.<br>
            Dans le cadre du cours de "Systèmes d'information médicale".<br>
            LISA-ULB MAI 2014
        </footer>
    </body>
</html>
