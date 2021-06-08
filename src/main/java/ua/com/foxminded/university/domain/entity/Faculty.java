package ua.com.foxminded.university.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Faculty {

    private int id;
    private String name;
    private Teacher dean;

}
