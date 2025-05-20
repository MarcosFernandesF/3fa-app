package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Usuario;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserAuthenticatorStore {
    private static final String FILE_PATH = "src/main/java/client/authenticator/users-secrets.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<UserAuthenticator> loadUsersSecrets() {
        try {
            File f = new File(FILE_PATH);
            if (!f.exists()) return new ArrayList<>();
            return mapper.readValue(f, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar usuários", e);
        }
    }

    public static void saveUsuarios(List<UserAuthenticator> usuarios) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), usuarios);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar usuários", e);
        }
    }

    public static void addUserAuthenticator(UserAuthenticator u) {
        List<UserAuthenticator> lista = loadUsersSecrets();
        lista.removeIf(existing -> existing.nome.equalsIgnoreCase(u.nome));
        lista.add(u);
        saveUsuarios(lista);
    }

    public static Optional<UserAuthenticator> findByNome(String nome) {
        System.out.println("Arquivo usuarios.json em: " + new File(FILE_PATH).getAbsolutePath());
        return loadUsersSecrets().stream()
                .filter(u -> u.nome.equalsIgnoreCase(nome))
                .findFirst();
    }
}