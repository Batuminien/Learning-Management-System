package com.lsm.model.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lsm.model.entity.ProfilePhoto;
import com.lsm.model.entity.StudentDetails;
import com.lsm.model.entity.TeacherDetails;
import com.lsm.model.entity.enums.Role;
import com.lsm.model.entity.enums.SchoolLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "app_users")
@Inheritance(strategy = InheritanceType.JOINED)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser implements UserDetails {
    
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_seq")
    @SequenceGenerator(
            name = "app_user_seq",
            sequenceName = "app_users_seq",
            allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 3, max = 60)
    @Column(name = "username", unique = true)
    private String username;

    @NotNull
    @Size(min = 2, max = 50)
    @Column(name = "name")
    private String name;

    @NotNull
    @Size(min = 2, max = 50)
    @Column(name = "surname")
    private String surname;

    @NotNull
    @Size(min = 5, max = 100)
    @Email
    @Column(name = "email", unique = true)
    private String email;

    @NotNull
    @Size(min = 6, max = 100)
    @Column(name = "password")
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "role")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "school_level")
    private SchoolLevel schoolLevel;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "phone", column = @Column(name = "student_phone")),
            @AttributeOverride(name = "tc", column = @Column(name = "student_tc")),
            @AttributeOverride(name = "birthDate", column = @Column(name = "student_birth_date")),
            @AttributeOverride(name = "registrationDate", column = @Column(name = "student_registration_date")),
            @AttributeOverride(name = "parentName", column = @Column(name = "student_parent_name")),
            @AttributeOverride(name = "parentPhone", column = @Column(name = "student_parent_phone")),
            @AttributeOverride(name = "classEntity", column = @Column(name = "class_id_student"))
    })
    private StudentDetails studentDetails;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "phone", column = @Column(name = "teacher_phone")),
            @AttributeOverride(name = "tc", column = @Column(name = "teacher_tc")),
            @AttributeOverride(name = "birthDate", column = @Column(name = "teacher_birth_date"))
    })
    private TeacherDetails teacherDetails;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfilePhoto profilePhoto;


    public AppUser(String username, String email, String rawPassword, Role role) {
        this.username = username;
        this.email = email;
        this.password = hashPassword(rawPassword);
        this.role = role;
    }

    public AppUser(String username, String email, String rawPassword, Role role, StudentDetails studentDetails) {
        this(username, email, rawPassword, role);
        this.studentDetails = studentDetails;
    }

    public AppUser(String username, String email, String rawPassword, Role role, TeacherDetails teacherDetails) {
        this(username, email, rawPassword, role);
        this.teacherDetails = teacherDetails;
    }

    private String hashPassword(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    // public void setPassword(String rawPassword) {
    //     this.password = hashPassword(rawPassword);
    // }
    
    public boolean checkPassword(String rawPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, this.password);
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}