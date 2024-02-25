package me.ronygomes.ums.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher_designations")
public class Designation extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "teacher_designation_seq")
    private Long id;

    @Column(unique = true, length = 100)
    private String title;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
