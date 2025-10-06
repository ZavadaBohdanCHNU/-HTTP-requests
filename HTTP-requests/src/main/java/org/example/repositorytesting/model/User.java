package org.example.repositorytesting.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "items")
public class User {
    @Id
    private String id;
    private String name;
    private String code;
    private String description;
    private LocalDateTime createDate;
    private List<LocalDateTime> updateDates;
}
