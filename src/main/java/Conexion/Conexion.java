/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Conexion;

/**
 *
 * @author Nayeli Daniela
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    // Configura tus datos de conexión a SQL Server
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=AutoTechSolutions;encrypt=true;trustServerCertificate=true;";
    private static final String USUARIO = "sa"; 
    private static final String CLAVE = "nayelita"; 

    public static Connection conectar() {
        try {
            Connection con = DriverManager.getConnection(URL, USUARIO, CLAVE);
            return con;
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            return null;
        }
    }

    public static Connection obtenerConexion() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
