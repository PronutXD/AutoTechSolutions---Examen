/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao;

/**
 *
 * @author Nayeli Daniela
 */
import Conexion.Conexion;
import modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClienteDao {

    public boolean insertar(Cliente cliente) {
        String sql = "INSERT INTO clientes (documento, nombres, apellidos, telefono, correo, estado) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, cliente.getDocumento());
            pst.setString(2, cliente.getNombres());
            pst.setString(3, cliente.getApellidos());
            pst.setString(4, cliente.getTelefono());
            pst.setString(5, cliente.getCorreo());
            pst.setString(6, cliente.getEstado());
            
            pst.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.out.println("Error al insertar cliente en la BD: " + e.getMessage());
            return false;
        }
    }
}