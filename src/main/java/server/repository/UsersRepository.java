package server.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.CryptoUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

/**
 * Repositório de usuários do lado do servidor.
 * Responsável por salvar, carregar e consultar dados de usuários armazenados.
 */
public class UsersRepository {

    private static final String FILE_PATH = "src/main/resources/server/users.txt";
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Obtém a lista completa de usuários armazenados no servidor.
     * @return Lista de objetos {@link User}. Se o arquivo não existir, retorna uma lista vazia.
     */
    public static List<User> SelectAll() {
        try {
            File f = new File(FILE_PATH);
            if (!f.exists() || f.length() == 0) return new ArrayList<>();

            String encryptedData = Files.readString(f.toPath());
            if (encryptedData.trim().isEmpty()) return new ArrayList<>();

            String jsonData = CryptoUtils.DecryptFileData(encryptedData);
            return mapper.readValue(jsonData, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Erro ao ler ou descriptografar o repositório de usuários: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Salva a lista de usuários no servidor.
     * @param users Lista de usuários a ser salva.
     */
    public static void Save(List<User> users) {
        try {
            String jsonData = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(users);
            String encryptedData = CryptoUtils.EncryptFileData(jsonData);
            Files.writeString(new File(FILE_PATH).toPath(), encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar usuários criptografados", e);
        }
    }

    /**
     * Adiciona ou atualiza um usuário no servidor.
     * @param userToAdd Usuário a ser adicionado ou atualizado.
     */
    public static void Add(User userToAdd) {
        List<User> users = SelectAll();
        users.removeIf(existing -> existing.Name.equalsIgnoreCase(userToAdd.Name));
        users.add(userToAdd);
        Save(users);
    }

    /**
     * Busca um usuário pelo nome (case-insensitive).
     * @param name Nome do usuário a ser buscado.
     * @return Um {@link Optional} com o usuário encontrado, ou {@link Optional#empty()} se não encontrar.
     */
    public static Optional<User> SelectByName(String name) {
        return SelectAll().stream()
                .filter(user -> user.Name.equalsIgnoreCase(name))
                .findFirst();
    }
}
