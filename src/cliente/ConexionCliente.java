package cliente;

import common.Protocolo;
import java.net.*;
import java.io.*;
import common.TiposMensaje;

public class ConexionCliente {

    private String nombreCliente;
    private int protocolo; // Protocolo.TCP o Protocolo.UDP

    // Variables para UDP
    private DatagramSocket socketUDP;
    private InetAddress direccionServidor;
    private int puertoUDP = 12346;

    // Variables para TCP
    private Socket socketTCP;
    private BufferedReader entradaTCP;
    private PrintWriter salidaTCP;
    private int puertoTCP = 12345;

    // Constructor
    public ConexionCliente(String nombreCliente, int protocolo, String ipServidor) {
        try {
            this.nombreCliente = nombreCliente;
            this.protocolo = protocolo;

            if (protocolo == Protocolo.UDP) {
                iniciarUDP(ipServidor);
            } else if (protocolo == Protocolo.TCP) {
                iniciarTCP(ipServidor);
            } else {
                System.out.println("Protocolo no soportado");
            }

        } catch (Exception e) {
            System.out.println("Error al iniciar cliente: " + e.getMessage());
        }
    }

    // -------------------
    // MÉTODOS UDP
    // -------------------
    private void iniciarUDP(String ipServidor) throws Exception {
        socketUDP = new DatagramSocket();
        direccionServidor = InetAddress.getByName(ipServidor);

        // Enviar mensaje de conexión
        enviarMensaje(Protocolo.CONEXION + "|" + nombreCliente);

        // Hilo para escuchar mensajes UDP
        new Thread(this::escucharUDP).start();
        System.out.println("Cliente UDP conectado como: " + nombreCliente);
    }

    private void escucharUDP() {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                socketUDP.receive(paquete);
                String mensaje = new String(paquete.getData(), 0, paquete.getLength());
                TiposMensaje.procesarMensaje(mensaje);
            }
        } catch (IOException e) {
            System.out.println("Error UDP: " + e.getMessage());
        }
    }

    // -------------------
    // MÉTODOS TCP
    // -------------------
    private void iniciarTCP(String ipServidor) throws Exception {
        socketTCP = new Socket(ipServidor, puertoTCP);
        entradaTCP = new BufferedReader(new InputStreamReader(socketTCP.getInputStream()));
        salidaTCP = new PrintWriter(socketTCP.getOutputStream(), true);

        // Enviar mensaje de conexión
        enviarMensaje(Protocolo.CONEXION + "|" + nombreCliente);

        // Hilo para escuchar mensajes TCP
        new Thread(this::escucharTCP).start();
        System.out.println("Cliente TCP conectado como: " + nombreCliente);
    }

    private void escucharTCP() {
        try {
            String mensaje;
            while ((mensaje = entradaTCP.readLine()) != null) {
                TiposMensaje.procesarMensaje(mensaje);
            }
        } catch (IOException e) {
            System.out.println("Error TCP: " + e.getMessage());
        }
    }

    // -------------------
    // ENVÍO DE MENSAJES
    // -------------------
    public void enviarMensaje(String mensaje) {
        try {
            if (protocolo == Protocolo.UDP) {
                byte[] datos = mensaje.getBytes();
                DatagramPacket paquete = new DatagramPacket(datos, datos.length, direccionServidor, puertoUDP);
                socketUDP.send(paquete);
            } else if (protocolo == Protocolo.TCP) {
                salidaTCP.println(mensaje);
            }
        } catch (IOException e) {
            System.out.println("Error al enviar mensaje: " + e.getMessage());
        }
    }

    // -------------------
    // MAIN DE PRUEBA
    // -------------------
    public static void main(String[] args) {
        String ip = args.length > 0 ? args[0] : "localhost";

        ConexionCliente clienteUDP = new ConexionCliente("MarioUDP", Protocolo.UDP, ip);
        ConexionCliente clienteTCP = new ConexionCliente("MarioTCP", Protocolo.TCP, ip);

        clienteUDP.enviarMensaje(Protocolo.BROADCAST + "|Hola a todos desde UDP!");
        clienteTCP.enviarMensaje(Protocolo.UNICAST + "|MarioUDP|Hola desde TCP a UDP!");
    }
}
