package servidor;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * INTERFAZ GRAFICA DEL SERVIDOR - MONITOREO EN TIEMPO REAL
 * 
 * Esta interfaz permite visualizar toda la actividad del servidor:
 * - Clientes conectados (TCP y UDP)
 * - Mensajes entrantes y salientes
 * - Estados del servidor y metricas
 * - Control de operaciones del servidor
 * 
 * Caracteristicas:
 * - Actualizacion automatica cada 2 segundos
 * - Visualizacion diferenciada por tipo de protocolo
 * - Log de eventos con timestamp
 * - Metricas en tiempo real
 * 
 * @author Alin (Implementado por Angel)
 * @version 1.0
 */
public class GUIservidor extends JFrame {
    
    // =============================================
    // COMPONENTES DE LA INTERFAZ
    // =============================================
    
    // Paneles principales
    private JPanel panelPrincipal;
    private JPanel panelClientes;
    private JPanel panelLogs;
    private JPanel panelControl;
    
    // Componentes para lista de clientes
    private JTextArea areaClientesTCP;
    private JTextArea areaClientesUDP;
    private JTextArea areaLogs;
    
    // Scroll panes para las areas de texto
    private JScrollPane scrollClientesTCP;
    private JScrollPane scrollClientesUDP;
    private JScrollPane scrollLogs;
    
    // Componentes de control y metricas
    private JLabel labelEstado;
    private JLabel labelClientesTCP;
    private JLabel labelClientesUDP;
    private JLabel labelTotalMensajes;
    private JButton botonActualizar;
    private JButton botonLimpiarLogs;
    
    // Contadores
    private int totalMensajes = 0;
    
    // Formato para timestamps
    private SimpleDateFormat formatoFecha;
    
    // =============================================
    // CONSTRUCTOR
    // =============================================
    
    /**
     * Constructor de la interfaz grafica del servidor
     * Inicializa todos los componentes y configura la interfaz
     */
    public GUIservidor() {
        // Configuracion basica de la ventana
        super("Servidor de Chat - Monitoreo en Tiempo Real");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null); // Centrar en pantalla
        
        // Inicializar componentes
        inicializarComponentes();
        configurarInterfaz();
        
        // Iniciar actualizacion automatica
        iniciarActualizacionAutomatica();
        
        // Mostrar ventana
        setVisible(true);
        
