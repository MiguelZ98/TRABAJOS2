package ServidorChatMP;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorMpPrincipal {

    private static Map<String, PrintWriter> Usuarios = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("El servidor de chat está funcionando...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept(),Usuarios));
            }
        }catch(IOException ex){
            System.out.println("Error al establecer socket de servidor" + ex.toString());
            System.exit(0);
        }
    }

}
