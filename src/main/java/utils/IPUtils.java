package utils;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IPUtils {

    private static final String TOKEN = "f5a4a4e30122f9";

    public static String GetCurrentCountry() {
        try {
            URL url = new URL("https://ipinfo.io/json?token=" + TOKEN);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            return json.getString("country");

        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar IPInfo", e);
        }
    }
}
