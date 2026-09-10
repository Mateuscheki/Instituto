package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.audit.Auditar;
import edu.unialfa.institutoMario.audit.LogAuditoriaService;
import edu.unialfa.institutoMario.audit.TipoAcao;
import edu.unialfa.institutoMario.model.Documento;
import edu.unialfa.institutoMario.model.Projetos;
import edu.unialfa.institutoMario.repository.DocumentoRepository;
import edu.unialfa.institutoMario.repository.ProjetoRepository;
import jakarta.transaction.Transactional;
// import lombok.AllArgsConstructor; <--- REMOVER
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final DocumentoRepository documentoRepository;
    private final LogAuditoriaService logAuditoriaService;

    public ProjetoService(ProjetoRepository projetoRepository, DocumentoRepository documentoRepository,
                           LogAuditoriaService logAuditoriaService) {
        this.projetoRepository = projetoRepository;
        this.documentoRepository = documentoRepository;
        this.logAuditoriaService = logAuditoriaService;
    }

    @Transactional
    @Auditar(acao = TipoAcao.CRIACAO, entidade = "Projeto")
    public void salvar(Projetos projeto){
        projetoRepository.save(projeto);
    }

    public List<Projetos> listarTodos(){
        return projetoRepository.findAll();
    }

    public Projetos bucarPorId(Long id){
        return projetoRepository.findById(id).orElseThrow(() -> new RuntimeException("Projeto não encontrado"));
    }

    @Transactional
    @Auditar(acao = TipoAcao.EXCLUSAO, entidade = "Projeto")
    public void deletarPorId(Long id) {
        if (!projetoRepository.existsById(id)) {
            throw new RuntimeException("Projeto com id " + id + " não encontrado para exclusão.");
        }

        projetoRepository.deleteById(id);
    }


    @Transactional
    public Long deletarDocumento(Long idDocumento, String uploadDir) {
        // ... (o restante do código do método deletarDocumento permanece o mesmo)
        Documento doc = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado."));

        Long projetoId = doc.getProjeto().getId();
        String nomeArquivo = doc.getUrlArquivo();

        try {
            Path caminhoArquivo = Paths.get(uploadDir, nomeArquivo);
            Files.deleteIfExists(caminhoArquivo);
        } catch (IOException e) {
            System.err.println("Erro ao deletar arquivo físico: " + nomeArquivo + " | Erro: " + e.getMessage());
        }

        documentoRepository.delete(doc);

        logAuditoriaService.registrar(TipoAcao.EXCLUSAO, "Documento do Projeto", idDocumento,
                "Documento excluído(a) (ID " + idDocumento + ") do Projeto (ID " + projetoId + ")");

        return projetoId;
    }
}