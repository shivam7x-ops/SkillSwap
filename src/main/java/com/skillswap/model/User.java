package com.skillswap.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model representing a student on the platform.
 *
 * <p>Stored in the Firebase Realtime Database under {@code /users/{uid}}.
 */
public class User {

    private String uid;
    private String name;
    private String email;
    private String college;
    private List<String> skillsToTeach;
    private List<String> skillsToLearn;

    /** No-arg constructor required by Firebase deserialisation. */
    public User() {
        this.skillsToTeach = new ArrayList<>();
        this.skillsToLearn = new ArrayList<>();
    }

    public User(String uid, String name, String email, String college) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.college = college;
        this.skillsToTeach = new ArrayList<>();
        this.skillsToLearn = new ArrayList<>();
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public String getUid()                     { return uid; }
    public void   setUid(String uid)           { this.uid = uid; }

    public String getName()                    { return name; }
    public void   setName(String name)         { this.name = name; }

    public String getEmail()                   { return email; }
    public void   setEmail(String email)       { this.email = email; }

    public String getCollege()                 { return college; }
    public void   setCollege(String college)   { this.college = college; }

    public List<String> getSkillsToTeach()                          { return skillsToTeach; }
    public void         setSkillsToTeach(List<String> skillsToTeach){ this.skillsToTeach = skillsToTeach; }

    public List<String> getSkillsToLearn()                          { return skillsToLearn; }
    public void         setSkillsToLearn(List<String> skillsToLearn){ this.skillsToLearn = skillsToLearn; }

    // ── Convenience ────────────────────────────────────────────────────

    /**
     * Convert to a {@code Map} suitable for Firebase {@code setValue()}.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid",           uid);
        map.put("name",          name);
        map.put("email",         email);
        map.put("college",       college);
        map.put("skillsToTeach", skillsToTeach);
        map.put("skillsToLearn", skillsToLearn);
        return map;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", college='" + college + '\'' +
                ", skillsToTeach=" + skillsToTeach +
                ", skillsToLearn=" + skillsToLearn +
                '}';
    }
}
