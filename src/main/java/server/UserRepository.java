package server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.User;

import java.io.File;
import java.util.*;

public class UserRepository {
    private static final String FILE_PATH = "src/main/java/server/users/users.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<User> SelectAllUsers() {
        try {
            File f = new File(FILE_PATH);
            if (!f.exists()) return new ArrayList<>();
            return mapper.readValue(f, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Erro ao selecionar todos usuários", e);
        }
    }

    public static void SaveUsers(List<User> users) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), users);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar usuários", e);
        }
    }

    public static void AddUser(User userToAdd) {
        List<User> users = SelectAllUsers();
        users.removeIf(existing -> existing.Name.equalsIgnoreCase(userToAdd.Name));
        users.add(userToAdd);
        SaveUsers(users);
    }

    public static Optional<User> SelectUserByName(String name) {
        return SelectAllUsers().stream()
            .filter(u -> u.Name.equalsIgnoreCase(name))
            .findFirst();
    }
}