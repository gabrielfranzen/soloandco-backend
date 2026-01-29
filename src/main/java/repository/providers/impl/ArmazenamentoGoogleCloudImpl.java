package repository.providers.impl;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import repository.providers.annotations.GoogleStorage;
import repository.providers.api.ArmazenamentoArquivoApi;

import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@GoogleStorage
public class ArmazenamentoGoogleCloudImpl implements ArmazenamentoArquivoApi {

	@Inject
	@ConfigProperty(name = "google.storage.bucket")
	private String bucketName;

	@Inject
	@ConfigProperty(name = "google.storage.credentials.path")
	private String credenciaisPath;

	private volatile Storage storage;

	private Storage getStorage() {
		if (storage == null) {
			synchronized (this) {
				if (storage == null) {
					storage = criarStorage();
				}
			}
		}
		return storage;
	}

	private Storage criarStorage() {
		validarConfiguracao();
		try (InputStream credenciais = abrirCredenciais()) {
			return StorageOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(credenciais))
				.build()
				.getService();
		} catch (IOException e) {
			throw new IllegalStateException("Não foi possível carregar as credenciais do Google Storage", e);
		}
	}

	private void validarConfiguracao() {
		if (bucketName == null || bucketName.isBlank()) {
			throw new IllegalStateException("Propriedade google.storage.bucket não configurada");
		}
	}

	private InputStream abrirCredenciais() throws IOException {
		if (credenciaisPath != null && !credenciaisPath.isBlank()) {
			return Files.newInputStream(Path.of(credenciaisPath));
		}
		InputStream recurso = getClass().getResourceAsStream("/google-storage-key.json");
		if (recurso == null) {
			throw new IllegalStateException("Arquivo de credenciais google-storage-key.json não encontrado");
		}
		return recurso;
	}

	@Override
	public String upload(String nomeArquivo, byte[] arquivo, String contentType) {
		if (arquivo == null || arquivo.length == 0) {
			throw new IllegalArgumentException("Arquivo é obrigatório");
		}

		String uuid = UUID.randomUUID().toString();
		BlobId blobId = BlobId.of(bucketName, uuid);
		BlobInfo.Builder builder = BlobInfo.newBuilder(blobId)
			.setMetadata(criarMetadata(nomeArquivo));

		if (contentType != null && !contentType.isBlank()) {
			builder.setContentType(contentType);
		}

		getStorage().create(builder.build(), arquivo);
		return uuid;
	}

	@Override
	public byte[] download(String uuid) {
		if (uuid == null || uuid.isBlank()) {
			return null;
		}
		Blob blob = getStorage().get(BlobId.of(bucketName, uuid));
		if (blob == null || !blob.exists()) {
			return null;
		}
		return blob.getContent();
	}

	@Override
	public boolean existe(String uuid) {
		if (uuid == null || uuid.isBlank()) {
			return false;
		}
		Blob blob = getStorage().get(BlobId.of(bucketName, uuid));
		return blob != null && blob.exists();
	}

	@Override
	public String obterContentType(String uuid) {
		if (uuid == null || uuid.isBlank()) {
			return null;
		}
		Blob blob = getStorage().get(BlobId.of(bucketName, uuid));
		if (blob == null || !blob.exists()) {
			return null;
		}
		return blob.getContentType();
	}

	private Map<String, String> criarMetadata(String nomeArquivo) {
		Map<String, String> metadata = new HashMap<>();
		if (nomeArquivo != null && !nomeArquivo.isBlank()) {
			metadata.put("nomeOriginal", nomeArquivo);
		}
		return metadata;
	}
}

