package com.spkmainan.calculation;

import com.spkmainan.calculation.CalculationDto.PrecheckResponse;
import com.spkmainan.calculation.CalculationDto.RunDetail;
import com.spkmainan.calculation.CalculationDto.RunSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calculations")
@Tag(name = "Calculations", description = "Admin AHP-SAW calculation & publish (auth)")
public class CalculationController {

    private final CalculationService service;

    public CalculationController(CalculationService service) {
        this.service = service;
    }

    @PostMapping("/precheck")
    @Operation(summary = "Completeness & consistency checks before running")
    public PrecheckResponse precheck() {
        return service.precheck();
    }

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Synthesize SAW rankings for every profile (creates a session)")
    public RunSummary run() {
        return service.run();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a session's results to the mobile app")
    public RunSummary publish(@PathVariable Long id) {
        return service.publish(id);
    }

    @PostMapping("/{id}/unpublish")
    @Operation(summary = "Withdraw a published session (mobile shows no result until re-published)")
    public RunSummary unpublish(@PathVariable Long id) {
        return service.unpublish(id);
    }

    @GetMapping
    @Operation(summary = "List calculation sessions (reports)")
    public List<RunSummary> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Session detail: per-profile rankings")
    public RunDetail detail(@PathVariable Long id) {
        return service.detail(id);
    }
}
