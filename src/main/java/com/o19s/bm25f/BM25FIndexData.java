package com.o19s.bm25f;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BM25FIndexData {

    public static class RawDoc {

        public String id;
        public String headline;
        public String description;

        public RawDoc(String id, String headline, String description) {
            this.id = id;
            this.headline = headline;
            this.description = description;
        }        

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(id).append(", ").append(headline).append(", ").append(description);
            return sb.toString();
        }
        
        
    }

    public static class SRQuery {
        public int srId;
        public String problemDetails;
        public String problemDescrptn;

        public SRQuery(int srId, String problemDetails, String problemDescrptn) {
            this.srId = srId;
            this.problemDetails = problemDetails;
            this.problemDescrptn = problemDescrptn;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(srId).append(", ").append(problemDetails).append(", ").append(problemDescrptn);
            return sb.toString();
        }
    }
    
    public static Set<RawDoc> getDocs() {
        Set<RawDoc> docs = new HashSet<>();
        String qry = "SELECT identifier, headline, description FROM full_bugs";
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/test", "colm_mchugh", "Infy123+");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(qry);
            while (rs.next()) {
                docs.add(new RawDoc(rs.getString(1), rs.getString(2), rs.getString(3)));
            }
            connection.close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BM25FIndexData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(BM25FIndexData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return docs;
    }

    public static Set<SRQuery> getQrys() {
        Set<SRQuery> qrs = new HashSet<>();
        String qry = "SELECT input_file, problem_details, problemdescription FROM full_260k";
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/test", "colm_mchugh", "Infy123+");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(qry);
            while (rs.next()) {
                qrs.add(new SRQuery(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
            connection.close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BM25FIndexData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(BM25FIndexData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return qrs;
    }
    
    public boolean testConnection() {
        try {
            // Test connection to the database.
            Class.forName("org.postgresql.Driver");
            Connection connection = null;
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/test", "colm_mchugh", "Infy123+");
            assert connection != null;
            connection.close();
            return true;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BM25FIndexData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(BM25FIndexData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
}
