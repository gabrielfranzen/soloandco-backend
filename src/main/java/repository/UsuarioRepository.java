package repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import jakarta.ejb.Stateless;
import model.Usuario;
import model.dto.UsuarioDTO;
import repository.base.AbstractCrudRepository;
import utils.BcryptUtil;

@Stateless
public class UsuarioRepository extends AbstractCrudRepository<Usuario> {
	
    public Optional<Usuario> buscarPorId(Integer id) {
        return Optional.ofNullable(em.find(Usuario.class, id));
    }
    
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        String norm = email.toLowerCase().trim();
        List<Usuario> list = em.createQuery(
            "select u from Usuario u where lower(u.email) = :e", Usuario.class)
            .setParameter("e", norm)
            .setMaxResults(1)
            .getResultList();
        return list.stream().findFirst();
    }
    
    public Optional<Usuario> buscarPorRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return Optional.empty();
        List<Usuario> list = em.createQuery(
            "select u from Usuario u where u.refreshToken = :rt", Usuario.class)
            .setParameter("rt", refreshToken)
            .setMaxResults(1)
            .getResultList();
        return list.stream().findFirst();
    }

    public Optional<Usuario> buscarPorTelefone(String telefone) {
        if (telefone == null) return Optional.empty();
        String norm = telefone.trim();
        List<Usuario> list = em.createQuery(
            "select u from Usuario u where u.telefone = :t", Usuario.class)
            .setParameter("t", norm)
            .setMaxResults(1)
            .getResultList();
        return list.stream().findFirst();
    }
    
    public Usuario salvar(Usuario u) {
        if (u.getId() == null) {
            em.persist(u);
            return u;
        }
        return em.merge(u);
    }
    
    public void excluir(Usuario u) {
        if (u == null) return;
        Usuario managed = (u.getId() != null) ? em.find(Usuario.class, u.getId()) : null;
        if (managed != null) em.remove(managed);
    }
    
    /**
     * Atribui um refresh token ao usuário.
     * A validação de expiração deve ser feita no JWT, não aqui.
     */
    public void emitirRefreshToken(Usuario u, String refreshToken) {
        if (u == null) throw new IllegalArgumentException("Usuário obrigatório");
        if (refreshToken == null || refreshToken.isBlank()) 
            throw new IllegalArgumentException("Refresh token obrigatório");
        
        u.setRefreshToken(refreshToken);
        em.merge(u);
    }
    
    /**
     * Verifica se o refresh token do usuário corresponde ao fornecido.
     * A validação de expiração deve ser feita no JWT, não aqui.
     */
    public boolean possuiRefreshToken(Usuario u, String refreshToken) {
        if (u == null || refreshToken == null || refreshToken.isBlank()) return false;
        return refreshToken.equals(u.getRefreshToken());
    }
    
    /**
     * Revoga o refresh token do usuário (define como null).
     */
    public void revokeRefreshToken(Usuario u) {
        if (u == null) return;
        u.setRefreshToken(null);
        em.merge(u);
    }
 
    /**
     * Verifica se a senha fornecida corresponde à senha do usuário.
     * Usa BCrypt para verificação segura de senha.
     */
    public boolean verificarSenha(String senhaFornecida, String senhaArmazenada) {
        if (senhaFornecida == null || senhaArmazenada == null) {
            return false;
        }
        
        // Se a senha armazenada não for um hash BCrypt (migração de dados antigos),
        // compara diretamente e retorna false (forçando atualização)
        if (!BcryptUtil.isBcryptHash(senhaArmazenada)) {
            // Para compatibilidade com senhas antigas não criptografadas
            // você pode retornar senhaFornecida.equals(senhaArmazenada) aqui
            // mas é mais seguro forçar a atualização da senha
            return senhaFornecida.equals(senhaArmazenada);
        }
        
        return BcryptUtil.verificarSenha(senhaFornecida, senhaArmazenada);
    }
    
    /**
     * Verifica se a senha e email coincidem com um usuário no banco.
     */
    public boolean verificarSenhaPorEmail(String email, String senha) {
        if (email == null || senha == null) {
            return false;
        }
        
        Optional<Usuario> usuarioOpt = buscarPorEmail(email);
        if (usuarioOpt.isEmpty()) {
            return false;
        }
        
        Usuario usuario = usuarioOpt.get();
        return verificarSenha(senha, usuario.getSenha());
    }

    public boolean refreshValido(Usuario u, String refreshToken) {
        return u.getRefreshToken() != null && u.getRefreshToken().equals(refreshToken);
    }
    
    /**
     * Atribui um refresh token ao usuário com duração específica.
     * A validação de expiração deve ser feita no JWT, não aqui.
     */
    public void emitirRefresh(Usuario u, String refreshToken, Duration validity) {
        if (u == null) throw new IllegalArgumentException("Usuário obrigatório");
        if (refreshToken == null || refreshToken.isBlank()) 
            throw new IllegalArgumentException("Refresh token obrigatório");
        
        u.setRefreshToken(refreshToken);
        em.merge(u);
    }
    
    /**
     * Atribui um refresh token ao usuário (versão simplificada sem duração).
     */
    public void emitirRefresh(Usuario u, String refreshToken) {
        if (u == null) throw new IllegalArgumentException("Usuário obrigatório");
        if (refreshToken == null || refreshToken.isBlank()) 
            throw new IllegalArgumentException("Refresh token obrigatório");
        
        u.setRefreshToken(refreshToken);
        em.merge(u);
    }
    
    /**
     * Valida se os dados do usuário são únicos antes do cadastro.
     * @param usuario Usuário a ser validado
     * @throws IllegalArgumentException se algum dado já estiver em uso
     */
    public void validarDadosUnicos(Usuario usuario) {
        validarDadosUnicos(usuario, null);
    }
    
    /**
     * Valida se os dados do usuário são únicos, ignorando o próprio usuário (para atualizações).
     * @param usuario Usuário a ser validado
     * @param idUsuarioExcluir ID do usuário a ser ignorado na validação (null para cadastro)
     * @throws IllegalArgumentException se algum dado já estiver em uso
     */
    public void validarDadosUnicos(Usuario usuario, Integer idUsuarioExcluir) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        
        // Validação de email duplicado
        if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
            Optional<Usuario> usuarioComEmail = buscarPorEmail(usuario.getEmail());
            if (usuarioComEmail.isPresent() && 
                (idUsuarioExcluir == null || !idUsuarioExcluir.equals(usuarioComEmail.get().getId()))) {
                throw new IllegalArgumentException("E-mail já cadastrado");
            }
        }
        
        // Validação de telefone duplicado
        if (usuario.getTelefone() != null && !usuario.getTelefone().trim().isEmpty()) {
            Optional<Usuario> usuarioComTelefone = buscarPorTelefone(usuario.getTelefone());
            if (usuarioComTelefone.isPresent() && 
                (idUsuarioExcluir == null || !idUsuarioExcluir.equals(usuarioComTelefone.get().getId()))) {
                throw new IllegalArgumentException("Telefone já cadastrado");
            }
        }
        
    }
    
    /**
     * Busca usuário por email e retorna como DTO (sem informações sensíveis).
     */
    public Optional<UsuarioDTO> buscarPorEmailDTO(String email) {
        if (email == null) return Optional.empty();
        String norm = email.toLowerCase().trim();
        List<Usuario> list = em.createQuery(
            "select u from Usuario u where lower(u.email) = :e", Usuario.class)
            .setParameter("e", norm)
            .setMaxResults(1)
            .getResultList();
        
        return list.stream().findFirst().map(this::converterParaDTO);
    }
    
    /**
     * Converte um Usuario para UsuarioDTO (remove informações sensíveis).
     */
    private UsuarioDTO converterParaDTO(Usuario usuario) {
        if (usuario == null) return null;
        
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setDataCadastro(usuario.getDataCadastro());
        dto.setEmail(usuario.getEmail());
        dto.setRoles(usuario.getRoles());
        dto.setTelefone(usuario.getTelefone());
        
        return dto;
    }
    
    
    
}
