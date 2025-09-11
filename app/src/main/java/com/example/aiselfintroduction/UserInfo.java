package com.example.aiselfintroduction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserInfo implements Serializable {
    // 기본 정보
    private String name;
    private String phone;
    private String email;
    private String educationLevel;
    private String school;
    private String major;
    private List<String> certificates;
    private List<String> experience;
    private String personality;
    private List<String> keywords;
    private String projects;
    private String extraSentence;
    private List<FileInfo> files;

    public UserInfo() {
        this.certificates = new ArrayList<>();
        this.experience = new ArrayList<>();
        this.keywords = new ArrayList<>();
        this.files = new ArrayList<>();
    }

    // Getters and Setters
    // 기본 정보 getter/setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public List<String> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<String> certificates) {
        this.certificates = certificates;
    }
    public List<String> getExperience() {
        return experience;
    }

    public void setExperience(List<String> experience) {
        this.experience = experience;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    public String getExtraSentence() {
        return extraSentence;
    }

    public void setExtraSentence(String extraSentence) {
        this.extraSentence = extraSentence;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }

    // Inner class for file information
    public static class FileInfo implements Serializable {
        private String fileName;
        private String filePath;
        private String mimeType;
        private String extractedText;

        public FileInfo(String fileName, String filePath, String mimeType, String extractedText) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.mimeType = mimeType;
            this.extractedText = extractedText;
        }
        // 기존 생성자도 유지할 경우
        public FileInfo(String fileName, String filePath, String mimeType) {
            this(fileName, filePath, mimeType, ""); // 기본 빈 OCR 값
        }

        public String getFileName() {
            return fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getMimeType() {
            return mimeType;
        }
        public String getExtractedText() { return extractedText; }
    }
}