        agregarLog("Interfaz del servidor iniciada correctamente");
        agregarLog("Servidor escuchando en TCP:12345 y UDP:12346");
    }
    
    // =============================================
    // INICIALIZACION DE COMPONENTES
    // =============================================
    
    /**
     * Inicializa todos los componentes de la interfaz
     */
    private void inicializarComponentes() {
        // Inicializar formato de fecha
        formatoFecha = new SimpleDateFormat("HH:mm:ss");
        
        // Crear paneles principales
        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelClientes = new JPanel(new GridLayout(1, 2, 10, 10));
        panelLogs = new JPanel(new BorderLayout());
        panelControl = new JPanel(new FlowLayout());
        
        // Crear areas de texto
        areaClientesTCP = new JTextArea();
        areaClientesUDP = new JTextArea();
        areaLogs = new JTextArea();
        
        // Configurar areas de texto
        configurarAreaTexto(areaClientesTCP);
        configurarAreaTexto(areaClientesUDP);
        configurarAreaTexto(areaLogs);
        
        // Crear scroll panes
        scrollClientesTCP = crearScrollPane(areaClientesTCP, "Clientes TCP Conectados");
        scrollClientesUDP = crearScrollPane(areaClientesUDP, "Clientes UDP Registrados");
        scrollLogs = crearScrollPane(areaLogs, "Log de Eventos del Servidor");
        
        // Configurar etiquetas de estado
        labelEstado = new JLabel("Estado: INICIADO");
        labelClientesTCP = new JLabel("TCP: 0");
        labelClientesUDP = new JLabel("UDP: 0");
        labelTotalMensajes = new JLabel("Mensajes: 0");
        
        // Configurar botones
        botonActualizar = new JButton("Actualizar Ahora");
        botonLimpiarLogs = new JButton("Limpiar Logs");
        
        // Configurar acciones de botones
        configurarAccionesBotones();
    }
    
    /**
     * Configura las propiedades basicas de un JTextArea
     * 
     * @param areaTexto Area de texto a configurar
     */
    private void configurarAreaTexto(JTextArea areaTexto) {
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaTexto.setBackground(new Color(240, 240, 240));
        areaTexto.setMargin(new Insets(5, 5, 5, 5));
    }
    
    /**
     * Crea un JScrollPane para un JTextArea con borde titulado
     * 
     * @param areaTexto Area de texto a envolver
     * @param titulo Titulo para el borde
     * @return JScrollPane configurado
     */
    private JScrollPane crearScrollPane(JTextArea areaTexto, String titulo) {
        JScrollPane scrollPane = new JScrollPane(areaTexto);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            titulo, 
            TitledBorder.LEFT, 
            TitledBorder.TOP
        ));
        return scrollPane;
    }
    
    /**
     * Configura las acciones de los botones
     */
    private void configurarAccionesBotones() {
        // Boton Actualizar
        botonActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarInformacion();
            }
        });
        
        // Boton Limpiar Logs
        botonLimpiarLogs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                areaLogs.setText("");
                agregarLog("Logs limpiados manualmente");
            }
        });
    }
    
    // =============================================
    // CONFIGURACION DE LA INTERFAZ
    // =============================================
    
    /**
     * Configura el layout y agrega todos los componentes a la interfaz
     */
    private void configurarInterfaz() {
        // Configurar panel de clientes
        panelClientes.add(crearPanelCliente(scrollClientesTCP, "CLIENTES TCP", Color.BLUE));
        panelClientes.add(crearPanelCliente(scrollClientesUDP, "CLIENTES UDP", Color.MAGENTA));
        
        // Configurar panel de control
        panelControl.setBackground(Color.LIGHT_GRAY);
        panelControl.add(new JLabel("SERVIDOR ACTIVO - "));
        panelControl.add(labelEstado);
        panelControl.add(new JLabel(" | "));
        panelControl.add(labelClientesTCP);
        panelControl.add(new JLabel(" | "));
        panelControl.add(labelClientesUDP);
        panelControl.add(new JLabel(" | "));
        panelControl.add(labelTotalMensajes);
        panelControl.add(new JLabel(" | "));
        panelControl.add(botonActualizar);
        panelControl.add(botonLimpiarLogs);
        
        // Configurar panel de logs
        panelLogs.add(scrollLogs, BorderLayout.CENTER);
        
        // Ensamblar interfaz principal
        panelPrincipal.add(panelClientes, BorderLayout.NORTH);
        panelPrincipal.add(panelLogs, BorderLayout.CENTER);
        panelPrincipal.add(panelControl, BorderLayout.SOUTH);
        
        // Agregar margenes
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Agregar a la ventana principal
        setContentPane(panelPrincipal);
    }
    
    /**
     * Crea un panel para cliente con estilo especifico
     * 
     * @param scrollPane ScrollPane con el area de texto
     * @param titulo Titulo del panel
     * @param color Color del titulo
     * @return JPanel configurado
     */
    private JPanel crearPanelCliente(JScrollPane scrollPane, String titulo, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        
        TitledBorder border = (TitledBorder) scrollPane.getBorder();
        border.setTitleColor(color);
        border.setTitle(titulo);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    // =============================================
    // FUNCIONALIDADES PRINCIPALES
    // =============================================
    
    /**
     * Actualiza toda la informacion en la interfaz
     * - Lista de clientes TCP/UDP
     * - Metricas y contadores
     * - Estado del servidor
     */
    public void actualizarInformacion() {
        try {
            // Obtener listas actualizadas de clientes
            List<String> clientesTCP = ServidorMixto.getClientesTCP();
            List<String> clientesUDP = ServidorMixto.getClientesUDP();
            
            // Actualizar area de clientes TCP
            areaClientesTCP.setText("");
            if (clientesTCP.isEmpty()) {
                areaClientesTCP.append("No hay clientes TCP conectados\n");
            } else {
                areaClientesTCP.append("Total: " + clientesTCP.size() + " clientes\n");
                areaClientesTCP.append("====================\n");
                for (int i = 0; i < clientesTCP.size(); i++) {
                    areaClientesTCP.append((i + 1) + ". " + clientesTCP.get(i) + "\n");
                }
            }
            
            // Actualizar area de clientes UDP
            areaClientesUDP.setText("");
            if (clientesUDP.isEmpty()) {
                areaClientesUDP.append("No hay clientes UDP registrados\n");
            } else {
                areaClientesUDP.append("Total: " + clientesUDP.size() + " clientes\n");
                areaClientesUDP.append("====================\n");
                for (int i = 0; i < clientesUDP.size(); i++) {
                    areaClientesUDP.append((i + 1) + ". " + clientesUDP.get(i) + "\n");
                }
            }
            
            // Actualizar metricas
            labelClientesTCP.setText("TCP: " + clientesTCP.size());
            labelClientesUDP.setText("UDP: " + clientesUDP.size());
            labelTotalMensajes.setText("Mensajes: " + totalMensajes);
            labelEstado.setText("Estado: ACTIVO - " + (clientesTCP.size() + clientesUDP.size()) + " clientes");
            
        } catch (Exception e) {
            agregarLog("ERROR al actualizar informacion: " + e.getMessage());
        }
    }
    
    /**
     * Agrega un mensaje al log con timestamp
     * 
     * @param mensaje Mensaje a agregar al log
     */
    public void agregarLog(String mensaje) {
        String timestamp = formatoFecha.format(new Date());
        areaLogs.append("[" + timestamp + "] " + mensaje + "\n");
        
        // Auto-scroll al final
        areaLogs.setCaretPosition(areaLogs.getDocument().getLength());
        
        // Incrementar contador si es un mensaje de chat
        if (mensaje.contains("BROADCAST") || mensaje.contains("UNICAST") || 
            mensaje.contains("ANYCAST") || mensaje.contains("Mensaje de")) {
            totalMensajes++;
        }
    }
    
    /**
     * Agrega un mensaje de evento especial al log
     * 
     * @param mensaje Mensaje del evento
     * @param tipo Tipo de evento (CONEXION, DESCONEXION, ERROR, etc.)
     */
    public void agregarEvento(String mensaje, String tipo) {
        String timestamp = formatoFecha.format(new Date());
        String mensajeFormateado = "[" + timestamp + "] [" + tipo + "] " + mensaje;
        areaLogs.append(mensajeFormateado + "\n");
        areaLogs.setCaretPosition(areaLogs.getDocument().getLength());
    }
    
    // =============================================
    // ACTUALIZACION AUTOMATICA
    // =============================================
    
    /**
     * Inicia la actualizacion automatica cada 2 segundos
     */
    private void iniciarActualizacionAutomatica() {
        Timer timer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarInformacion();
            }
        });
        timer.start();
    }
    
    // =============================================
    // METODO PRINCIPAL
    // =============================================
    
    /**
     * Punto de entrada para la interfaz grafica del servidor
     * 
     * @param args Argumentos de linea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Ejecutar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUIservidor();
            }
        });
    }
    
}