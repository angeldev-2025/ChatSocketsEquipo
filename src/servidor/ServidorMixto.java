package servidor;

import common.Protocolo;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.SwingUtilities;

/**
 * SERVIDOR MIXTO - SISTEMA DE CHAT CON SOCKETS TCP Y UDP
 * 
 * Este servidor maneja conexiones simultaneas de clientes usando ambos protocolos:
 * - TCP: Conexiones confiables y orientadas a conexion
 * - UDP: Conexiones rapidas y no orientadas a conexion
 * 
 * Caracteristicas principales:
 * - Escucha en multiples puertos simultaneamente
 * - Gestiona clientes concurrentes con hilos separados
 * - Consola de administracion integrada
 * - Registro de clientes TCP y UDP por separado
 * - Sistema de mensajeria entre clientes (BROADCAST, UNICAST, etc.)
 * 
 */
public class ServidorMixto {
    
    // =============================================
    // CONFIGURACION DEL SERVIDOR
    // =============================================
    
    /**
     * Puerto para conexiones TCP (orientadas a conexion)
     * Los clientes TCP establecen una conexion persistente
     */
    private static final int PUERTO_TCP = 12345;
    
    /**
     * Puerto para conexiones UDP (no orientadas a conexion)
     * Los clientes UDP envian datagramas independientes
     */
    private static final int PUERTO_UDP = 12346;
    
    // =============================================
    // ESTRUCTURAS DE DATOS PARA GESTION DE CLIENTES
    // =============================================
    
    /**
     * Lista de identificadores de clientes TCP conectados
     * Lista sincronizada para acceso seguro desde multiples hilos
     */
    private static List<String> clientesTCP = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * Lista de identificadores de clientes UDP registrados
     * Los clientes UDP no mantienen conexion persistente
     */
    private static List<String> clientesUDP = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * Mapa de manejadores de clientes activos (solo TCP)
     * Permite encontrar manejadores especificos para enviar mensajes
     */
    private static Map<String, ManejadorClientes> manejadoresActivos = Collections.synchronizedMap(new HashMap<>());
    
    /**
     * Bandera que controla el estado del servidor
     * false = servidor detenido, true = servidor activo
     */
    private static boolean servidorActivo = true;

    // =============================================
    // METODO PRINCIPAL
    // =============================================
    
