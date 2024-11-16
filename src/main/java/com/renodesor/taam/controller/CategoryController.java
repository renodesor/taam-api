package com.renodesor.taam.controller;

import com.renodesor.taam.entity.Category;
import com.renodesor.taam.entity.TaamUser;
import com.renodesor.taam.repository.CategoryRepository;
import com.renodesor.taam.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name="Categories Service - CategoryController", description = "Category Controller exposes REST APIs for languages services")
@RestController
@RequestMapping("/taam-api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final TaamUser taamUser;

    @Operation(summary = "Get all Categories REST API", description = "Get all Categories REST API is used to get all categories in the database")
    @ApiResponse(responseCode = "200", description = "HTTP Status 200 SUCCES")
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            if(categories.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(categories);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get Category By Id REST API", description = "Get Category By Id REST API is used to a category by its id in the database")
    @ApiResponse(responseCode = "200", description = "HTTP Status 200 SUCCES")
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable UUID id) {
        try{
            Optional<Category> category = categoryRepository.findById(id);
            if(category.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(categoryRepository.findById(id).orElseThrow());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Save category REST API", description = "Save category REST API is used to save categories to the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category CREATED successfully"),
            @ApiResponse(responseCode = "409", description = "Category already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    //@Transactional
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        try {
            if(categoryRepository.findById(category.getId()).isPresent()) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            Utils.setAuditInfo(category, "CREATE", taamUser);
            Category createdCategory = categoryRepository.save(category);
            return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
        }
        catch(DataIntegrityViolationException ex) {
            log.error("A category with this name already exists in the database");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        catch(Exception ex) {
            log.error("Error while trying to insert new category");
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Put category REST API", method = "PUT", description = "Put category REST API is used to add(if not exist) or update (if already exist) categories to the database")
    @ApiResponse(responseCode = "201", description = "HTTP status 201 CREATED")
    @PutMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "201", description = "Category CREATED successfully"),
            @ApiResponse(responseCode = "204", description = "No Content, category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    //@Transactional
    public ResponseEntity<Category> putCategory(@RequestBody Category category) {
        try {
            HttpStatus status;
            if(categoryRepository.findById(category.getId()).isPresent()) {
                Utils.setAuditInfo(category, "UPDATE", taamUser);
                status = HttpStatus.OK;
            } else {
                Utils.setAuditInfo(category, "CREATE", taamUser);
                status = HttpStatus.CREATED;
            }
            Category createdCategory = categoryRepository.save(category);
            return new ResponseEntity<>(createdCategory, status);
        }
        catch(DataIntegrityViolationException ex) {
            log.error("A category with this name or id already exists in the database");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        catch(Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Patch category REST API", method = "PATCH", description = "Patch category REST API is used to update partially an existing category to the database")
    @ApiResponse(responseCode = "200", description = "HTTP status 200 UPDATED")
    @PatchMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "204", description = "No Content, category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    //@Transactional
    public ResponseEntity<Category> patchCategory(@RequestBody Category category) {
        try {
            UUID categoryId = category.getId();
            Category existingCategory = categoryRepository.findById(categoryId).orElse(null);
            if(existingCategory == null) {
               return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            existingCategory = Utils.mergeTwoObjects(existingCategory, category, Category.class);
            Utils.setAuditInfo(existingCategory, "UPDATE", taamUser);
            existingCategory = categoryRepository.save(existingCategory);
            return ResponseEntity.ok(existingCategory);
        }
        catch(DataIntegrityViolationException ex) {
            log.error("A category with this name or id already exists in the database");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        catch(Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Delete category REST API", description = "Delete category REST API is used to delete a category in the database")
    @ApiResponse(responseCode = "204", description = "HTTP Status 204 NO CONTENT")
    @DeleteMapping("/{id}")
    //@Transactional
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        try{
            if(categoryRepository.findById(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            categoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
