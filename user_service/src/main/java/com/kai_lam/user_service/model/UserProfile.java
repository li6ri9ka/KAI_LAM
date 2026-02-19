package com.kai_lam.user_service.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
public class UserProfile {
    @Id
    @Column(name = "id_user", nullable = false, updatable = false)
    private UUID idUser;

    @Column(name = "name_user", nullable = false, length = 120)
    private String nameUser;

    @Column(name = "midle_name_user", length = 120)
    private String midleNameUser;

    @Column(name = "login_user", length = 100)
    private String loginUser;

    @Column(name = "email_user", length = 255)
    private String emailUser;

    @Column(name = "phone_user", length = 64)
    private String phoneUser;

    @Column(name = "telegram_user", length = 128)
    private String telegramUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_spec_user_id")
    private NameSpecialty nameSpecialty;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserTeam> userTeams = new HashSet<>();

    public UUID getIdUser() {
        return idUser;
    }

    public void setIdUser(UUID idUser) {
        this.idUser = idUser;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public String getMidleNameUser() {
        return midleNameUser;
    }

    public void setMidleNameUser(String midleNameUser) {
        this.midleNameUser = midleNameUser;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public String getPhoneUser() {
        return phoneUser;
    }

    public void setPhoneUser(String phoneUser) {
        this.phoneUser = phoneUser;
    }

    public String getTelegramUser() {
        return telegramUser;
    }

    public void setTelegramUser(String telegramUser) {
        this.telegramUser = telegramUser;
    }

    public NameSpecialty getNameSpecialty() {
        return nameSpecialty;
    }

    public void setNameSpecialty(NameSpecialty nameSpecialty) {
        this.nameSpecialty = nameSpecialty;
    }

    public Set<UserTeam> getUserTeams() {
        return userTeams;
    }
}
