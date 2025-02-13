package Tarea1;

import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static List<String> usuariosConectados = new ArrayList<>();
    private static List<ManejadorCliente> clientes = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket servidor = new ServerSocket(5050);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            while (true) {
                Socket socket = servidor.accept();
                ManejadorCliente cliente = new ManejadorCliente(socket);
                clientes.add(cliente);
                cliente.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para actualizar y mostrar los usuarios conectados
    private static synchronized void mostrarUsuarios() {
        System.out.println("Usuarios conectados: " + usuariosConectados);
        enviarMensajeATodos("Usuarios conectados: " + usuariosConectados);
    }

    // Método para enviar mensajes a todos los clientes conectados
    private static synchronized void enviarMensajeATodos(String mensaje) {
        for (ManejadorCliente cliente : clientes) {
            cliente.enviarMensaje(mensaje);
        }
    }

    // Clase para manejar clientes
    static class ManejadorCliente extends Thread {
        private Socket socket;
        private PrintWriter salida;
        private String usuario;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        public void enviarMensaje(String mensaje) {
            if (salida != null) {
                salida.println(mensaje);
            }
        }

        public void run() {
            try {
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                salida = new PrintWriter(socket.getOutputStream(), true);

                // Pedir nombre de usuario
                salida.println("Ingrese su nombre de usuario:");
                usuario = entrada.readLine();

                // Validar que el usuario no sea nulo o vacío
                if (usuario == null || usuario.trim().isEmpty()) {
                    usuario = "Usuario_" + new Random().nextInt(1000);
                }

                // Verificar que el nombre sea único
                synchronized (usuariosConectados) {
                    int contador = 1;
                    String nombreOriginal = usuario;
                    while (usuariosConectados.contains(usuario)) {
                        usuario = nombreOriginal + contador;
                        contador++;
                    }
                    usuariosConectados.add(usuario);
                }

                System.out.println(usuario + " se ha conectado.");
                mostrarUsuarios();

                // Escuchar mensajes del usuario
                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    System.out.println(usuario + ": " + mensaje);
                    enviarMensajeATodos(usuario + ": " + mensaje);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                synchronized (usuariosConectados) {
                    usuariosConectados.remove(usuario);
                }
                synchronized (clientes) {
                    clientes.remove(this);
                }

                System.out.println(usuario + " se ha desconectado.");
                mostrarUsuarios();
            }
        }
    }
}
