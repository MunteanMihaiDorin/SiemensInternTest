package com.siemens.internship;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message="Name cannot be blank")
    @Size(max=100, message="Name must not be longer than 100 characters")
    private String name;

    @Size(max=500, message="Description must be at most 500 characters")
    private String description;

    @Size(max=50, message="Status must be at most 50 characters")
    private String status;

    @NotBlank(message="Email cannot be blank")
    @Email(message="Email should be valid")
    private String email;

/*
  Added field-level validation to enforce that:
  - name must not be blank
  - email must be present and valid

  These were introduced as part of the refactor
  to improve input reliability and data integrity.
*/
}