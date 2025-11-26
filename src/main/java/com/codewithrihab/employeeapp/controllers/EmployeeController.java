package com.codewithrihab.employeeapp.controllers;

import com.codewithrihab.employeeapp.dtos.CreateUserDto;
import com.codewithrihab.employeeapp.entities.Employee;
import software.amazon.awssdk.core.sync.RequestBody;
import com.codewithrihab.employeeapp.mappers.EmployeeDtoMapper;
import com.codewithrihab.employeeapp.repositories.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final EmployeeDtoMapper employeeDtoMapper;
    private final S3Client s3Client;
    private final String BUCKET_NAME = "employe-image-bucket";

    public EmployeeController(EmployeeRepository employeeRepository, EmployeeDtoMapper employeeDtoMapper, S3Client s3Client) {
        this.employeeRepository = employeeRepository;
        this.employeeDtoMapper = employeeDtoMapper;
        this.s3Client = s3Client;
    }


    @PostMapping("/add")
    public ResponseEntity<?> save(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) MultipartFile photo
    ) {
        if(employeeRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("User with this email already exists");
        }

        // Map fields to Employee entity
        Employee emp = new Employee();
        emp.setFirstName(firstName);
        emp.setLastName(lastName);
        emp.setEmail(email);
        emp.setLocation(location);
        emp.setRole(role);

        // handle photo upload to S3 and set photo URL in Employee entity
        if(photo != null && !photo.isEmpty()) {
            try {
                String key = "employees/" + System.currentTimeMillis() + "_" + photo.getOriginalFilename();
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(BUCKET_NAME)
                                .key(key)
                                .contentType(photo.getContentType())
                                .build(),
                        RequestBody.fromBytes(photo.getBytes())
                );

                // set public URL (assuming bucket is public or using CloudFront)
                String photoUrl = "https://" + BUCKET_NAME + ".s3.amazonaws.com/" + key;
                emp.setPhotoUrl(photoUrl);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Failed to upload photo");
            }
        }
        employeeRepository.save(emp);
        return ResponseEntity.ok("Employee saved");
    }



    @GetMapping("/employee/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {

        return employeeRepository.findById(id)
                .<ResponseEntity<?>>map(employee -> ResponseEntity.ok(employee))
                .orElseGet(() -> ResponseEntity
                        .status(404)
                        .body("Employee not found"));
    }


    @DeleteMapping("/employee/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!employeeRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Employee not found");
        }
        employeeRepository.deleteById(id);
        return ResponseEntity.ok("Employee deleted");
    }
    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return ResponseEntity.ok(employees);
    }
    @GetMapping("/test-s3")
    public String testS3() {
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            return "Buckets: " + response.buckets().stream().map(b -> b.name()).toList();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

}
