package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserAuthenticatorRepository {
    private static final String FILE_PATH = "src/main/java/client/authenticator/users-secrets.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<UserAuthenticator> selectAllUsersSecrets() {
        try {
            File f = new File(FILE_PATH);
            if (!f.exists()) return new ArrayList<>();
            return mapper.readValue(f, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Erro ao selecionar todas as secrets armazenadas", e);
        }
    }

    public static void saveUserAuthenticator(List<UserAuthenticator> usersAuthenticators) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), usersAuthenticators);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar uma user-secret", e);
        }
    }

    public static void addUserAuthenticator(UserAuthenticator userAuthenticator) {
        List<UserAuthenticator> usersSecrets = selectAllUsersSecrets();
        usersSecrets.removeIf(existing -> existing.Name.equalsIgnoreCase(userAuthenticator.Name));
        usersSecrets.add(userAuthenticator);
        saveUserAuthenticator(usersSecrets);
    }

    public static Optional<UserAuthenticator> selectUserSecretByName(String name) {
        return selectAllUsersSecrets().stream()
            .filter(user -> user.Name.equalsIgnoreCase(name))
            .findFirst();
    }
}