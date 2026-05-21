package com.sajana.docqa;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AskRequest {
    @NotBlank
    private String document;

    @NotBlank
    private String question;
}
