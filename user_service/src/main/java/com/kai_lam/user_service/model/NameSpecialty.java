package com.kai_lam.user_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "name_specialty")
public class NameSpecialty {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_name_specialty", nullable = false, updatable = false)
    private UUID idNameSpecialty;

    @Column(name = "name_specialty", nullable = false, length = 120, unique = true)
    private String nameSpecialty;

    public UUID getIdNameSpecialty() {
        return idNameSpecialty;
    }

    public String getNameSpecialty() {
        return nameSpecialty;
    }

    public void setNameSpecialty(String nameSpecialty) {
        this.nameSpecialty = nameSpecialty;
    }
}
