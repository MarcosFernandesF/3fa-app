package server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Usuario;

import java.io.File;
import java.util.*;

public class UserRepository {
    private static final String FILE_PATH = "src/main/java/server/users/usuarios.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Usuario> loadUsuarios() {
        try {
            File f = new File(FILE_PATH);
            if (!f.exists()) return new ArrayList<>();
            return mapper.readValue(f, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar usuários", e);
        }
    }

    public static void saveUsuarios(List<Usuario> usuarios) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), usuarios);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar usuários", e);
        }
    }

    public static void addUsuario(Usuario u) {
        List<Usuario> lista = loadUsuarios();
        lista.removeIf(existing -> existing.nome.equalsIgnoreCase(u.nome));
        lista.add(u);
        saveUsuarios(lista);
    }

    public static Optional<Usuario> findByNome(String nome) {
        System.out.println("Arquivo usuarios.json em: " + new File(FILE_PATH).getAbsolutePath());
        return loadUsuarios().stream()
                .filter(u -> u.nome.equalsIgnoreCase(nome))
                .findFirst();
    }
}