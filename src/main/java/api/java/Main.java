package api.java;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        /*    
        System.out.println("Inicia programa");
        UserHandler user = new UserHandler();
        boolean validation = user.validateUser("DiegoA", "Diego123");
        System.out.println("Result: " + validation);
        */
       int puerto = 8081;
        
        // Crear el servidor HTTP en el puerto indicado
        HttpServer server = HttpServer.create(new InetSocketAddress(puerto), 0);

        // Definir los endpoints y sus manejadores (handlers)
        server.createContext("/api/auth", new AuthenticationHandler());

        // Configurar un ejecutor por defecto
        server.setExecutor(null); 

        // Iniciar el servidor
        server.start();
        System.out.println("Servidor web iniciado en http://localhost:" + puerto + "/");
    }
}