    /**
     * Punto de entrada del servidor mixto
     * Inicia los servidores TCP y UDP en hilos separados
     * y lanza la consola de administracion
     * 
     * @param args Argumentos de linea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        System.out.println("INICIANDO SERVIDOR MIXTO (TCP/UDP)");
        System.out.println("Puerto TCP: " + PUERTO_TCP);
        System.out.println("Puerto UDP: " + PUERTO_UDP);
        System.out.println("Equipo: Alin, Abril, Mario, Angel");
        
        // Iniciar interfaz grafica del servidor
        SwingUtilities.invokeLater(() -> new GUIservidor());
        
        // Iniciar servidor TCP en un hilo separado
        Thread hiloTCP = new Thread(() -> iniciarServidorTCP());
        hiloTCP.start();
        
        // Iniciar servidor UDP en un hilo separado
        Thread hiloUDP = new Thread(() -> iniciarServidorUDP());
        hiloUDP.start();
        
        // Iniciar consola de administracion en el hilo principal
        iniciarConsolaAdministracion();
    }
    
    
    
    // =============================================
    // SERVIDOR TCP - ORIENTADO A CONEXION
    // =============================================
    
    /**
     * Inicia y gestiona el servidor TCP
     * - Crea un ServerSocket en el puerto configurado
     * - Acepta conexiones entrantes de clientes TCP
     * - Crea un ManejadorClientes para cada conexion
     * - Ejecuta cada manejador en un hilo separado
     */
    private static void iniciarServidorTCP() {
        try (ServerSocket servidorTCP = new ServerSocket(PUERTO_TCP)) {
            System.out.println("Servidor TCP escuchando en puerto " + PUERTO_TCP);
            
            // Bucle principal del servidor TCP
            while (servidorActivo) {
                try {
                    // Esperar y aceptar una nueva conexion TCP
                    Socket clienteSocket = servidorTCP.accept();
                    
                    // Obtener informacion del cliente conectado
                    String ipCliente = clienteSocket.getInetAddress().getHostAddress();
                    int puertoCliente = clienteSocket.getPort();
                    String idCliente = Protocolo.generarIdCliente(ipCliente, puertoCliente);
                    
                    System.out.println("Nuevo cliente TCP conectado: " + idCliente);
                    
                    // Registrar cliente en la lista de TCP
                    clientesTCP.add(idCliente);
                    
                    // Crear y ejecutar manejador para este cliente
                    ManejadorClientes manejador = new ManejadorClientes(clienteSocket, idCliente, Protocolo.TCP);
                    
                    // Registrar manejador en el mapa de activos
                    manejadoresActivos.put(idCliente, manejador);
                    
                    Thread hiloCliente = new Thread(manejador);
                    hiloCliente.start();
                    
                } catch (IOException e) {
                    // Manejar errores de aceptacion de conexiones
                    if (servidorActivo) {
                        System.err.println("Error aceptando conexion TCP: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error iniciando servidor TCP: " + e.getMessage());
        }
    }
    
    // =============================================
    // SERVIDOR UDP - NO ORIENTADO A CONEXION
    // =============================================
    
    /**
     * Inicia y gestiona el servidor UDP
     * - Crea un DatagramSocket en el puerto configurado
     * - Recibe datagramas de clientes UDP
     * - Procesa cada paquete recibido
     */
    private static void iniciarServidorUDP() {
        try (DatagramSocket servidorUDP = new DatagramSocket(PUERTO_UDP)) {
            System.out.println("Servidor UDP escuchando en puerto " + PUERTO_UDP);
            
            // Buffer para recibir datos UDP
            byte[] buffer = new byte[1024];
            
            // Bucle principal del servidor UDP
            while (servidorActivo) {
                try {
                    // Crear paquete para recibir datos
                    DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                    
                    // Esperar y recibir un paquete UDP
                    servidorUDP.receive(paquete);
                    
                    // Procesar el paquete recibido
                    procesarPaqueteUDP(paquete, servidorUDP);
                    
                } catch (IOException e) {
                    // Manejar errores de recepcion UDP
                    if (servidorActivo) {
                        System.err.println("Error recibiendo paquete UDP: " + e.getMessage());
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Error iniciando servidor UDP: " + e.getMessage());
        }
    }
    
    /**
     * Procesa un paquete UDP recibido
     * - Extrae informacion del cliente (IP y puerto)
     * - Registra al cliente si es nuevo
     * - Procesa el mensaje contenido en el paquete
     * 
     * @param paquete Paquete UDP recibido
     * @param servidorUDP Socket UDP del servidor para posibles respuestas
     */
    private static void procesarPaqueteUDP(DatagramPacket paquete, DatagramSocket servidorUDP) {
        // Extraer informacion del remitente
        String ipCliente = paquete.getAddress().getHostAddress();
        int puertoCliente = paquete.getPort();
        String idCliente = Protocolo.generarIdCliente(ipCliente, puertoCliente);
        
        // Registrar cliente UDP si es nuevo
        if (!clientesUDP.contains(idCliente)) {
            clientesUDP.add(idCliente);
            System.out.println("Nuevo cliente UDP registrado: " + idCliente);
        }
        
        // Convertir datos del paquete a String
        String mensaje = new String(paquete.getData(), 0, paquete.getLength());
        System.out.println("Mensaje UDP de " + idCliente + ": " + mensaje);
        
   
        // Esto se expandira cuando se implemente el manejo completo de mensajes
    }
    
    // =============================================
    // METODOS PARA COMUNICACION ENTRE CLIENTES
    // =============================================
    
    /**
     * Envia un mensaje a todos los clientes TCP conectados (BROADCAST)
     * 
     * @param mensaje Mensaje a enviar a todos los clientes
     * @param remitente ID del cliente que envia el mensaje (para exclusion opcional)
     */
    public static void broadcastMensaje(String mensaje, String remitente) {
        System.out.println("BROADCAST de " + remitente + ": " + mensaje);
        
        // Enviar mensaje a todos los clientes TCP conectados
        synchronized (manejadoresActivos) {
            for (Map.Entry<String, ManejadorClientes> entry : manejadoresActivos.entrySet()) {
                String idCliente = entry.getKey();
                ManejadorClientes manejador = entry.getValue();
                
                // Opcional: excluir al remitente del broadcast
                if (!idCliente.equals(remitente) && manejador.estaActivo()) {
                    manejador.enviarMensaje("[BROADCAST de " + remitente + "] " + mensaje);
                }
            }
        }
    }
    
    /**
     * Envia un mensaje a un cliente TCP especifico (UNICAST)
     * 
     * @param mensaje Mensaje a enviar
     * @param destino ID del cliente destino
     * @param remitente ID del cliente remitente
     * @return true si el mensaje se envio exitosamente, false si el destino no existe
     */
    public static boolean unicastMensaje(String mensaje, String destino, String remitente) {
        System.out.println("UNICAST de " + remitente + " a " + destino + ": " + mensaje);
        
        ManejadorClientes manejadorDestino = manejadoresActivos.get(destino);
        
        if (manejadorDestino != null && manejadorDestino.estaActivo()) {
            manejadorDestino.enviarMensaje("[PRIVADO de " + remitente + "] " + mensaje);
            return true;
        } else {
            System.out.println("Cliente destino no encontrado o inactivo: " + destino);
            return false;
        }
    }
    
    /**
     * Envia un mensaje a cualquier cliente TCP disponible (ANYCAST)
     * 
     * @param mensaje Mensaje a enviar
     * @param remitente ID del cliente remitente
     * @return ID del cliente que recibio el mensaje, o null si no hay clientes disponibles
     */
    public static String anycastMensaje(String mensaje, String remitente) {
        System.out.println("ANYCAST de " + remitente + ": " + mensaje);
        
        synchronized (manejadoresActivos) {
            for (Map.Entry<String, ManejadorClientes> entry : manejadoresActivos.entrySet()) {
                String idCliente = entry.getKey();
                ManejadorClientes manejador = entry.getValue();
                
                // Enviar al primer cliente disponible (excluyendo al remitente)
                if (!idCliente.equals(remitente) && manejador.estaActivo()) {
                    manejador.enviarMensaje("[ANYCAST de " + remitente + "] " + mensaje);
                    return idCliente;
                }
            }
        }
        
        System.out.println("No hay clientes disponibles para ANYCAST");
        return null;
    }
    
    /**
     * Remueve un manejador de cliente de la lista de activos
     * Se llama cuando un cliente se desconecta
     * 
     * @param idCliente ID del cliente a remover
     */
    public static void removerManejador(String idCliente) {
        manejadoresActivos.remove(idCliente);
        clientesTCP.remove(idCliente);
        System.out.println("Manejador removido para cliente: " + idCliente);
    }
    
    // =============================================
    // METODOS DE ACCESO PARA OTROS COMPONENTES
    // =============================================
    
    /**
     * Obtiene la lista de clientes TCP conectados
     * 
     * @return Lista de IDs de clientes TCP
     */
    public static List<String> getClientesTCP() {
        return new ArrayList<>(clientesTCP);
    }
    
    /**
     * Obtiene la lista de clientes UDP registrados
     * 
     * @return Lista de IDs de clientes UDP
     */
    public static List<String> getClientesUDP() {
        return new ArrayList<>(clientesUDP);
    }
    
    /**
     * Obtiene el mapa de manejadores activos
     * 
     * @return Mapa de manejadores de clientes activos
     */
    public static Map<String, ManejadorClientes> getManejadoresActivos() {
        return new HashMap<>(manejadoresActivos);
    }
    
    // =============================================
    // CONSOLA DE ADMINISTRACION
    // =============================================
    
    /**
     * Inicia la consola de administracion del servidor
     * Permite ejecutar comandos mientras el servidor esta en ejecucion
     * Comandos disponibles: clientes, estado, salir
     */
    private static void iniciarConsolaAdministracion() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\nConsola de administracion - Comandos: 'clientes', 'estado', 'salir'");
        
        // Bucle de lectura de comandos
        while (servidorActivo) {
            System.out.print("\nComando > ");
            String comando = scanner.nextLine().trim().toLowerCase();
            
            // Procesar comando ingresado
            switch (comando) {
                case "clientes":
                    mostrarClientesConectados();
                    break;
                case "estado":
                    mostrarEstadoServidor();
                    break;
                case "salir":
                    System.out.println("Cerrando servidor...");
                    servidorActivo = false;
                    break;
                default:
                    System.out.println("Comando no reconocido. Use: clientes, estado, salir");
            }
        }
        
        // Liberar recursos al salir
        scanner.close();
        System.exit(0);
    }
    
    /**
     * Muestra la lista de clientes conectados al servidor
     * Separa clientes TCP y UDP con sus respectivos contadores
     */
    private static void mostrarClientesConectados() {
        System.out.println("\nCLIENTES CONECTADOS:");
        System.out.println("Clientes TCP (" + clientesTCP.size() + "): " + clientesTCP);
        System.out.println("Clientes UDP (" + clientesUDP.size() + "): " + clientesUDP);
        System.out.println("Manejadores activos: " + manejadoresActivos.size());
    }
    
    /**
     * Muestra el estado actual del servidor
     * Incluye informacion de configuracion y metricas
     */
    private static void mostrarEstadoServidor() {
        System.out.println("\nESTADO DEL SERVIDOR:");
        System.out.println("Servidor activo: " + servidorActivo);
        System.out.println("Puerto TCP: " + PUERTO_TCP);
        System.out.println("Puerto UDP: " + PUERTO_UDP);
        System.out.println("Total clientes: " + (clientesTCP.size() + clientesUDP.size()));
        System.out.println("Manejadores activos: " + manejadoresActivos.size());
    }
    
}