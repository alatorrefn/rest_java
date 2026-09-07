package api.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class AuthenticationHandler implements HttpHandler {

    private static final String URL = "jdbc:mysql://localhost:3306/db_java";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "Dm260705";

    private boolean validateUser(String user, String password) {
        if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        // conectar a la bd
        try (Connection conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
            if (conexion != null) {
                System.out.println("¡Conexión exitosa a la base de datos!");
            }
            System.out.println("Conexión exitosa a MySQL.\n");

            // obtener usuario y contraseña de la bd
            String query = "SELECT * FROM users WHERE username = '" + user + "'";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                String userFromDB = rs.getString("username");
                String passwordFromDB = rs.getString("password");
                System.out.println("Info user: " + userFromDB + " " + passwordFromDB);
                if (user.equals(userFromDB) && password.equals(passwordFromDB)) {
                    return true;
                }
            } else {
                System.out.println("La consulta no obtuvo resultados.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }

        return false;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // 1. Validar que el método sea POST
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // 2. Leer el JSON del InputStream
        InputStream is = exchange.getRequestBody();
        String jsonBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // 3. Extraer los dos campos desde el JSON
        String username = obtenerValorJson(jsonBody, "username");
        String password = obtenerValorJson(jsonBody, "password");

        boolean isValid = validateUser(username, password);
        String respuesta;

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        // 4. Procesar la respuesta
        if (isValid) {
            respuesta = String.format("{\"success\": true, \"username\": \"%s\"}",
                    username);
            exchange.sendResponseHeaders(200, respuesta.getBytes().length);

        } else {
            respuesta = "{\"error\": \"Usuario invalido o contraseña incorrecta\"}";
            exchange.sendResponseHeaders(400, respuesta.getBytes().length);
        }

        // 5. Responder al cliente;

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(respuesta.getBytes());
        }
    }

    // Método auxiliar nativo para extraer el valor de una clave en un JSON plano
    private String obtenerValorJson(String json, String clave) {
        Pattern pattern = Pattern.compile("\"" + clave + "\"\\s*:\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
