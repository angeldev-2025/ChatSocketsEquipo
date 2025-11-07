package servidor;

import common.Protocolo;
import java.io.*;
import java.net.*;

/**
 * MANEJADOR DE CLIENTES - GESTION DE CONEXIONES INDIVIDUALES
 * 
 * Esta clase se encarga de manejar la comunicacion con un cliente especifico.
 * Se ejecuta en un hilo separado para cada cliente, permitiendo multiples
 * conexiones simultaneas.
 * 
 * Responsabilidades:
 * - Establecer flujos de entrada/salida con el cliente
 * - Recibir y procesar mensajes del cliente
 * - Enviar respuestas y mensajes al cliente
 * - Gestionar la desconexion y liberacion de recursos
 * 
 * @author Angel  
 * @version 1.0
 */
public class ManejadorClientes implements Runnable {
    
    // =============================================
    // ATRIBUTOS DE LA CONEXION
    // =============================================
    
    /**
     * Socket de conexion con el cliente (solo para TCP)
     * Para UDP, este valor sera null
     */
    private Socket clienteSocket;
    
    /**
     * Identificador unico del cliente
     * Generado con IP:PUERTO:TIMESTAMP
     */
    private String idCliente;
    
    /**
     * Tipo de protocolo usado por el cliente
     * Valores: Protocolo.TCP o Protocolo.UDP
     */
    private int tipoProtocolo;
    
    /**
     * Bandera que indica si el manejador esta activo
     * false = conexion cerrada, true = conexion activa
     */
    private boolean activo = true;
    
    // =============================================
    // FLUJOS DE COMUNICACION (SOLO TCP)
    // =============================================
    
    /**
     * Lector para recibir mensajes del cliente
     * null para conexiones UDP
     */
    private BufferedReader entrada;
    
    /**
     * Escritor para enviar mensajes al cliente
     * null para conexiones UDP
     */
    private PrintWriter salida;
    
    // =============================================
    // CONSTRUCTOR
    // =============================================
    
    /**
     * Constructor para clientes TCP
     * Inicializa los flujos de entrada/salida y configura la conexion
     * 
     * @param socket Socket de conexion con el cliente
     * @param idCliente Identificador unico del cliente
     * @param tipoProtocolo Tipo de protocolo (debe ser Protocolo.TCP)
     */
    public ManejadorClientes(Socket socket, String idCliente, int tipoProtocolo) {
        this.clienteSocket = socket;
        this.idCliente = idCliente;
        this.tipoProtocolo = tipoProtocolo;
        
        try {
            // Configurar flujos de entrada/salida solo para TCP
            this.entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
            this.salida = new PrintWriter(clienteSocket.getOutputStream(), true);
            
        } catch (IOException e) {
            System.err.println("Error configurando flujos para cliente: " + idCliente);
            this.activo = false;
        }
    }
    
    // =============================================
    // METODO PRINCIPAL DEL HILO
    // =============================================
    
    /**
     * Metodo principal que se ejecuta cuando se inicia el hilo
     * Contiene el bucle principal de recepcion de mensajes
     * Solo funciona para clientes TCP
     */
    @Override
    public void run() {
        System.out.println("Iniciando manejador para cliente: " + idCliente + 
                          " (" + Protocolo.getDescripcionProtocolo(tipoProtocolo) + ")");
        
        // Enviar mensaje de bienvenida al cliente
        enviarMensaje("Bienvenido al servidor! Tu ID: " + idCliente);
        
        // Bucle principal de recepcion de mensajes (solo para TCP)
        while (activo && tipoProtocolo == Protocolo.TCP) {
            try {
                // Leer mensaje del cliente
                String mensaje = entrada.readLine();
                
                // Si mensaje es null, cliente se desconecto
                if (mensaje == null) {
                    System.out.println("Cliente desconectado: " + idCliente);
                    activo = false;
                    break;
                }
                
                System.out.println("Mensaje de " + idCliente + ": " + mensaje);
                
                // Procesar el mensaje recibido
                procesarMensaje(mensaje);
                
            } catch (IOException e) {
                // Manejar errores de lectura
                if (activo) {
                    System.err.println("Error leyendo mensaje de " + idCliente + ": " + e.getMessage());
                }
                activo = false;
            }
        }
        
        // Realizar limpieza al finalizar
        cerrarConexion();
    }
    
    // =============================================
    // PROCESAMIENTO DE MENSAJES
    // =============================================
    
    /**
     * Procesa un mensaje recibido del cliente
     * Actualmente implementa un echo simple
     * En el futuro manejara los diferentes tipos de envio:
     * - UNICAST: Mensaje a cliente especifico
     * - BROADCAST: Mensaje a todos los clientes  
     * - MULTICAST: Mensaje a grupo de clientes
     * - ANYCAST: Mensaje a cualquier cliente disponible
     * 
     * @param mensaje Mensaje de texto recibido del cliente
     */
    private void procesarMensaje(String mensaje) {
        // TODO: Implementar logica completa de procesamiento de mensajes
        // Esta seccion se expandira para manejar los diferentes tipos de envio
        
        // Por ahora, solo responde con un echo del mensaje
        enviarMensaje("Echo: " + mensaje);
        
        System.out.println("Mensaje procesado de " + idCliente);
    }
    
    // =============================================
    // METODOS DE ENVIO DE MENSAJES
    // =============================================
    
    /**
     * Envia un mensaje al cliente conectado
     * Solo funciona para clientes TCP
     * 
     * @param mensaje Mensaje a enviar al cliente
     */
    public void enviarMensaje(String mensaje) {
        if (salida != null && activo) {
            salida.println(mensaje);
        }
    }
    
    // =============================================
    // METODOS DE LIMPIEZA Y CIERRE
    // =============================================
    
    /**
     * Cierra la conexion con el cliente y libera todos los recursos
     * Se ejecuta automaticamente cuando el cliente se desconecta
     */
    private void cerrarConexion() {
        activo = false;
        
        try {
            // Cerrar flujos en orden inverso al de creacion
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (clienteSocket != null) clienteSocket.close();
            
            System.out.println("Conexion cerrada para: " + idCliente);
            
        } catch (IOException e) {
            System.err.println("Error cerrando conexion de " + idCliente);
        }
    }
    
    // =============================================
    // METODOS DE ACCESO (GETTERS)
    // =============================================
    
    /**
     * Obtiene el identificador unico del cliente
     * 
     * @return Identificador del cliente
     */
    public String getIdCliente() {
        return idCliente;
    }
    
    /**
     * Obtiene el tipo de protocolo usado por el cliente
     * 
     * @return Protocolo.TCP o Protocolo.UDP
     */
    public int getTipoProtocolo() {
        return tipoProtocolo;
    }
    
    /**
     * Verifica si el manejador esta activo y la conexion esta abierta
     * 
     * @return true si la conexion esta activa, false si esta cerrada
     */
    public boolean estaActivo() {
        return activo;
    }
}