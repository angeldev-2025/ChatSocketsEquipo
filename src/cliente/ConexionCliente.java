package cliente;

import common.Protocolo;
import cliente.TiposMensaje;
import java.net.*;
import java.io.*;

/**
 * Clase ConexionCliente (UDP)
 * Maneja la conexión con el servidor usando el protocolo definido en Protocolo.java
 * 
 * Autor: Mario
 */
public class ConexionCliente {

    private DatagramSocket socket;
    private InetAddress direccionServidor;
    private int puertoServidor = 12346; // Puerto UDP
    private String nombreCliente;

    /**
     * Constructor: inicia la conexión UDP con el servidor
     */
    public ConexionCliente(String nombreCliente) {
        try {
            this.nombreCliente = nombreCliente;
            socket = new DatagramSocket();
            direccionServidor = InetAddress.getByName("localhost");

            // ✅ Enviar mensaje de CONEXION usando el protocolo
            enviarMensaje(Protocolo.CONEXION + "|" + nombreCliente);

            System.out.println("Cliente UDP conectado como: " + nombreCliente);

            // Hilo para escuchar
            new Thread(() -> escucharServidor()).start();

        } catch (Exception e) {
            System.out.println("Error al conectar con el servidor UDP: " + e.getMessage());
        }
    }

    /**
     * Envía un mensaje UDP al servidor con formato correcto
     */
    public void enviarMensaje(String mensaje) {
        try {
            byte[] datos = mensaje.getBytes();
            DatagramPacket paquete = new DatagramPacket(datos, datos.length, direccionServidor, puertoServidor);
            socket.send(paquete);
        } catch (IOException e) {
            System.out.println("Error al enviar mensaje UDP: " + e.getMessage());
        }
    }

    /**
     * Escucha mensajes del servidor y los envía a TiposMensaje
     */
    private void escucharServidor() {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                socket.receive(paquete);

                String mensaje = new String(paquete.getData(), 0, paquete.getLength());

                // Procesar mensaje usando la clase TiposMensaje
                TiposMensaje.procesarMensaje(mensaje);
            }

        } catch (IOException e) {
            System.out.println("Error al recibir mensaje UDP: " + e.getMessage());
        }
    }


    // Test
    public static void main(String[] args) {
        ConexionCliente cliente = new ConexionCliente("MarioUDP");

        // Mensaje broadcast
        cliente.enviarMensaje(Protocolo.BROADCAST + "|" + "Hola a todos!");

        // Mensaje unicast → destino|mensaje
        cliente.enviarMensaje(Protocolo.UNICAST + "|" + "Cliente2" + "|" + "Mensaje privado");

        // Anycast
        cliente.enviarMensaje(Protocolo.ANYCAST + "|" + "Mensaje aleatorio");
    }
}

