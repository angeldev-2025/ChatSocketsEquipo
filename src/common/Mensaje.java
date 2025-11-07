package common;

import java.io.Serializable;
import java.util.Date;

/**
 * CLASE MENSAJE - ESTRUCTURA DE DATOS PARA MENSAJES DEL CHAT
 * 
 * Esta clase representa un mensaje en el sistema de chat.
 * Implementa Serializable para permitir la transmision a traves de la red.
 * Contiene toda la informacion necesaria para identificar y procesar un mensaje.
 * 
 * Caracteristicas:
 * - Almacena contenido, remitente, destino y tipo de mensaje
 * - Incluye timestamp para ordenamiento y registro
 * - Soporta todos los tipos de envio definidos en Protocolo
 * - Formato estandarizado para serializacion/deserializacion
 * 
 * @author Alin (Implementado por Angel)
 * @version 1.0
 */
public class Mensaje implements Serializable {
    
    // =============================================
    // CONSTANTES DE SERIALIZACION
    // =============================================
    
    private static final long serialVersionUID = 1L;
    
    // =============================================
    // ATRIBUTOS DEL MENSAJE
    // =============================================
    
    /**
     * Contenido textual del mensaje
     */
    private String contenido;
    
    /**
     * Identificador unico del cliente remitente
     */
    private String remitente;
    
    /**
     * Identificador unico del cliente destino
     * Para BROADCAST puede ser null o "TODOS"
     */
    private String destino;
    
    /**
     * Tipo de mensaje segun Protocolo.java
     * Valores: MENSAJE_TEXTO, CONEXION, DESCONEXION, etc.
     */
    private int tipoMensaje;
    
    /**
     * Tipo de envio segun Protocolo.java
     * Valores: UNICAST, BROADCAST, MULTICAST, ANYCAST
     */
    private int tipoEnvio;
    
    /**
     * Marca de tiempo cuando se creo el mensaje
     */
    private Date timestamp;
    
    // =============================================
    // CONSTRUCTORES
    // =============================================
    
    /**
     * Constructor completo para mensajes con todos los parametros
     * 
     * @param contenido Texto del mensaje
     * @param remitente ID del cliente que envia el mensaje
     * @param destino ID del cliente destino (null para broadcast)
     * @param tipoMensaje Tipo de mensaje (de Protocolo.java)
     * @param tipoEnvio Tipo de envio (de Protocolo.java)
     */
    public Mensaje(String contenido, String remitente, String destino, 
                  int tipoMensaje, int tipoEnvio) {
        this.contenido = contenido;
        this.remitente = remitente;
        this.destino = destino;
        this.tipoMensaje = tipoMensaje;
        this.tipoEnvio = tipoEnvio;
        this.timestamp = new Date(); // Timestamp automatico
    }
    
    /**
     * Constructor simplificado para mensajes de texto normales
     * 
     * @param contenido Texto del mensaje
     * @param remitente ID del cliente que envia el mensaje
     * @param tipoEnvio Tipo de envio (UNICAST, BROADCAST, etc.)
     */
    public Mensaje(String contenido, String remitente, int tipoEnvio) {
        this(contenido, remitente, null, Protocolo.MENSAJE_TEXTO, tipoEnvio);
    }
    
    // =============================================
    // METODOS DE ACCESO (GETTERS)
    // =============================================
    
    /**
     * Obtiene el contenido del mensaje
     * 
     * @return Texto del mensaje
     */
    public String getContenido() {
        return contenido;
    }
    
    /**
     * Obtiene el identificador del remitente
     * 
     * @return ID del cliente remitente
     */
    public String getRemitente() {
        return remitente;
    }
    
    /**
     * Obtiene el identificador del destino
     * 
     * @return ID del cliente destino, o null para broadcast
     */
    public String getDestino() {
        return destino;
    }
    
    /**
     * Obtiene el tipo de mensaje
     * 
     * @return Constante de tipo de mensaje de Protocolo.java
     */
    public int getTipoMensaje() {
        return tipoMensaje;
    }
    
    /**
     * Obtiene el tipo de envio
     * 
     * @return Constante de tipo de envio de Protocolo.java
     */
    public int getTipoEnvio() {
        return tipoEnvio;
    }
    
    /**
     * Obtiene la marca de tiempo del mensaje
     * 
     * @return Fecha y hora cuando se creo el mensaje
     */
    public Date getTimestamp() {
        return timestamp;
    }
    
    // =============================================
    // METODOS DE MODIFICACION (SETTERS)
    // =============================================
    
    /**
     * Establece el contenido del mensaje
     * 
     * @param contenido Nuevo texto del mensaje
     */
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
    
    /**
     * Establece el remitente del mensaje
     * 
     * @param remitente ID del cliente remitente
     */
    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }
    
    /**
     * Establece el destino del mensaje
     * 
     * @param destino ID del cliente destino
     */
    public void setDestino(String destino) {
        this.destino = destino;
    }
    
    /**
     * Establece el tipo de mensaje
     * 
     * @param tipoMensaje Constante de tipo de mensaje de Protocolo.java
     */
    public void setTipoMensaje(int tipoMensaje) {
        this.tipoMensaje = tipoMensaje;
    }
    
    /**
     * Establece el tipo de envio
     * 
     * @param tipoEnvio Constante de tipo de envio de Protocolo.java
     */
    public void setTipoEnvio(int tipoEnvio) {
        this.tipoEnvio = tipoEnvio;
    }
    
    // =============================================
    // METODOS DE UTILIDAD
    // =============================================
    
    /**
     * Convierte el mensaje a formato String legible
     * 
     * @return Representacion en texto del mensaje
     */
    @Override
    public String toString() {
        String tipoEnvioStr = Protocolo.getDescripcionTipoEnvio(tipoEnvio);
        String timestampStr = timestamp.toString();
        
        if (destino != null) {
            return String.format("[%s] %s -> %s: %s (%s)", 
                               timestampStr, remitente, destino, contenido, tipoEnvioStr);
        } else {
            return String.format("[%s] %s: %s (%s)", 
                               timestampStr, remitente, contenido, tipoEnvioStr);
        }
    }
    
    /**
     * Verifica si el mensaje es de tipo broadcast
     * 
     * @return true si es BROADCAST, false en caso contrario
     */
    public boolean esBroadcast() {
        return tipoEnvio == Protocolo.BROADCAST;
    }
    
    /**
     * Verifica si el mensaje es de tipo unicast
     * 
     * @return true si es UNICAST, false en caso contrario
     */
    public boolean esUnicast() {
        return tipoEnvio == Protocolo.UNICAST;
    }
    
    /**
     * Verifica si el mensaje es de tipo anycast
     * 
     * @return true si es ANYCAST, false en caso contrario
     */
    public boolean esAnycast() {
        return tipoEnvio == Protocolo.ANYCAST;
    }
    
    /**
     * Obtiene una descripcion legible del tipo de envio
     * 
     * @return Descripcion en texto del tipo de envio
     */
    public String getDescripcionEnvio() {
        return Protocolo.getDescripcionTipoEnvio(tipoEnvio);
    }
}