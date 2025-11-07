package common;

public class TiposMensaje {

    public static void procesarMensaje(String mensaje) {
        if (mensaje == null || mensaje.isEmpty()) return;

        String[] partes = mensaje.split("\\|");
        int tipo = Integer.parseInt(partes[0]);

        switch(tipo) {
            case Protocolo.BROADCAST:
                System.out.println("[BROADCAST] " + partes[1]);
                break;
            case Protocolo.UNICAST:
                System.out.println("[UNICAST] " + partes[2] + " (de " + partes[1] + ")");
                break;
            case Protocolo.ANYCAST:
                System.out.println("[ANYCAST] " + partes[1]);
                break;
            case Protocolo.CONEXION:
                System.out.println("[CONEXIÓN] Cliente conectado: " + partes[1]);
                break;
            case Protocolo.MENSAJE_TEXTO:
                System.out.println("[MENSAJE] " + partes[1]);
                break;
            default:
                System.out.println("[DESCONOCIDO] " + mensaje);
        }
    }
}
