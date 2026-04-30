package uk.gov.cslearning.catalogue.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cslearning.catalogue.Utils;
import uk.gov.cslearning.catalogue.dto.EventDto;
import uk.gov.cslearning.catalogue.dto.ModuleDto;
import uk.gov.cslearning.catalogue.service.EventService;
import uk.gov.cslearning.catalogue.service.ModuleService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reporting")
public class ReportController {

    private static final PageRequest MAX_PAGEABLE = PageRequest.of(0, 10000);
    private final EventService eventService;
    private final ModuleService moduleService;

    public ReportController(EventService eventService, ModuleService moduleService) {
        this.eventService = eventService;
        this.moduleService = moduleService;
    }

    @GetMapping("/modules")
    public ResponseEntity<Map<String, ModuleDto>> getModules() {
        ResponseEntity<Map<String, ModuleDto>> response = ResponseEntity.ok(moduleService.getModuleMap());
        if (Utils.hasRole("KPMG_SUPPLIER_REPORTER")) {
            response = ResponseEntity.ok(moduleService.getModuleMapForSupplier("KPMG", MAX_PAGEABLE));
        }
        return response;
    }

    @GetMapping(value = "/modules-for-course-ids", params = {"courseIds"})
    public ResponseEntity<Map<String, ModuleDto>> getModulesForCourseIds(@RequestParam List<String> courseIds) {
        return ResponseEntity.ok(moduleService.getModuleMapForCourseIds(courseIds));
    }

    @GetMapping("/events")
    public ResponseEntity<Map<String, EventDto>> getEvents(HttpServletRequest request) {
        ResponseEntity<Map<String, EventDto>> response = ResponseEntity.ok(eventService.getEventMap());
        if (Utils.hasRole("KPMG_SUPPLIER_REPORTER")) {
            response = ResponseEntity.ok(eventService.getEventMapBySupplier("KPMG", MAX_PAGEABLE));
        }
        return response;
    }
}
