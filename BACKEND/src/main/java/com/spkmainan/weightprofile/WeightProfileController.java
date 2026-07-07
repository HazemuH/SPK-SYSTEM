package com.spkmainan.weightprofile;

import com.spkmainan.weightprofile.WeightProfileDto.PairwiseRequest;
import com.spkmainan.weightprofile.WeightProfileDto.Request;
import com.spkmainan.weightprofile.WeightProfileDto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weight-profiles")
@Tag(name = "Weight Profiles", description = "Admin weight profiles + AHP pairwise (auth)")
public class WeightProfileController {

    private final WeightProfileService service;

    public WeightProfileController(WeightProfileService service) {
        this.service = service;
    }

    @GetMapping
    public List<Response> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response create(@Valid @RequestBody Request request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody Request request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/pairwise")
    @Operation(summary = "Save the pairwise matrix and derive criterion weights + CR (AHP)")
    public Response computePairwise(@PathVariable Long id, @RequestBody PairwiseRequest request) {
        return service.computePairwise(id, request);
    }
}
