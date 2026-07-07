package com.spkmainan.toy;

import com.spkmainan.category.CategoryRepository;
import com.spkmainan.common.dto.PageResponse;
import com.spkmainan.common.exception.BadRequestException;
import com.spkmainan.common.exception.ResourceNotFoundException;
import com.spkmainan.criterion.CriterionEntity;
import com.spkmainan.criterion.CriterionRepository;
import com.spkmainan.domain.CriterionType;
import com.spkmainan.toy.ToyDto.Request;
import com.spkmainan.toy.ToyDto.Response;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ToyService {

    private final ToyRepository repository;
    private final CategoryRepository categoryRepository;
    private final CriterionRepository criterionRepository;

    public ToyService(ToyRepository repository, CategoryRepository categoryRepository,
                      CriterionRepository criterionRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.criterionRepository = criterionRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<Response> findAll(String search, String categoryCode, Pageable pageable) {
        String q = search == null ? "" : search;
        var page = (categoryCode == null || categoryCode.isBlank())
            ? repository.findByNameContainingIgnoreCase(q, pageable)
            : repository.findByNameContainingIgnoreCaseAndCategoryCode(q, categoryCode, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public Response create(Request request) {
        requireCategory(request.categoryCode());
        ToyEntity t = new ToyEntity(request.name(), request.categoryCode(), request.price(),
            request.ageMin(), request.ageMax(), request.stock(),
            request.active() == null || request.active(), request.description());
        applyTags(t, request.tags());
        if (request.scores() != null) {
            t.setScores(validatedScores(request.scores()));
        }
        return toResponse(repository.save(t));
    }

    @Transactional
    public Response update(Long id, Request request) {
        requireCategory(request.categoryCode());
        ToyEntity t = getOrThrow(id);
        t.setName(request.name());
        t.setCategoryCode(request.categoryCode());
        t.setPrice(request.price());
        t.setAgeMin(request.ageMin());
        t.setAgeMax(request.ageMax());
        t.setStock(request.stock());
        if (request.active() != null) {
            t.setActive(request.active());
        }
        t.setDescription(request.description());
        applyTags(t, request.tags());
        if (request.scores() != null) {
            t.setScores(validatedScores(request.scores()));
        }
        return toResponse(repository.save(t));
    }

    @Transactional
    public Response updateScores(Long id, Map<String, Integer> scores) {
        ToyEntity t = getOrThrow(id);
        t.setScores(validatedScores(scores));
        return toResponse(repository.save(t));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private ToyEntity getOrThrow(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Mainan tidak ditemukan: " + id));
    }

    private void requireCategory(String code) {
        if (!categoryRepository.existsByCode(code)) {
            throw new BadRequestException("Kategori tidak dikenal: " + code);
        }
    }

    private void applyTags(ToyEntity t, Set<String> tags) {
        t.setTags(tags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tags));
    }

    /** Scores must be for benefit criteria only, each rating in 1..5. */
    private Map<String, Integer> validatedScores(Map<String, Integer> scores) {
        Set<String> benefitCodes = criterionRepository.findAll().stream()
            .filter(c -> c.getType() == CriterionType.BENEFIT)
            .map(CriterionEntity::getCode).collect(Collectors.toSet());
        Map<String, Integer> clean = new LinkedHashMap<>();
        for (var e : scores.entrySet()) {
            if (!benefitCodes.contains(e.getKey())) {
                throw new BadRequestException("Kriteria bukan benefit atau tidak dikenal: " + e.getKey());
            }
            int v = e.getValue() == null ? 0 : e.getValue();
            if (v < 1 || v > 5) {
                throw new BadRequestException("Nilai " + e.getKey() + " harus 1–5 (diberikan " + v + ")");
            }
            clean.put(e.getKey(), v);
        }
        return clean;
    }

    private Response toResponse(ToyEntity t) {
        String categoryName = categoryRepository.findByCode(t.getCategoryCode())
            .map(c -> c.getName()).orElse(t.getCategoryCode());
        return new Response(t.getId(), t.getName(), t.getCategoryCode(), categoryName, t.getPrice(),
            t.getAgeMin(), t.getAgeMax(), t.getStock(), t.isActive(), t.getDescription(),
            new LinkedHashSet<>(t.getTags()), new LinkedHashMap<>(t.getScores()));
    }
}
