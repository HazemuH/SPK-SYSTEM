package com.spkmainan.criterion;

import com.spkmainan.common.exception.ResourceNotFoundException;
import com.spkmainan.criterion.CriterionDto.Response;
import com.spkmainan.criterion.CriterionDto.UpdateRequest;
import java.util.List;
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
    public Response update(Long id, UpdateRequest request) {
        CriterionEntity c = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Kriteria tidak ditemukan: " + id));
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
}
