package org.cheong.fittrackermiddleware.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("users")
public class User implements Persistable<String> {

    @Id
    private String id;

    @Override
    public boolean isNew() {
        return id == null;
    }
}
