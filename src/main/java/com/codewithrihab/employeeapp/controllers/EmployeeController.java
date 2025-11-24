package com.codewithrihab.employeeapp.controllers;

import com.codewithrihab.employeeapp.dtos.CreateUserDto;
import com.codewithrihab.employeeapp.entities.Employee;
import com.codewithrihab.employeeapp.mappers.EmployeeDtoMapper;
import com.codewithrihab.employeeapp.repositories.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class EmployeeController {
    private EmployeeRepository employeeRepository;
    private EmployeeDtoMapper employeeDtoMapper;


    public EmployeeController(EmployeeRepository employeeRepository, EmployeeDtoMapper employeeDtoMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeDtoMapper = employeeDtoMapper;

    }
    @PostMapping("/add")
    public ResponseEntity<CreateUserDto> save(@RequestBody CreateUserDto employee) {
        if( employeeRepository.findByEmail((employee.getEmail())).isPresent())
            return ResponseEntity.badRequest().build();
        Employee entity = employeeDtoMapper.toEntity(employee);
        employeeRepository.save(entity);
        return ResponseEntity.ok(employee);
    }
}
