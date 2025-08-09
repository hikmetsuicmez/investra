package com.investra.dtos.request;


import com.investra.enums.EstimatedTransactionVolume;
import com.investra.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
public class CreateIndividualClientRequest extends CreateClientRequest {

    @NotBlank(message = "İsim alanı zorunludur")
    private String fullName;

    @NotNull(message = "Vatandaşlık tipi zorunludur")
    private Boolean nationalityType;

    @NotNull(message = "Doğum tarihi zorunludur")
    private LocalDate birthDate;

    @NotNull(message = "Cinsiyet bilgisi zorunludur")
    private Gender gender;

    private String taxId;

    @Size(min = 5, max = 20, message = "Pasaport numarası 5 ile 20 karakter arasında olmalıdır")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Pasaport numarası yalnızca harf ve rakamlardan oluşabilir")
    private String passportNo;

    @Pattern(regexp = "^[0-9]{11}$", message = "Mavi Kart numarası 11 haneli bir sayı olmalıdır")
    private String blueCardNo;

    @Pattern(regexp = "^[0-9]{11}$", message = "Kimlik numarası 11 haneli bir sayı olmalıdır")
    private String nationalityNumber;

    private String profession;
    private String educationStatus;

    @Positive(message = "Aylık gelir pozitif bir değer olmalıdır")
    @NotNull(message = "Aylık gelir zorunludur")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Tahmin edilen işlem hacmi zorunludur")
    private EstimatedTransactionVolume estimatedTransactionVolume;

    @AssertTrue(message = "TCKN, Pasaport No, Yabancı Kimlik No veya Mavi Kart No alanlarından en az biri girilmelidir")
    public boolean isIdentificationProvided() {
        return (nationalityNumber != null && !nationalityNumber.isBlank()) ||
                (passportNo != null && !passportNo.isBlank()) ||
                (taxId != null && !taxId.isBlank()) ||
                (blueCardNo != null && !blueCardNo.isBlank());
    }
}
