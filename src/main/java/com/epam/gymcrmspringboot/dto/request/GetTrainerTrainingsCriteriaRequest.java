package com.epam.gymcrmspringboot.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTrainerTrainingsCriteriaRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate toDate;

    @Size(max = 100, message = "trainerName must not exceed 100 characters")
    String traineeName;

    @AssertTrue(message = "fromDate must be before or equal to toDate")
    public boolean isDateRangeValid() {
        if (fromDate != null && toDate != null) {
            return !fromDate.isAfter(toDate);
        }else return true;
    }

}
