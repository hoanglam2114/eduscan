package org.project.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    private String gender;
    private Integer age;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    private String job;

    // Liên kết một-một ngược lại với User
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Đánh dấu rằng khóa chính của entity này (id) cũng là khóa ngoại
    @JoinColumn(name = "user_id")
    private User user;

    // Getters and Setters
    // ...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}