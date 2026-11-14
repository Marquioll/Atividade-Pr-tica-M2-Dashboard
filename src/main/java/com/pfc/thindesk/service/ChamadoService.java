package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Chamado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.pfc.thindesk.repository.ChamadoRepository;

@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;

    public Chamado criarChamado(Chamado chamado) {
        chamado.setCriadoEm(java.time.Instant.now());
        chamado.setConcluidoEm("CONCLUIDO".equals(chamado.getStatus()) ? chamado.getCriadoEm() : null);
        return chamadoRepository.save(chamado);
    }

    public List<Chamado> listarChamados() {
        return chamadoRepository.findAll();
    }

    public Chamado atualizarChamado(String id, Chamado chamadoAtualizado) {
        return chamadoRepository.findById(id)
                .map(chamado -> {
                    chamado.setDescricao(chamadoAtualizado.getDescricao());
                    if (!"CONCLUIDO".equals(chamadoAtualizado.getStatus())) {
                        chamado.setConcluidoEm(null);
                    } else if (!"CONCLUIDO".equals(chamado.getStatus())) {
                        chamado.setConcluidoEm(java.time.Instant.now());
                    }
                    chamado.setStatus(chamadoAtualizado.getStatus());
                    chamado.setTipo(chamadoAtualizado.getTipo());
                    chamado.setTecnico(chamadoAtualizado.getTecnico());
                    chamado.setUsuario(chamadoAtualizado.getUsuario());
                    return chamadoRepository.save(chamado);
                })
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado com id: " + id));
    }

}
