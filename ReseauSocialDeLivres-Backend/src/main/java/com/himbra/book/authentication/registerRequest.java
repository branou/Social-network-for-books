package com.himbra.book.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter @Builder
public class registerRequest {
    @NotEmpty
    @NotBlank
    private String firstname;
    @NotEmpty
    @NotBlank
    private String lastname;
    @Email(message = "email is not valid")
    @NotEmpty
    @NotBlank
    private String email;
    @NotEmpty
    @NotBlank
    @Size(min=8,message = "password should at least be of 8 character minimum")
    private String password;
}
