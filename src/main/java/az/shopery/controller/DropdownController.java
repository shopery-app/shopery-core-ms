package az.shopery.controller;

import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.service.DropdownService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dropdowns")
public class DropdownController {

    private final DropdownService dropdownService;

    @GetMapping("/{type}")
    public ResponseEntity<SuccessResponse<List<String>>> getDropdownOptions(@PathVariable String type) {
        return ResponseEntity.ok(dropdownService.getDropdownOptions(type));
    }
}
