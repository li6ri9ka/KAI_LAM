package com.kai_lam.projects_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "secret_keys")
public class SecretKeyEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_secret_key", nullable = false, updatable = false)
    private UUID idSecretKey;

    @Column(name = "name_secret_key", nullable = false, length = 200)
    private String nameSecretKey;

    @Column(name = "encrypted_value", nullable = false, columnDefinition = "TEXT")
    private String encryptedValue;

    public UUID getIdSecretKey() {
        return idSecretKey;
    }

    public String getNameSecretKey() {
        return nameSecretKey;
    }

    public void setNameSecretKey(String nameSecretKey) {
        this.nameSecretKey = nameSecretKey;
    }

    public String getEncryptedValue() {
        return encryptedValue;
    }

    public void setEncryptedValue(String encryptedValue) {
        this.encryptedValue = encryptedValue;
    }
}
