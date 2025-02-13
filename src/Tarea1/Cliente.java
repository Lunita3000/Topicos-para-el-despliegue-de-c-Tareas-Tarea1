package Tarea1;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Cliente {
    private static DefaultListModel<String> modeloUsuarios = new DefaultListModel<>();
    private static JTextArea areaChat = new JTextArea();
    private static PrintWriter salida;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> crearInterfaz());
    }

    public static void crearInterfaz() {
        JFrame ventana = new JFrame("Chat Cliente");
        ventana.setSize(500, 400);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLayout(new BorderLayout());

        // Panel de usuarios conectados
        JList<String> listaUsuarios = new JList<>(modeloUsuarios);
        JScrollPane panelUsuarios = new JScrollPane(listaUsuarios);
        panelUsuarios.setPreferredSize(new Dimension(150, 0));

        // Área de chat
        areaChat.setEditable(false);
        JScrollPane panelChat = new JScrollPane(areaChat);

        // Campo de mensaje
        JTextField campoMensaje = new JTextField();
        JButton btnEnviar = new JButton("Enviar");

        // Panel inferior para mensajes
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);

        // Agregar componentes a la ventana
        ventana.add(panelUsuarios, BorderLayout.EAST);
        ventana.add(panelChat, BorderLayout.CENTER);
        ventana.add(panelInferior, BorderLayout.SOUTH);

        // Acción al presionar "Enviar"
        btnEnviar.addActionListener(e -> {
            String mensaje = campoMensaje.getText().trim();
            if (!mensaje.isEmpty()) {
                salida.println(mensaje);
                campoMensaje.setText("");
            }
        });

        // Acción al presionar "Enter"
        campoMensaje.addActionListener(e -> btnEnviar.doClick());

        ventana.setVisible(true);

        conectarServidor();
    }

    public static void conectarServidor() {
        try {
            Socket socket = new Socket("127.0.0.1", 5050);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            // Pedir nombre de usuario
            System.out.println("Este es un mensaje nuevo");
            String usuario = JOptionPane.showInputDialog("Ingrese su nombre de usuario:");
            if (usuario == null || usuario.trim().isEmpty()) {
                usuario = "Usuario_" + new java.util.Random().nextInt(1000);
            }
            salida.println(usuario);

            // Hilo para recibir mensajes y actualizar la interfaz
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = entrada.readLine()) != null) {
                        System.out.println("Mensaje recibido: " + mensaje);

                        if (mensaje.startsWith("Usuarios conectados:")) {
                            actualizarListaUsuarios(mensaje);
                        } else {
                            areaChat.append(mensaje + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "No se pudo conectar al servidor.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void actualizarListaUsuarios(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            modeloUsuarios.clear();
            String lista = mensaje.replace("Usuarios conectados: [", "").replace("]", "");
            String[] usuarios = lista.split(", ");

            for (String usuario : usuarios) {
                if (!usuario.trim().isEmpty()) {
                    modeloUsuarios.addElement(usuario.trim());
                }
            }
        });
    }
}
