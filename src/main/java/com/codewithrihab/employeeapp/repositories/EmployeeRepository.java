package com.codewithrihab.employeeapp.repositories;

import com.codewithrihab.employeeapp.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Object> findByEmail(String email);
}
