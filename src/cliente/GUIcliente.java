package cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class GUIcliente extends JFrame {
    private JTextField txtServidor;
    private JTextField txtPuerto;
    private JButton btnConectar;
    private JTextArea areaMensajes;
    private DefaultListModel<String> modeloUsuarios;
    private JList<String> listaUsuarios;
    private JButton btnBroadcast, btnUnicast, btnAnycast, btnMulticast, btnEnviar;
    private JTextField txtMensaje;

    private ClienteChat clienteTCP;
    private boolean conectado = false;

    public GUIcliente() {
        setTitle("Cliente Chat TCP/UDP");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 🧩 Panel superior: conexión
        JPanel panelConexion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelConexion.add(new JLabel("Servidor:"));
        txtServidor = new JTextField("localhost", 10);
        panelConexion.add(txtServidor);

        panelConexion.add(new JLabel("Puerto:"));
        txtPuerto = new JTextField("12345", 5);
        panelConexion.add(txtPuerto);

        btnConectar = new JButton("Conectar");
        panelConexion.add(btnConectar);
        add(panelConexion, BorderLayout.NORTH);

        // 💬 Panel central dividido en mensajes y usuarios
        JSplitPane panelCentral = new JSplitPane();
        panelCentral.setResizeWeight(0.75);

        // Izquierda: área de mensajes
        areaMensajes = new JTextArea();
        areaMensajes.setEditable(false);
        JScrollPane scrollMensajes = new JScrollPane(areaMensajes);
        scrollMensajes.setBorder(BorderFactory.createTitledBorder("Mensajes"));

        // Derecha: lista de usuarios conectados
        modeloUsuarios = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloUsuarios);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        scrollUsuarios.setBorder(BorderFactory.createTitledBorder("Usuarios conectados"));

        panelCentral.setLeftComponent(scrollMensajes);
        panelCentral.setRightComponent(scrollUsuarios);
        add(panelCentral, BorderLayout.CENTER);

        // 🔘 Panel inferior: botones de cast + barra de mensaje
        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnBroadcast = new JButton("BROADCAST");
        btnUnicast = new JButton("UNICAST");
        btnAnycast = new JButton("ANYCAST");
        btnMulticast = new JButton("MULTICAST");

        panelBotones.add(btnBroadcast);
        panelBotones.add(btnUnicast);
        panelBotones.add(btnAnycast);
        panelBotones.add(btnMulticast);

        panelInferior.add(panelBotones, BorderLayout.NORTH);

        JPanel panelMensaje = new JPanel(new BorderLayout(5, 5));
        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");

        panelMensaje.add(txtMensaje, BorderLayout.CENTER);
        panelMensaje.add(btnEnviar, BorderLayout.EAST);

        panelInferior.add(panelMensaje, BorderLayout.SOUTH);
        add(panelInferior, BorderLayout.SOUTH);

        // 🎯 Acción del botón Conectar
        btnConectar.addActionListener(e -> {
            if (!conectado) {
                String host = txtServidor.getText().trim();
                int puerto = Integer.parseInt(txtPuerto.getText().trim());
                clienteTCP = new ClienteChat(this);
                if (clienteTCP.conectar(host, puerto)) {
                    conectado = true;
                    btnConectar.setText("Desconectar");
                }
            } else {
                clienteTCP.cerrar();
                conectado = false;
                btnConectar.setText("Conectar");
                agregarMensaje("🔌 Desconectado del servidor.");
            }
        });

        // ✉️ Acción del botón Enviar
        btnEnviar.addActionListener(e -> {
            if (conectado && clienteTCP != null) {
                String mensaje = txtMensaje.getText().trim();
                if (!mensaje.isEmpty()) {
                    clienteTCP.enviarMensaje(mensaje);
                    agregarMensaje("Tú: " + mensaje);
                    txtMensaje.setText("");
                }
            } else {
                agregarMensaje("⚠️ No estás conectado al servidor.");
            }
        });

        // 🪄 Botones para BROADCAST / UNICAST / ANYCAST / MULTICAST
        btnBroadcast.addActionListener(e -> enviarComando("BROADCAST"));
        btnUnicast.addActionListener(e -> enviarComando("UNICAST"));
        btnAnycast.addActionListener(e -> enviarComando("ANYCAST"));
        btnMulticast.addActionListener(e -> abrirVentanaMulticast());
    }

    private void enviarComando(String tipo) {
        if (conectado && clienteTCP != null) {
            String mensaje = JOptionPane.showInputDialog(this, "Escribe el mensaje para " + tipo + ":");
            if (mensaje != null && !mensaje.trim().isEmpty()) {
                if (tipo.equals("UNICAST")) {
                    String destino = JOptionPane.showInputDialog(this, "ID del destino:");
                    clienteTCP.enviarMensaje(tipo + ":" + destino + ":" + mensaje);
                } else {
                    clienteTCP.enviarMensaje(tipo + ":" + mensaje);
                }
                agregarMensaje("Tú (" + tipo + "): " + mensaje);
            }
        } else {
            agregarMensaje("⚠️ No estás conectado al servidor.");
        }
    }

    // 🪟 Ventana secundaria para seleccionar miembros MULTICAST
    private void abrirVentanaMulticast() {
        if (!conectado) {
            agregarMensaje("⚠️ Conéctate antes de crear un grupo multicast.");
            return;
        }

        JDialog dialogo = new JDialog(this, "Seleccionar miembros del grupo", true);
        dialogo.setSize(300, 300);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(10, 10));

        // Lista de usuarios disponibles
        JList<String> listaSeleccion = new JList<>(modeloUsuarios);
        listaSeleccion.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dialogo.add(new JScrollPane(listaSeleccion), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAceptar = new JButton("Crear grupo");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        btnCancelar.addActionListener(ev -> dialogo.dispose());
        btnAceptar.addActionListener(ev -> {
            List<String> seleccionados = listaSeleccion.getSelectedValuesList();
            if (!seleccionados.isEmpty()) {
                String grupo = String.join(",", seleccionados);
                agregarMensaje("Grupo multicast creado con: " + grupo);
                clienteTCP.enviarMensaje("MULTICAST:" + grupo);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Selecciona al menos un usuario.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            dialogo.dispose();
        });

        dialogo.setVisible(true);
    }

    // Mostrar mensajes en el área de texto
    public void agregarMensaje(String mensaje) {
        areaMensajes.append(mensaje + "\n");
    }

    // Actualizar lista de usuarios conectados
    public void actualizarUsuarios(java.util.List<String> usuarios) {
        modeloUsuarios.clear();
        for (String u : usuarios) {
            modeloUsuarios.addElement(u);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIcliente().setVisible(true));
    }
}
