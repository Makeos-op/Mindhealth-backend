package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.G6_MH_MetaBienestarRequestDTO;
import com.upc.mind_health.entities.G6_MH_ContenidoTerapeutico;
import com.upc.mind_health.entities.G6_MH_MetaBienestar;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_ContenidoTerapeuticoRepository;
import com.upc.mind_health.repositories.G6_MH_MetaBienestarRepository;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_BienestarCatalogoService {

    private final G6_MH_ContenidoTerapeuticoRepository contenidoRepository;
    private final G6_MH_MetaBienestarRepository metaRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;

    // 🌟 HU-42 ESCENARIO 1 y 2: Filtrar recursos de relajación por categoría emocional
    @Transactional(readOnly = true)
    public List<G6_MH_ContenidoTerapeutico> filtrarEjerciciosPorCategoria(String categoria) {
        return contenidoRepository.findByEmocionAsociadaContainingIgnoreCase(categoria)
                .stream()
                .filter(c -> "EJERCICIO".equalsIgnoreCase(c.getTipo()) || "VIDEO".equalsIgnoreCase(c.getTipo()))
                .collect(Collectors.toList());
    }

    // 🌟 HU-43 ESCENARIO 1: Agregar una nueva meta diaria con validaciones de longitud
    @Transactional
    public G6_MH_MetaBienestar registrarMetaDiaria(G6_MH_MetaBienestarRequestDTO dto) {
        if (dto.getNombreMeta() == null || dto.getNombreMeta().trim().length() < 3 || dto.getNombreMeta().trim().length() > 30) {
            throw new RuntimeException("El nombre de la meta debe tener obligatoriamente entre 3 y 30 caracteres.");
        }

        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        G6_MH_MetaBienestar nuevaMeta = G6_MH_MetaBienestar.builder()
                .usuario(usuario)
                .nombreMeta(dto.getNombreMeta().trim())
                .completada(false) // Por defecto: Pendiente
                .fechaRegistro(LocalDate.now())
                .build();

        return metaRepository.save(nuevaMeta);
    }

    // 🌟 HU-43 ESCENARIO 2: Actualizar el estado de la meta a completada (Checkbox)
    @Transactional
    public String conmutarEstadoMeta(Long idMeta, boolean completada) {
        G6_MH_MetaBienestar meta = metaRepository.findById(idMeta)
                .orElseThrow(() -> new RuntimeException("La meta especificada no existe."));

        meta.setCompletada(completada);
        metaRepository.save(meta);
        return "Estado de la meta actualizado correctamente en la base de datos.";
    }

    // 🌟 HU-43 ESCENARIO 3: Limpieza y eliminación física de una meta
    @Transactional
    public String eliminarMetaDiaria(Long idMeta) {
        G6_MH_MetaBienestar meta = metaRepository.findById(idMeta)
                .orElseThrow(() -> new RuntimeException("La meta que intenta eliminar no existe."));

        metaRepository.delete(meta);
        return "La meta ha sido eliminada de la base de datos de forma segura.";
    }

    // 🌟 HU-43: Listar metas del usuario
    @Transactional(readOnly = true)
    public List<G6_MH_MetaBienestar> listarMetasUsuario(Long idUsuario) {
        return metaRepository.findByUsuarioIdUsuarioOrderByFechaRegistroDesc(idUsuario);
    }
}