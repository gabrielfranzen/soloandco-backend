package utils.email;

public class TemplateEmailRecuperacaoDeSenha {

	// Template HTML para e-mail de recuperação de senha
	public static String gerarEmailRecuperacaoSenha(String nomeUsuario, String linkRecuperacao) {
		return "<!DOCTYPE html>" +
			"<html lang='pt-BR'>" +
			"<head>" +
			"    <meta charset='UTF-8'>" +
			"    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
			"    <title>Recuperação de Senha - Solo & Co</title>" +
			"    <style>" +
			"        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 0; }" +
			"        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
			"        .header { background-color: #2c3e50; padding: 30px; text-align: center; }" +
			"        .header h1 { color: #ffffff; margin: 0; font-size: 28px; }" +
			"        .content { padding: 30px; }" +
			"        .content h2 { color: #2c3e50; margin-top: 0; }" +
			"        .content p { margin: 15px 0; color: #555; }" +
			"        .btn-container { text-align: center; margin: 30px 0; }" +
			"        .btn { display: inline-block; padding: 15px 30px; background-color: #3498db; color: #ffffff !important; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px; }" +
			"        .btn:hover { background-color: #2980b9; }" +
			"        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
			"        .warning strong { color: #856404; }" +
			"        .security-note { background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
			"        .security-note strong { color: #721c24; }" +
			"        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #6c757d; }" +
			"        .footer p { margin: 5px 0; }" +
			"    </style>" +
			"</head>" +
			"<body>" +
			"    <div class='container'>" +
			"        <div class='header'>" +
			"            <h1>Solo & Co</h1>" +
			"        </div>" +
			"        <div class='content'>" +
			"            <h2>Olá" + (nomeUsuario != null && !nomeUsuario.isBlank() ? ", " + nomeUsuario : "") + "!</h2>" +
			"            <p>Recebemos uma solicitação para redefinir a senha da sua conta no <strong>Solo & Co</strong>.</p>" +
			"            <p>Se você fez essa solicitação, clique no botão abaixo para criar uma nova senha:</p>" +
			"            <div class='btn-container'>" +
			"                <a href='" + linkRecuperacao + "' class='btn'>Redefinir Minha Senha</a>" +
			"            </div>" +
			"            <div class='warning'>" +
			"                <strong>Atenção:</strong> Este link é válido por apenas <strong>30 minutos</strong> e pode ser usado apenas uma vez." +
			"            </div>" +
			"            <div class='security-note'>" +
			"                <strong>Segurança:</strong>" +
			"                <ul style='margin: 10px 0; padding-left: 20px;'>" +
			"                    <li><strong>NÃO compartilhe este link com ninguém!</strong></li>" +
			"                    <li>Se você não solicitou a recuperação de senha, ignore este e-mail.</li>" +
			"                    <li>Nesse caso, sua senha permanecerá segura e inalterada.</li>" +
			"                </ul>" +
			"            </div>" +
			"        </div>" +
			"        <div class='footer'>" +
			"            <p>Este é um e-mail automático, por favor não responda.</p>" +
			"            <p>&copy; " + java.time.Year.now().getValue() + " Solo & Co. Todos os direitos reservados.</p>" +
			"        </div>" +
			"    </div>" +
			"</body>" +
			"</html>";
	}
}

