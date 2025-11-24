package com.codewithrihab.employeeapp.dtos;

import lombok.Data;

@Data
public class CreateUserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String location;
    private String role;
}
