package client.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.CryptoUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositório de secrets de usuários do lado do cliente.
 * Responsável por salvar, carregar e consultar os segredos TOTP armazenados localmente.
 */
public class UsersSecretsRepository {

    private static final String FILE_PATH = "src/main/resources/client/users-secrets.txt";
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Obtém a lista completa de segredos de usuários armazenados localmente.
     * @return Lista de objetos {@link UserSecret}. Se o arquivo não existir, retorna uma lista vazia.
     */
    public static List<UserSecret> SelectAll() {
        try {
            File f = new File(FILE_PATH);
            if (!f.exists() || f.length() == 0) return new ArrayList<>();

            String encryptedData = Files.readString(f.toPath());
            if (encryptedData.trim().isEmpty()) return new ArrayList<>();

            String jsonData = CryptoUtils.DecryptFileData(encryptedData);
            return mapper.readValue(jsonData, new TypeReference<>() {});
        } catch (Exception e) {
            System.out.println("Erro ao ler ou descriptografar o repositório de segredos de usuário: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Salva a lista de segredos de usuários localmente.
     * @param usersSecrets Lista de {@link UserSecret} a ser salva.
     */
    public static void Save(List<UserSecret> usersSecrets) {
        try {
            String jsonData = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(usersSecrets);
            String encryptedData = CryptoUtils.EncryptFileData(jsonData);
            Files.writeString(new File(FILE_PATH).toPath(), encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar segredos de usuário criptografados", e);
        }
    }

    /**
     * Adiciona ou atualiza uma user-secret local.
     * Se já existir uma user-secret com o mesmo nome, ela será substituída.
     * @param userSecret Objeto {@link UserSecret} a ser adicionado ou atualizado.
     */
    public static void Add(UserSecret userSecret) {
        List<UserSecret> usersSecrets = SelectAll();
        usersSecrets.removeIf(existing -> existing.Name.equalsIgnoreCase(userSecret.Name));
        usersSecrets.add(userSecret);
        Save(usersSecrets);
    }

    /**
     * Busca uma user-secret pelo nome do usuário (case-insensitive).
     * @param name Nome do usuário cuja secret será buscada.
     * @return Um {@link Optional} com a user-secret correspondente, ou {@link Optional#empty()} se não for encontrada.
     */
    public static Optional<UserSecret> SelectByName(String name) {
        return SelectAll().stream()
            .filter(userSecret -> userSecret.Name.equalsIgnoreCase(name))
            .findFirst();
    }
}
