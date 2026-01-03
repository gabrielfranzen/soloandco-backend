package repository;

import jakarta.ejb.Stateless;
import org.eclipse.microprofile.config.ConfigProvider;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Repository para gerenciar o armazenamento de arquivos no storage local.
 * Simula um sistema de cloud storage salvando arquivos fisicamente em disco.
 */
@Stateless
public class StorageRepository {
    
    private static final Logger LOGGER = Logger.getLogger(StorageRepository.class.getName());
    
    /**
     * Obtém o caminho do storage a partir das configurações.
     * @return Caminho do storage
     */
    private String obterCaminhoStorage() {
        try {
            Optional<String> storagePath = ConfigProvider.getConfig()
                .getOptionalValue("storage", String.class);
            
            if (storagePath.isPresent() && !storagePath.get().trim().isEmpty()) {
                return storagePath.get();
            }
            
            // Fallback para valor padrão
            String caminhoDefault = "C:\\Storage";
            LOGGER.warning("Propriedade 'storage' não encontrada nas configurações. Usando padrão: " + caminhoDefault);
            return caminhoDefault;
        } catch (Exception e) {
            // Se falhar ao ler configuração, usa valor padrão
            String caminhoDefault = "C:\\Storage";
            LOGGER.warning("Erro ao ler propriedade 'storage': " + e.getMessage() + ". Usando padrão: " + caminhoDefault);
            return caminhoDefault;
        }
    }
    
    /**
     * Salva um arquivo no storage.
     * Gera um hash único para o arquivo e o salva com esse nome.
     * 
     * @param conteudoBase64 Conteúdo do arquivo em base64
     * @param extensao Extensão do arquivo (ex: "png", "jpg")
     * @return Nome do arquivo salvo (hash + extensão)
     * @throws IOException Se houver erro ao salvar o arquivo
     */
    public String salvarArquivo(String conteudoBase64, String extensao) throws IOException {
        try {
            // Remove o prefixo data:image/...;base64, se existir
            String base64Limpo = limparBase64(conteudoBase64);
            
            // Decodifica o base64 para bytes
            byte[] bytes = Base64.getDecoder().decode(base64Limpo);
            
            // Gera o hash do conteúdo
            String hash = gerarHash(bytes);
            
            // Define o nome do arquivo (hash + extensão)
            String nomeArquivo = hash + "." + extensao.toLowerCase();
            
            // Obtém o caminho do storage
            String storagePath = obterCaminhoStorage();
            
            // Cria o diretório de storage se não existir
            Path dirStorage = Paths.get(storagePath);
            if (!Files.exists(dirStorage)) {
                Files.createDirectories(dirStorage);
                LOGGER.info("Diretório de storage criado: " + storagePath);
            }
            
            // Salva o arquivo
            Path caminhoArquivo = dirStorage.resolve(nomeArquivo);
            Files.write(caminhoArquivo, bytes);
            
            LOGGER.info("Arquivo salvo no storage: " + nomeArquivo);
            return nomeArquivo;
            
        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe("Erro ao gerar hash do arquivo: " + e.getMessage());
            throw new IOException("Erro ao gerar hash do arquivo", e);
        }
    }
    
