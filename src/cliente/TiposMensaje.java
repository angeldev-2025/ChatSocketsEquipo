package cliente;

import common.Protocolo;

/**
 * Clase TiposMensaje
 * 
 * Procesa mensajes recibidos usando los códigos numéricos definidos en Protocolo.java
 * 
 * Autor: Mario
 */
public class TiposMensaje {

    /**
     * Procesa un mensaje recibido por UDP.
     * El mensaje llega en formato: TIPO:contenido
     *
     * Ejemplo:
     * 201:Hola a todos
     * 200:Cliente2:Hola
     */
    public static void procesarMensaje(String mensaje) {

        if (mensaje == null || mensaje.isEmpty()) return;

        // Separar tipo y contenido
        String[] partes = mensaje.split(":", 2);

        if (partes.length < 1) {
            System.out.println("[FORMATO INVALIDO] " + mensaje);
            return;
        }

        int tipo;
        try {
            tipo = Integer.parseInt(partes[0]);  // <-- Aquí usamos los códigos numéricos
        } catch (NumberFormatException e) {
            System.out.println("[TIPO INVALIDO] " + mensaje);
            return;
        }

        String contenido = (partes.length > 1) ? partes[1] : "";

        // --------------------------------------------
        // PROCESAR SEGÚN EL TIPO DE MENSAJE
        // --------------------------------------------

        switch (tipo) {

            case Protocolo.BROADCAST:  // 201
                System.out.println("[BROADCAST] " + contenido);
                break;

            case Protocolo.UNICAST:  // 200
                System.out.println("[UNICAST] " + contenido);
                break;

            case Protocolo.ANYCAST:  // 203
                System.out.println("[ANYCAST] " + contenido);
                break;

            case Protocolo.MENSAJE_TEXTO: // 102
                System.out.println("[MENSAJE] " + contenido);
                break;

            case Protocolo.CONEXION: // 100
                System.out.println("[CONEXIÓN] " + contenido);
                break;

            case Protocolo.DESCONEXION: // 101
                System.out.println("[DESCONEXIÓN] " + contenido);
                break;

            default:
                System.out.println("[TIPO DESCONOCIDO " + tipo + "] " + contenido);
        }
    }
}
