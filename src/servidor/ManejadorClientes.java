package servidor;

import common.Protocolo;
import java.io.*;
import java.net.*;
import java.util.List;

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
 * - Interpretar y ejecutar diferentes tipos de envio de mensajes
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
        enviarMensaje("Comandos disponibles:");
        enviarMensaje("  BROADCAST:mensaje  -> Enviar a todos");
        enviarMensaje("  UNICAST:destino:mensaje -> Mensaje privado");
        enviarMensaje("  ANYCAST:mensaje    -> Enviar a cualquier cliente");
        enviarMensaje("  LISTA               -> Ver clientes conectados");
        
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
     * Procesa un mensaje recibido del cliente e interpreta el tipo de envio
     * Soporta los siguientes formatos:
     * - BROADCAST:mensaje -> Envia a todos los clientes
     * - UNICAST:destino:mensaje -> Envia a cliente especifico
     * - ANYCAST:mensaje -> Envia a cualquier cliente disponible
     * - LISTA -> Devuelve lista de clientes conectados
     * - mensaje normal -> Echo simple (comportamiento por defecto)
     * 
     * @param mensaje Mensaje de texto recibido del cliente
     */
    private void procesarMensaje(String mensaje) {
        // Verificar si es un comando especial
        if (mensaje.toUpperCase().startsWith("BROADCAST:")) {
            // Formato: BROADCAST:mensaje
            String contenido = mensaje.substring(10); // Remover "BROADCAST:"
            ServidorMixto.broadcastMensaje(contenido, this.idCliente);
            
        } else if (mensaje.toUpperCase().startsWith("UNICAST:")) {
            // Formato: UNICAST:destino:mensaje
            String[] partes = mensaje.split(":", 3);
            if (partes.length == 3) {
                String destino = partes[1];
                String contenido = partes[2];
                boolean exito = ServidorMixto.unicastMensaje(contenido, destino, this.idCliente);
                
                if (!exito) {
                    enviarMensaje("ERROR: Cliente destino no encontrado: " + destino);
                }
            } else {
                enviarMensaje("ERROR: Formato UNICAST incorrecto. Use: UNICAST:destino:mensaje");
            }
            
        } else if (mensaje.toUpperCase().startsWith("ANYCAST:")) {
            // Formato: ANYCAST:mensaje
            String contenido = mensaje.substring(8); // Remover "ANYCAST:"
            String destino = ServidorMixto.anycastMensaje(contenido, this.idCliente);
            
            if (destino != null) {
                enviarMensaje("ANYCAST enviado a: " + destino);
            } else {
                enviarMensaje("ERROR: No hay clientes disponibles para ANYCAST");
            }
            
        } else if (mensaje.equalsIgnoreCase("LISTA")) {
            // Mostrar lista de clientes conectados
            mostrarListaClientes();
            
        } else {
            // Mensaje normal - comportamiento de echo
            enviarMensaje("Echo: " + mensaje);
            System.out.println("Mensaje procesado de " + idCliente);
        }
    }
    
    /**
     * Envia al cliente la lista de clientes TCP conectados actualmente
     */
    private void mostrarListaClientes() {
        List<String> clientes = ServidorMixto.getClientesTCP();
        enviarMensaje("=== CLIENTES CONECTADOS (" + clientes.size() + ") ===");
        
        for (int i = 0; i < clientes.size(); i++) {
            String cliente = clientes.get(i);
            String indicador = cliente.equals(this.idCliente) ? " (TU)" : "";
            enviarMensaje((i + 1) + ". " + cliente + indicador);
        }
        
        enviarMensaje("=== FIN DE LISTA ===");
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
     * Notifica al servidor para remover este manejador de la lista activa
     */
    private void cerrarConexion() {
        activo = false;
        
        try {
            // Cerrar flujos en orden inverso al de creacion
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (clienteSocket != null) clienteSocket.close();
            
            // Notificar al servidor que este manejador ya no esta activo
            ServidorMixto.removerManejador(this.idCliente);
            
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