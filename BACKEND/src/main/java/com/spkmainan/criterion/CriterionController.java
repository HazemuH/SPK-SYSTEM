package com.spkmainan.criterion;

import com.spkmainan.criterion.CriterionDto.Response;
import com.spkmainan.criterion.CriterionDto.UpdateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/criteria")
@Tag(name = "Criteria", description = "Admin criterion management — 10 fixed criteria (auth)")
public class CriterionController {

    private final CriterionService service;

    public CriterionController(CriterionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Response> list() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody UpdateRequest request) {
        return service.update(id, request);
    }
}
