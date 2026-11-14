package com.pfc.thindesk.controller;

import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.repository.ChamadoRepository;
import java.time.*;
import java.util.*;
import java.util.stream.*;
import org.springframework.dao.DataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class DashboardController {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private final ChamadoRepository chamados;

    public DashboardController(ChamadoRepository chamados) {
        this.chamados = chamados;
    }

    @GetMapping("/admin/dashboard")
    public String pagina() {
        return "dashboard";
    }

    @GetMapping("/api/dashboard/filtros")
    @ResponseBody
    public Map<String, List<String>> filtros() {
        var base = chamados.findAll();
        return Map.of("status", valores(base, Chamado::getStatus),
                      "tipos", valores(base, Chamado::getTipo));
    }

    @GetMapping("/api/dashboard/resumo")
    @ResponseBody
    public ResponseEntity<?> resumo(
            @RequestParam(name="inicio", required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name="fim", required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(name="status", required=false) String status,
            @RequestParam(name="tipo", required=false) String tipo) {
        if ((inicio == null) != (fim == null) ||
                (inicio != null && (inicio.isAfter(fim) || inicio.plusDays(365).isBefore(fim)))) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe um intervalo valido de ate 366 dias."));
        }
        var base = chamados.findAll().stream()
                .filter(c -> status == null || status.isBlank() || status.equals(rotulo(c.getStatus())))
                .filter(c -> tipo == null || tipo.isBlank() || tipo.equals(rotulo(c.getTipo())))
                .toList();
        var abertosNoPeriodo = base.stream().filter(c -> contem(c.getCriadoEm(), inicio, fim)).toList();
        var atendidos = base.stream().filter(c -> "CONCLUIDO".equals(c.getStatus()))
                .filter(c -> c.getConcluidoEm() != null && contem(c.getConcluidoEm(), inicio, fim)).toList();
        var situacoes = abertosNoPeriodo.stream().collect(Collectors.groupingBy(
                c -> rotulo(c.getStatus()), TreeMap::new, Collectors.counting()));
        var serie = new TreeMap<LocalDate, Long>();
        if (inicio != null) {
            for (LocalDate dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
                serie.put(dia, 0L);
            }
        }
        atendidos.forEach(c -> serie.merge(data(c.getConcluidoEm()), 1L, Long::sum));
        var ranking = atendidos.stream().collect(Collectors.groupingBy(
                c -> rotulo(c.getTipo()), Collectors.counting())).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(10).map(e -> new TipoQuantidade(e.getKey(), e.getValue())).toList();
        return ResponseEntity.ok(Map.of("total", abertosNoPeriodo.size(), "atendidos", atendidos.size(),
                "situacoes", situacoes, "serie", serie, "ranking", ranking,
                "semDataCriacao", base.stream().filter(c -> c.getCriadoEm() == null).count(),
                "semDataConclusao", base.stream().filter(c -> "CONCLUIDO".equals(c.getStatus())
                        && c.getConcluidoEm() == null).count()));
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseBody
    public ResponseEntity<?> bancoIndisponivel() {
        return ResponseEntity.status(503).body(Map.of("erro", "Banco de dados indisponivel."));
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<?> parametroInvalido() {
        return ResponseEntity.badRequest().body(Map.of("erro", "Use datas no formato AAAA-MM-DD."));
    }

    private static LocalDate data(Instant instante) {
        return instante.atZone(ZONA).toLocalDate();
    }

    private static boolean contem(Instant instante, LocalDate inicio, LocalDate fim) {
        return inicio == null || (instante != null && !data(instante).isBefore(inicio) && !data(instante).isAfter(fim));
    }

    private static String rotulo(String valor) {
        return valor == null || valor.isBlank() ? "Sem classificacao" : valor.strip();
    }

    private static List<String> valores(List<Chamado> base, java.util.function.Function<Chamado, String> campo) {
        return base.stream().map(campo).map(DashboardController::rotulo).distinct().sorted().toList();
    }

    public record TipoQuantidade(String tipo, long quantidade) {}
}
