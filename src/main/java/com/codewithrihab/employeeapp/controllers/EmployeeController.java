package com.codewithrihab.employeeapp.controllers;

import com.codewithrihab.employeeapp.dtos.CreateUserDto;
import com.codewithrihab.employeeapp.entities.Employee;
import com.codewithrihab.employeeapp.mappers.EmployeeDtoMapper;
import com.codewithrihab.employeeapp.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;

@RestController
@RequestMapping("/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final S3Client s3Client;
    private final String BUCKET_NAME = "employe-image-bucket";
    private final EmployeeDtoMapper employeeDtoMapper;

    public EmployeeController(EmployeeService employeeService, S3Client s3Client, EmployeeDtoMapper employeeDtoMapper) {
        this.employeeService = employeeService;
        this.s3Client = s3Client;
        this.employeeDtoMapper = employeeDtoMapper;
    }

    @PostMapping("/add")
    public ResponseEntity<CreateUserDto> save(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) MultipartFile photo
    ) {
        if (employeeService.emailExists(email)) {
            return ResponseEntity.badRequest().build();
        }

        Employee emp = new Employee();
        emp.setFirstName(firstName);
        emp.setLastName(lastName);
        emp.setEmail(email);
        emp.setLocation(location);
        emp.setRole(role);

        // handle photo upload to S3 (same as before)
        if (photo != null && !photo.isEmpty()) {
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
                emp.setPhotoUrl("https://" + BUCKET_NAME + ".s3.amazonaws.com/" + key);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).build();
            }
        }

        Employee saved = employeeService.saveEmployee(emp);
        CreateUserDto dto = employeeDtoMapper.toDto(saved);
        return ResponseEntity.ok(dto);  // return the created employee
    }


    @GetMapping("/{id}")
    public ResponseEntity<CreateUserDto> getEmployeeById(@PathVariable Long id) {
        try {
            CreateUserDto dto = employeeService.getEmployeeById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok("Employee deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CreateUserDto>> getAllEmployees() {
        List<CreateUserDto> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }
}
