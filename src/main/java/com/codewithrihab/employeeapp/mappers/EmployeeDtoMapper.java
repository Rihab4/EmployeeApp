package com.codewithrihab.employeeapp.mappers;

import com.codewithrihab.employeeapp.dtos.CreateUserDto;
import com.codewithrihab.employeeapp.entities.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeDtoMapper {
    Employee toEntity(CreateUserDto createUserDto);
    CreateUserDto toDto(Employee employee);

}
