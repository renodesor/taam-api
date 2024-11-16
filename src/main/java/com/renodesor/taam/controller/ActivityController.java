package com.renodesor.taam.controller;

import com.renodesor.taam.entity.Activity;
import com.renodesor.taam.repository.ActivityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/taam-api/activities")
@RequiredArgsConstructor
public class ActivityController {
    @Autowired
    private ActivityRepository activityRepository;

    @Operation(summary = "Get all Activities REST API", description = "Get all Activities REST API is used to get all activities in the database")
    @ApiResponse(responseCode = "200", description = "HTTP Status 200 SUCCES")
    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities() {
        return ResponseEntity.ok(activityRepository.findAll());
    }

    @Operation(summary = "Get Activity By Id REST API", description = "Get Activity By Id REST API is used to a activity by its id in the database")
    @ApiResponse(responseCode = "200", description = "HTTP Status 200 SUCCES")
    @GetMapping("/{id}")
    public ResponseEntity<Activity> getActivityById(@PathVariable UUID id) {
        return ResponseEntity.ok(activityRepository.findById(id).orElseThrow(() -> new RuntimeException("Activity not found")));
    }
    
    @Operation(summary = "Save activity REST API", description = "Save activity REST API is used to save activities to the database")
    @ApiResponse(responseCode = "201", description = "HTTP status 201 CREATED")
    @PostMapping
    @Transactional
    public ResponseEntity<Activity> addActivity(@RequestBody Activity activity) {
        try {
            Activity createdActivity = activityRepository.save(activity);
            return ResponseEntity.ok(createdActivity);
        }
        catch(Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Delete activity REST API", description = "Delete activity REST API is used to delete a activity in the database")
    @ApiResponse(responseCode = "204", description = "HTTP Status 204 NO CONTENT")
    public ResponseEntity<Void> deleteActivity(@PathVariable UUID id) {
        Activity activity = activityRepository.findById(id).orElseThrow(() -> new RuntimeException("Activity not found"));
        activityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }    
}
