package common;

/**
 * PROTOCOLO DE COMUNICACION - SISTEMA DE CHAT CON SOCKETS
 * 
 * Esta clase define las constantes y reglas de comunicacion entre cliente y servidor.
 * Contiene los tipos de protocolo, mensajes y metodos de envio requeridos en la Actividad 9.
 * 
 * Funcionalidades:
 * - Definir protocolos de transporte (TCP/UDP)
 * - Tipos de mensajes del sistema
 * - Modos de envio (Unicast, Broadcast, Multicast, Anycast)
 * - Validaciones y utilidades para la comunicacion
 * 
 * @author Angel
 * @version 1.0
 */
public class Protocolo {
    
    // =============================================
    // PROTOCOLOS DE TRANSPORTE SOPORTADOS
    // =============================================
    
    /**
     * Protocolo TCP - Orientado a conexion
     * Caracteristicas: Confiable, en orden, con control de flujo
     */
    public static final int TCP = 1;
    
    /**
     * Protocolo UDP - No orientado a conexion  
     * Caracteristicas: Rapido, sin garantias de entrega
     */
    public static final int UDP = 2;
    
    // =============================================
    // TIPOS DE MENSAJES DEL SISTEMA
    // =============================================
    
    /**
     * Mensaje de conexion de un nuevo cliente
     */
    public static final int CONEXION = 100;
    
    /**
     * Mensaje de desconexion de un cliente
     */
    public static final int DESCONEXION = 101;
    
    /**
     * Mensaje de texto normal entre clientes
     */
    public static final int MENSAJE_TEXTO = 102;
    
    /**
     * Mensaje que contiene un archivo
     */
    public static final int MENSAJE_ARCHIVO = 103;
    
    /**
     * Solicitud de lista de clientes conectados
     */
    public static final int LISTA_CLIENTES = 104;
    
    // =============================================
    // TIPOS DE ENVIO (REQUERIDOS EN ACTIVIDAD 9)
    // =============================================
    
    /**
     * UNICAST: Mensaje dirigido a un cliente especifico
     * Uso: Comunicacion privada entre dos usuarios
     */
    public static final int UNICAST = 200;
    
    /**
     * BROADCAST: Mensaje enviado a todos los clientes conectados
     * Uso: Anuncios generales o mensajes publicos
     */
    public static final int BROADCAST = 201;
    
    /**
     * MULTICAST: Mensaje enviado a un grupo especifico de clientes
     * Uso: Salas de chat o grupos tematicos
     */
    public static final int MULTICAST = 202;
    
    /**
     * ANYCAST: Mensaje enviado a cualquier cliente disponible
     * Uso: Busqueda de servicios o recursos distribuidos
     */
    public static final int ANYCAST = 203;
    
    // =============================================
    // ESTADOS DE CONEXION
    // =============================================
    
    public static final int CONECTADO = 300;
    public static final int DESCONECTADO = 301;
    
    // =============================================
    // METODOS DE VALIDACION
    // =============================================
    
    /**
     * Valida si un numero representa un protocolo de transporte soportado
     * 
     * @param protocolo Numero del protocolo a validar
     * @return true si el protocolo es TCP o UDP, false en caso contrario
     */
    public static boolean esProtocoloSoportado(int protocolo) {
        return protocolo == TCP || protocolo == UDP;
    }
    
    /**
     * Valida si un tipo de envio es valido segun los definidos en la actividad
     * 
     * @param tipoEnvio Tipo de envio a validar
     * @return true si es un tipo de envio valido (200-203), false en caso contrario
     */
    public static boolean esTipoEnvioValido(int tipoEnvio) {
        return tipoEnvio >= UNICAST && tipoEnvio <= ANYCAST;
    }
    
    /**
     * Convierte un tipo de envio numerico a su representacion en texto
     * 
     * @param tipoEnvio Numero del tipo de envio
     * @return Cadena descriptiva del tipo de envio
     */
    public static String getDescripcionTipoEnvio(int tipoEnvio) {
        switch(tipoEnvio) {
            case UNICAST: return "UNICAST";
            case BROADCAST: return "BROADCAST";
            case MULTICAST: return "MULTICAST";
            case ANYCAST: return "ANYCAST";
            default: return "DESCONOCIDO";
        }
    }
    
    /**
     * Convierte un protocolo numerico a su representacion en texto
     * 
     * @param protocolo Numero del protocolo
     * @return Cadena descriptiva del protocolo
     */
    public static String getDescripcionProtocolo(int protocolo) {
        switch(protocolo) {
            case TCP: return "TCP";
            case UDP: return "UDP";
            default: return "DESCONOCIDO";
        }
    }
    
    /**
     * Genera un identificador unico para un cliente basado en su IP, puerto y timestamp
     * Formato: IP:PUERTO:TIMESTAMP
     * 
     * @param ip Direccion IP del cliente
     * @param puerto Puerto de conexion del cliente
     * @return Identificador unico del cliente
     */
    public static String generarIdCliente(String ip, int puerto) {
        return ip + ":" + puerto + ":" + System.currentTimeMillis();
    }
    
    /**
     * Parsea un identificador de cliente para extraer IP y puerto
     * 
     * @param idCliente Identificador en formato IP:PUERTO:TIMESTAMP
     * @return Arreglo con [IP, Puerto, Timestamp]
     */
    public static String[] parsearIdCliente(String idCliente) {
        return idCliente.split(":");
    }
}