package cliente;
import java.io.*;
import java.net.*;

public class ClienteChat {
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    private GUIcliente gui;

    public ClienteChat(GUIcliente gui) {
        this.gui = gui;
    }

    // 🔌 Conectar al servidor TCP
    public boolean conectar(String host, int puerto) {
        try {
            socket = new Socket(host, puerto);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            gui.agregarMensaje(" Conectado al servidor TCP en " + host + ":" + puerto);

            // Hilo para escuchar mensajes del servidor
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = entrada.readLine()) != null) {
                        gui.agregarMensaje("Servidor: " + mensaje);
                    }
                } catch (IOException e) {
                    gui.agregarMensaje("❌ Conexión cerrada.");
                }
            }).start();

            return true;
        } catch (IOException e) {
            gui.agregarMensaje("❌ Error al conectar: " + e.getMessage());
            return false;
        }
    }

    // ✉️ Enviar mensaje al servidor
    public void enviarMensaje(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
        }
    }

    // 🔒 Cerrar conexión
    public void cerrar() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
