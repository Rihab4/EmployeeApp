package com.codewithrihab.employeeapp.service;

import com.codewithrihab.employeeapp.dtos.CreateUserDto;
import com.codewithrihab.employeeapp.entities.Employee;
import com.codewithrihab.employeeapp.mappers.EmployeeDtoMapper;
import com.codewithrihab.employeeapp.repositories.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeDtoMapper employeeDtoMapper;

    private final String TOPIC = "employee-events";

    public EmployeeService(EmployeeRepository employeeRepository,
                           EmployeeDtoMapper employeeDtoMapper
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeDtoMapper = employeeDtoMapper;
    }

    public Employee saveEmployee(Employee emp) {
        Employee saved = employeeRepository.save(emp);
        // Send Kafka message
        return saved;
    }

    public List<CreateUserDto> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(employeeDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    public CreateUserDto getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(employeeDtoMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public void deleteEmployee(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employeeRepository.delete(emp);

    }

    public boolean emailExists(String email) {
        return employeeRepository.findByEmail(email).isPresent();
    }
}