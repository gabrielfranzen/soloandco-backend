package repository.utilitarios;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import repository.providers.annotations.GoogleStorage;
import repository.providers.api.ArmazenamentoArquivoApi;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class ArmazenamentoRepository {

	private static final Logger LOGGER = Logger.getLogger(ArmazenamentoRepository.class.getName());

	public static final String STORAGE_GOOGLE = "google";

	private ArmazenamentoArquivoApi armazenamentoArquivoApi;

	@Inject
	@GoogleStorage
	private ArmazenamentoArquivoApi apiGoogleCloud;

	@Inject
	@ConfigProperty(name = "storage.tipo.soloandco", defaultValue = STORAGE_GOOGLE)
	private String tipoStorage;

	@PostConstruct
	private void postConstruct() {
		if (STORAGE_GOOGLE.equalsIgnoreCase(tipoStorage)) {
			armazenamentoArquivoApi = apiGoogleCloud;
		} else {
			LOGGER.log(Level.SEVERE, "Propriedade storage.tipo.soloandco inválida: {0}", tipoStorage);
		}
	}

	public String upload(String nomeArquivo, byte[] arquivo, String contentType) {
		return armazenamentoArquivoApi.upload(nomeArquivo, arquivo, contentType);
	}

	public byte[] download(String uuid) {
		return armazenamentoArquivoApi.download(uuid);
	}

	public boolean existe(String uuid) {
		return armazenamentoArquivoApi.existe(uuid);
	}

	public String obterContentType(String uuid) {
		return armazenamentoArquivoApi.obterContentType(uuid);
	}
}

