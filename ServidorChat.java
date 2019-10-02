package ServidorChatMP;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorChat {

    private static Map<String, PrintWriter> Usuarios = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("El servidor de chat está funcionando...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    private static class Handler implements Runnable {

        private String Nombre;
        private Socket socket;
        private Scanner Entrada;
        private PrintWriter Salida;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                Entrada = new Scanner(socket.getInputStream());
                Salida = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                    Salida.println("SUBMIT NAME ");
                    Nombre = Entrada.nextLine();
                    if (Nombre == null || Nombre.isEmpty() || Nombre.equalsIgnoreCase("quit")) {
                        return;
                    }
                    synchronized (Usuarios) {
                        if (!Usuarios.containsKey(Nombre)) {
                            Usuarios.put(Nombre, Salida);
                            Salida.println("NAMEA CCEPTED " + Nombre);
                            Usuarios.values().forEach(Escritor -> {
                                Escritor.println("MESSAGE " + Nombre + " ha iniciado sesión");
                            });
                            break;
                        }
                    }
                }
                while (true) {
                    String Input = Entrada.nextLine();
                    int ESPACIO = Input.indexOf(' ');
                    if (Input.startsWith("/") && !Input.toLowerCase().startsWith("/quit")) {
                        String Destinatario = Input.substring(1, ESPACIO);
                        String Mensaje = Input.substring(ESPACIO, Input.length());
                        if (Usuarios.containsKey(Destinatario)) {
                            Usuarios.get(Nombre).println("MESSAGE Mensaje privado para " + Destinatario + ": " + Mensaje);
                            Usuarios.get(Destinatario).println("MESSAGE Mensaje privado de " + Nombre + ": " + Mensaje);
                        }
                    } else if (Input.toLowerCase().startsWith("/quit")) {
                        return;
                    } else {
                        Usuarios.values().forEach(Escritor -> {
                            String Mensaje = "MESSAGE ";
                            if (Escritor == Salida) {
                                Mensaje += "Yo";
                            } else {
                                Mensaje += Nombre;
                            }
                            Escritor.println(Mensaje + ": " + Input);
                        });
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (Salida != null || Nombre != null) {
                    Usuarios.remove(Nombre);
                    Usuarios.values().forEach(Escritor -> {
                        Escritor.println("MESSAGE " + Nombre + " ha cerrado sesión");
                    });
                }
                try {
                    socket.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

}
