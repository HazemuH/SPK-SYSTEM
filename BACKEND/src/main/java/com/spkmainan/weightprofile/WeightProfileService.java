package com.spkmainan.weightprofile;

import com.spkmainan.ahp.AhpEngine;
import com.spkmainan.ahp.AhpResult;
import com.spkmainan.common.exception.ConflictException;
import com.spkmainan.common.exception.ResourceNotFoundException;
import com.spkmainan.criterion.CriterionEntity;
import com.spkmainan.criterion.CriterionRepository;
import com.spkmainan.weightprofile.WeightProfileDto.PairwiseEntry;
import com.spkmainan.weightprofile.WeightProfileDto.PairwiseRequest;
import com.spkmainan.weightprofile.WeightProfileDto.Request;
import com.spkmainan.weightprofile.WeightProfileDto.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeightProfileService {

    private final WeightProfileRepository repository;
    private final CriterionRepository criterionRepository;
    private final AhpEngine ahp;

    public WeightProfileService(WeightProfileRepository repository,
                                CriterionRepository criterionRepository, AhpEngine ahp) {
        this.repository = repository;
        this.criterionRepository = criterionRepository;
        this.ahp = ahp;
    }

    @Transactional(readOnly = true)
    public List<Response> findAll() {
        return repository.findAll().stream().map(Response::from).toList();
    }

    @Transactional(readOnly = true)
    public Response findById(Long id) {
        return Response.from(getOrThrow(id));
    }

    @Transactional
    public Response create(Request request) {
        WeightProfileEntity e = new WeightProfileEntity(
            uniqueCode(request.name()), request.name(), request.shortName(),
            request.icon(), request.description(), false, true);
        // Start with equal weights (perfectly consistent) until a pairwise is computed.
        List<CriterionEntity> criteria = criterionRepository.findAllByOrderByNoAsc();
        Map<String, Double> equal = new LinkedHashMap<>();
        double w = criteria.isEmpty() ? 0 : 1.0 / criteria.size();
        criteria.forEach(c -> equal.put(c.getCode(), w));
        e.setWeights(equal);
        e.setCr(0);
        e.setLambdaMax(criteria.size());
        e.setCi(0);
        return Response.from(repository.save(e));
    }

    @Transactional
    public Response update(Long id, Request request) {
        WeightProfileEntity e = getOrThrow(id);
        e.setName(request.name());
        e.setShortName(request.shortName());
        e.setIcon(request.icon());
        e.setDescription(request.description());
        return Response.from(repository.save(e));
    }

    @Transactional
    public void delete(Long id) {
        WeightProfileEntity e = getOrThrow(id);
        if (e.isDefaultProfile()) {
            throw new ConflictException("Profil default tidak bisa dihapus.");
        }
        repository.delete(e);
    }

    /**
     * Save a pairwise-comparison matrix, derive the criterion weights + consistency
     * (AHP), and store them on the profile.
     */
    @Transactional
    public Response computePairwise(Long id, PairwiseRequest request) {
        WeightProfileEntity e = getOrThrow(id);
        List<CriterionEntity> criteria = criterionRepository.findAllByOrderByNoAsc();
        int n = criteria.size();
        Map<String, Integer> index = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            index.put(criteria.get(i).getCode(), i);
        }

        double[][] a = new double[n][n];
        for (double[] row : a) {
            java.util.Arrays.fill(row, 1.0);
        }
        if (request.entries() != null) {
            for (PairwiseEntry entry : request.entries()) {
                Integer i = index.get(entry.rowCode());
                Integer j = index.get(entry.colCode());
                if (i == null || j == null || i.equals(j) || entry.value() <= 0) {
                    continue;
                }
                a[i][j] = entry.value();
                a[j][i] = 1.0 / entry.value();
            }
        }

        AhpResult result = ahp.derive(a);
        Map<String, Double> weights = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            weights.put(criteria.get(i).getCode(), result.weights()[i]);
        }
        e.setWeights(weights);
        e.setCr(result.cr());
        e.setLambdaMax(result.lambdaMax());
        e.setCi(result.ci());
        return Response.from(repository.save(e));
    }

    private WeightProfileEntity getOrThrow(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Profil bobot tidak ditemukan: " + id));
    }

    private String uniqueCode(String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "profil";
        }
        String code = base;
        int n = 2;
        while (repository.existsByCode(code)) {
            code = base + "-" + n++;
        }
        return code;
    }
}
