package com.lsm.model.entity.base;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.lsm.model.entity.StudentDetails;
import com.lsm.model.entity.enums.Role;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "app_users")
@Inheritance(strategy = InheritanceType.JOINED)
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    @NotNull
    @Size(min = 3, max = 60)
    @Column(name="username", unique=true)
    private String username;

    @NotNull
    @Size(min = 5, max = 100)
    @Column(name="email", unique=true)
    private String email;

    @NotNull
    @Size(min = 6, max = 100)
    @Column(name="password")
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name="role")
    private Role role;

    @Embedded
    @Nullable
    private StudentDetails studentDetails;

    @ElementCollection
    @CollectionTable(name = "user_classes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "class_id")
    private List<Long> classes;  // List of class IDs associated with the user (for teachers)

    // Constructors, Getters, and Setters

    public AppUser() {}

    public AppUser(String username, String email, String rawPassword, Role role) {
        this.username = username;
        this.email = email;
        this.password = hashPassword(rawPassword);
        this.role = role;
    }

    public AppUser(String username, String email, String rawPassword, Role role, StudentDetails studentDetails) {
        this.username = username;
        this.email = email;
        this.password = hashPassword(rawPassword);
        this.role = role;
        this.studentDetails = studentDetails;
    }

    public AppUser(String username, String email, String rawPassword, Role role, StudentDetails studentDetails, List<Long> classes) {
        this.username = username;
        this.email = email;
        this.password = hashPassword(rawPassword);
        this.role = role;
        this.studentDetails = studentDetails;
        this.classes = classes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Password hashing using BCrypt
    public void setPassword(String rawPassword) {
        this.password = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public boolean checkPassword(String rawPassword) {
        return BCrypt.checkpw(rawPassword, this.password);
    }

    @Override
    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    private String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public StudentDetails getStudentDetails() {
        return studentDetails;
    }

    public void setStudentDetails(StudentDetails studentDetails) {
        this.studentDetails = studentDetails;
    }

    public List<Long> getClasses() {
        return classes;
    }

    public void setClasses(List<Long> classes) {
        this.classes = classes;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.getRole().name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
