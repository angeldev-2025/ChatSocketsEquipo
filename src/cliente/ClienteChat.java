import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClienteChat extends JFrame {
    private static final String HOST = "localhost";
    private static final int PUERTO_TCP = 12345;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JTextArea areaChat;
    private JTextField campoMensaje;
    private JButton botonEnviar;
    private JLabel estadoConexion;

    public ClienteChat() {
        super("Cliente Chat TCP");

        // --- Configurar interfaz ---
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setFont(new Font("Arial", Font.PLAIN, 14));
        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        campoMensaje = new JTextField();
        botonEnviar = new JButton("Enviar");
        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(botonEnviar, BorderLayout.EAST);

        estadoConexion = new JLabel("Desconectado");
        estadoConexion.setHorizontalAlignment(SwingConstants.CENTER);
        add(estadoConexion, BorderLayout.NORTH);
        add(panelInferior, BorderLayout.SOUTH);

        // --- Eventos ---
        botonEnviar.addActionListener(e -> enviarMensaje());
        campoMensaje.addActionListener(e -> enviarMensaje());

        // --- Conectar con el servidor ---
        conectarServidor();

        setVisible(true);
    }

    private void conectarServidor() {
        try {
            socket = new Socket(HOST, PUERTO_TCP);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            estadoConexion.setText("Conectado a " + HOST + ":" + PUERTO_TCP);
            areaChat.append("✅ Conectado al servidor TCP\n");

            // Hilo para escuchar mensajes del servidor
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        areaChat.append("Servidor: " + mensaje + "\n");
                    }
                } catch (IOException ex) {
                    areaChat.append("❌ Conexión cerrada por el servidor.\n");
                }
            }).start();

        } catch (IOException e) {
            estadoConexion.setText("Error de conexión");
            areaChat.append("❌ No se pudo conectar al servidor.\n");
        }
    }

    private void enviarMensaje() {
        String mensaje = campoMensaje.getText().trim();
        if (!mensaje.isEmpty() && out != null) {
            out.println(mensaje);
            areaChat.append("Tú: " + mensaje + "\n");
            campoMensaje.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClienteChat::new);
    }
}
