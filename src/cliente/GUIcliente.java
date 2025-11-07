package cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GUIcliente extends JFrame {
    private JTextField txtServidor;
    private JTextField txtPuerto;
    private JButton btnConectar;
    private JTextArea areaMensajes;
    private DefaultListModel<String> modeloUsuarios;
    private JList<String> listaUsuarios;
    private JButton btnBroadcast, btnUnicast, btnAnycast, btnMulticast, btnEnviar;
    private JTextField txtMensaje;

    public GUIcliente() {
        setTitle("Cliente Chat (TCP/UDP)");
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
        scrollMensajes.setBorder(BorderFactory.createTitledBorder("Mensajess"));

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

        // 🎯 Evento del botón MULTICAST
        btnMulticast.addActionListener(e -> abrirVentanaMulticast());
    }

    // 🪟 Ventana secundaria para seleccionar miembros MULTICAST
    private void abrirVentanaMulticast() {
        JDialog dialogo = new JDialog(this, "Seleccionar miembros del grupo", true);
        dialogo.setSize(300, 300);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(10, 10));

        // Lista de usuarios disponibles
        JList<String> listaSeleccion = new JList<>(modeloUsuarios);
        listaSeleccion.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dialogo.add(new JScrollPane(listaSeleccion), BorderLayout.CENTER);

        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAceptar = new JButton("Crear grupo");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        btnCancelar.addActionListener(ev -> dialogo.dispose());
        btnAceptar.addActionListener(ev -> {
            java.util.List<String> seleccionados = listaSeleccion.getSelectedValuesList();
            if (!seleccionados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Grupo multicast creado con:\n" + String.join(", ", seleccionados),
                    "Multicast", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Selecciona al menos un usuario.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            dialogo.dispose();
        });

        dialogo.setVisible(true);
    }

    // Método para agregar mensajes en el chat
    public void agregarMensaje(String mensaje) {
        areaMensajes.append(mensaje + "\n");
    }

    // Método para actualizar la lista de usuarios conectados
    public void actualizarUsuarios(java.util.List<String> usuarios) {
        modeloUsuarios.clear();
        for (String u : usuarios) {
            modeloUsuarios.addElement(u);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GUIcliente().setVisible(true);
        });
    }
}
