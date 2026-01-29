package repository.providers.api;

public interface ArmazenamentoArquivoApi {
	String upload(String nomeArquivo, byte[] arquivo, String contentType);

	byte[] download(String uuid);

	boolean existe(String uuid);

	String obterContentType(String uuid);
}

