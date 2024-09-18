package com.himbra.book.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class RequestToLogin {
    @Email(message = "email is not valid")
    @NotEmpty(message ="email must not be empty")
    @NotBlank(message ="email must not be blank")
    private String email;
    @NotBlank(message = "password is blank")
    @NotEmpty(message ="password must not be empty")
    @Size(min=8,message = "password should at least be of 8 character minimum")
    private String password;
}
