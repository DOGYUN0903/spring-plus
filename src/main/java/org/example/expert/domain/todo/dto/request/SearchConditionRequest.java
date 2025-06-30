package org.example.expert.domain.todo.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SearchConditionRequest {

    private String keyword;
    private String managerNickname;
    private LocalDate startDate;
    private LocalDate endDate;
}
