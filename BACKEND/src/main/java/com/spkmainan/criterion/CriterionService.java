package com.spkmainan.criterion;

import com.spkmainan.ahp.SawEngine;
import com.spkmainan.common.exception.BadRequestException;
import com.spkmainan.common.exception.ConflictException;
import com.spkmainan.common.exception.ResourceNotFoundException;
import com.spkmainan.criterion.CriterionDto.CreateRequest;
import com.spkmainan.criterion.CriterionDto.Response;
import com.spkmainan.criterion.CriterionDto.UpdateRequest;
import com.spkmainan.domain.CriterionType;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriterionService {

    private final CriterionRepository repository;

    public CriterionService(CriterionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Response> findAll() {
        return repository.findAllByOrderByNoAsc().stream().map(Response::from).toList();
    }

    @Transactional
    public Response create(CreateRequest request) {
        CriterionType type = parseType(request.type());
        int no = repository.maxNo() + 1;
        CriterionEntity c = new CriterionEntity(
            uniqueCode(request.name()), no, request.name(), type,
            request.description(), request.abbr(), true);
        return Response.from(repository.save(c));
    }

    @Transactional
    public Response update(Long id, UpdateRequest request) {
        CriterionEntity c = getOrThrow(id);
        c.setName(request.name());
        c.setDescription(request.description());
        if (request.abbr() != null) {
            c.setAbbr(request.abbr());
        }
        if (request.active() != null) {
            c.setActive(request.active());
        }
        return Response.from(repository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        CriterionEntity c = getOrThrow(id);
        if (SawEngine.PRICE_CRITERION_CODE.equals(c.getCode())) {
            throw new ConflictException("Kriteria Harga tidak bisa dihapus (nilainya = harga jual).");
        }
        // Clean up this criterion's ratings and profile weights before removing it.
        repository.deleteToyScores(c.getCode());
        repository.deleteProfileWeights(c.getCode());
        repository.delete(c);
    }

    private CriterionEntity getOrThrow(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Kriteria tidak ditemukan: " + id));
    }

    private CriterionType parseType(String type) {
        String t = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        return switch (t) {
            case "benefit" -> CriterionType.BENEFIT;
            case "cost" -> CriterionType.COST;
            default -> throw new BadRequestException("Tipe kriteria harus 'benefit' atau 'cost'.");
        };
    }

    private String uniqueCode(String name) {
        String base = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "kriteria";
        }
        String code = base;
        int n = 2;
        while (repository.existsByCode(code)) {
            code = base + "-" + n++;
        }
        return code;
    }
}
