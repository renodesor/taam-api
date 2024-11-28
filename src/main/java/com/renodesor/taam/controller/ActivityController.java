package com.renodesor.taam.controller;

import com.renodesor.taam.entity.Activity;
import com.renodesor.taam.entity.TaamUser;
import com.renodesor.taam.repository.ActivityRepository;
import com.renodesor.taam.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/taam-api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityRepository activityRepository;

    private final TaamUser taamUser;
    private static final String UNEXPECTED_ERROR = "Unexpected error - {} ";

    @Operation(summary = "Get all Activities REST API", description = "Get all Activities REST API is used to get all activities in the database")
    @ApiResponse(responseCode = "200", description = "HTTP Status 200 SUCCES")
    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities() {
        try {
            List<Activity> activities = activityRepository.findAll();
            if(activities.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(activities);
        } catch (Exception ex) {
            log.error(UNEXPECTED_ERROR, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get Activity By Id REST API", description = "Get Activity By Id REST API is used to a activity by its id in the database")
    @ApiResponse(responseCode = "200", description = "HTTP Status 200 SUCCES")
    @GetMapping("/{id}")
    public ResponseEntity<Activity> getActivityById(@PathVariable UUID id) {
        try{
            Optional<Activity> activity = activityRepository.findById(id);
            if(activity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(activityRepository.findById(id).orElseThrow());
        } catch (Exception ex) {
            log.error(UNEXPECTED_ERROR, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Save activity REST API", description = "Save activity REST API is used to save activities to the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Activity CREATED successfully"),
            @ApiResponse(responseCode = "409", description = "Activity already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    //@Transactional
    public ResponseEntity<Activity> addActivity(@RequestBody Activity activity) {
        try {
            if(activityRepository.findById(activity.getId()).isPresent()) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            Utils.setAuditInfo(activity, "CREATE", taamUser);
            Activity createdActivity = activityRepository.save(activity);
            return new ResponseEntity<>(createdActivity, HttpStatus.CREATED);
        }
        catch(DataIntegrityViolationException ex) {
            log.error("A activity with this name already exists in the database");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        catch(Exception ex) {
            log.error(UNEXPECTED_ERROR, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Put activity REST API", method = "PUT", description = "Put activity REST API is used to add(if not exist) or update (if already exist) activities to the database")
    @ApiResponse(responseCode = "201", description = "HTTP status 201 CREATED")
    @PutMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity updated successfully"),
            @ApiResponse(responseCode = "201", description = "Activity CREATED successfully"),
            @ApiResponse(responseCode = "204", description = "No Content, activity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Activity not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    //@Transactional
    public ResponseEntity<Activity> putActivity(@RequestBody Activity activity) {
        try {
            HttpStatus status;
            Optional<Activity> existingActivity = activityRepository.findById(activity.getId());
            Activity activityToPersist;
            if(existingActivity.isPresent()) {
                activityToPersist = existingActivity.get();
                activityToPersist.setName(activity.getName());
                activityToPersist.setDescription(activity.getDescription());
                Utils.setAuditInfo(activityToPersist, "UPDATE", taamUser);
                status = HttpStatus.OK;
            } else {
                activityToPersist = activity;
                Utils.setAuditInfo(activityToPersist, "CREATE", taamUser);
                status = HttpStatus.CREATED;
            }
            Activity createdActivity = activityRepository.save(activityToPersist);
            return new ResponseEntity<>(createdActivity, status);
        }
        catch(DataIntegrityViolationException ex) {
            log.error("A activity with this name or id already exists in the database");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        catch(Exception ex) {
            log.error(UNEXPECTED_ERROR, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Patch activity REST API", method = "PATCH", description = "Patch activity REST API is used to update partially an existing activity to the database")
    @ApiResponse(responseCode = "200", description = "HTTP status 200 UPDATED")
    @PatchMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity updated successfully"),
            @ApiResponse(responseCode = "204", description = "No Content, activity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Activity not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    //@Transactional
    public ResponseEntity<Activity> patchActivity(@RequestBody Activity activity) {
        try {
            UUID activityId = activity.getId();
            Activity existingActivity = activityRepository.findById(activityId).orElse(null);
            if(existingActivity == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            existingActivity = Utils.mergeTwoObjects(existingActivity, activity, Activity.class);
            Utils.setAuditInfo(existingActivity, "UPDATE", taamUser);
            existingActivity = activityRepository.save(existingActivity);
            return ResponseEntity.ok(existingActivity);
        }
        catch(DataIntegrityViolationException ex) {
            log.error("A activity with this name or id already exists in the database");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        catch(Exception ex) {
            log.error(UNEXPECTED_ERROR, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Delete activity REST API", description = "Delete activity REST API is used to delete a activity in the database")
    @ApiResponse(responseCode = "204", description = "HTTP Status 204 NO CONTENT")
    @DeleteMapping("/{id}")
    //@Transactional
    public ResponseEntity<Void> deleteActivity(@PathVariable UUID id) {
        try{
            if(activityRepository.findById(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            activityRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            log.error(UNEXPECTED_ERROR, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }  
}