    /**
     * Busca um arquivo no storage e retorna seu conteúdo em base64.
     * 
     * @param nomeArquivo Nome do arquivo no storage
     * @return Conteúdo do arquivo em base64, ou null se não encontrado
     */
    public String buscarArquivo(String nomeArquivo) {
        try {
            if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
                return null;
            }
            
            String storagePath = obterCaminhoStorage();
            Path caminhoArquivo = Paths.get(storagePath).resolve(nomeArquivo);
            
            if (!Files.exists(caminhoArquivo)) {
                LOGGER.warning("Arquivo não encontrado no storage: " + nomeArquivo);
                return null;
            }
            
            byte[] bytes = Files.readAllBytes(caminhoArquivo);
            return Base64.getEncoder().encodeToString(bytes);
            
        } catch (IOException e) {
            LOGGER.severe("Erro ao buscar arquivo do storage: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Busca um arquivo no storage e retorna seus bytes.
     * 
     * @param nomeArquivo Nome do arquivo no storage
     * @return Bytes do arquivo, ou null se não encontrado
     */
    public byte[] buscarArquivoBytes(String nomeArquivo) {
        try {
            if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
                return null;
            }
            
            String storagePath = obterCaminhoStorage();
            Path caminhoArquivo = Paths.get(storagePath).resolve(nomeArquivo);
            
            if (!Files.exists(caminhoArquivo)) {
                LOGGER.warning("Arquivo não encontrado no storage: " + nomeArquivo);
                return null;
            }
            
            return Files.readAllBytes(caminhoArquivo);
            
        } catch (IOException e) {
            LOGGER.severe("Erro ao buscar arquivo do storage: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Deleta um arquivo do storage.
     * 
     * @param nomeArquivo Nome do arquivo a ser deletado
     * @return true se o arquivo foi deletado, false caso contrário
     */
    public boolean deletarArquivo(String nomeArquivo) {
        try {
            if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
                return false;
            }
            
            String storagePath = obterCaminhoStorage();
            Path caminhoArquivo = Paths.get(storagePath).resolve(nomeArquivo);
            
            if (!Files.exists(caminhoArquivo)) {
                LOGGER.warning("Arquivo não encontrado para deletar: " + nomeArquivo);
                return false;
            }
            
            Files.delete(caminhoArquivo);
            LOGGER.info("Arquivo deletado do storage: " + nomeArquivo);
            return true;
            
        } catch (IOException e) {
            LOGGER.severe("Erro ao deletar arquivo do storage: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica se um arquivo existe no storage.
     * 
     * @param nomeArquivo Nome do arquivo
     * @return true se o arquivo existe, false caso contrário
     */
    public boolean arquivoExiste(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            return false;
        }
        
        String storagePath = obterCaminhoStorage();
        Path caminhoArquivo = Paths.get(storagePath).resolve(nomeArquivo);
        return Files.exists(caminhoArquivo);
    }
    
    /**
     * Gera um hash SHA-256 do conteúdo do arquivo.
     * 
     * @param bytes Conteúdo do arquivo em bytes
     * @return Hash hexadecimal
     * @throws NoSuchAlgorithmException Se o algoritmo SHA-256 não estiver disponível
     */
    private String gerarHash(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(bytes);
        
        // Converte para hexadecimal
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    /**
     * Remove o prefixo data:image/...;base64, do conteúdo base64.
     * 
     * @param base64 Conteúdo em base64
     * @return Base64 limpo
     */
    private String limparBase64(String base64) {
        if (base64 == null) {
            return null;
        }
        
        // Remove espaços e quebras de linha
        String limpo = base64.replaceAll("\\s", "");
        
        // Remove o prefixo data:...;base64, se existir
        if (limpo.contains(",")) {
            int indiceVirgula = limpo.indexOf(",");
            limpo = limpo.substring(indiceVirgula + 1);
        }
        
        return limpo;
    }
    
    /**
     * Extrai a extensão de um nome de arquivo base64 ou nome de arquivo comum.
     * 
     * @param nomeArquivoOuBase64 Nome do arquivo ou string base64
     * @return Extensão do arquivo (sem ponto), ou "png" como padrão
     */
    public String extrairExtensao(String nomeArquivoOuBase64) {
        if (nomeArquivoOuBase64 == null || nomeArquivoOuBase64.trim().isEmpty()) {
            return "png";
        }
        
        // Verifica se é base64 com prefixo data:
        if (nomeArquivoOuBase64.startsWith("data:")) {
            // Extrai o tipo MIME
            int pontoVirgula = nomeArquivoOuBase64.indexOf(";");
            if (pontoVirgula > 0) {
                String mimeType = nomeArquivoOuBase64.substring(5, pontoVirgula);
                // Extrai a extensão do MIME type (ex: image/png -> png)
                if (mimeType.contains("/")) {
                    return mimeType.substring(mimeType.lastIndexOf("/") + 1);
                }
            }
        }
        
        // Tenta extrair extensão de nome de arquivo comum
        if (nomeArquivoOuBase64.contains(".")) {
            int ultimoPonto = nomeArquivoOuBase64.lastIndexOf(".");
            return nomeArquivoOuBase64.substring(ultimoPonto + 1).toLowerCase();
        }
        
        // Padrão
        return "png";
    }
}
