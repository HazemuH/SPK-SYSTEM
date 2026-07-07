package com.spkmainan.toy;

import com.spkmainan.common.dto.PageResponse;
import com.spkmainan.toy.ToyDto.Request;
import com.spkmainan.toy.ToyDto.Response;
import com.spkmainan.toy.ToyDto.ScoresRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/toys")
@Tag(name = "Toys", description = "Admin toy management incl. 1–5 criterion scores (auth)")
public class ToyController {

    private final ToyService service;

    public ToyController(ToyService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<Response> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryCode,
            Pageable pageable) {
        return service.findAll(search, categoryCode, pageable);
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

    @PutMapping("/{id}/scores")
    public Response updateScores(@PathVariable Long id, @Valid @RequestBody ScoresRequest request) {
        return service.updateScores(id, request.scores());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
