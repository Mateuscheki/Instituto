package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.audit.Auditar;
import edu.unialfa.institutoMario.audit.TipoAcao;
import edu.unialfa.institutoMario.model.Professor;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.repository.ProfessorRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProfessorService {
    private final ProfessorRepository repository;

    @Transactional
    @Auditar(acao = TipoAcao.CRIACAO, entidade = "Professor")
    public void salvar(Professor professor) {
        repository.save(professor);
    }

    public List<Professor> listarTodos() {
        return repository.findAll();
    }

    public Professor buscarPorId(Long id) {
        return repository.findById(id).get();
    }

    @Auditar(acao = TipoAcao.EXCLUSAO, entidade = "Professor")
    public void deletarPorId(Long id) {
        repository.deleteById(id);
    }

    public boolean existsByUsuarioId(Long usuarioId) {
        return repository.existsByUsuarioId(usuarioId);
    }

    public void deletarPorUsuarioId(Long usuarioId) {
        repository.findByUsuarioId(usuarioId)
                .ifPresent(repository::delete);
    }

    public Professor buscarPorUsuario(Usuario usuario) {
        return repository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado para o usuário logado"));
    }

}
