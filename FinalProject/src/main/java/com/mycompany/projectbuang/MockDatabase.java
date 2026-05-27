package com.mycompany.projectbuang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockDatabase {
    public static final Map<String, UserAccount> userDatabase = new HashMap<>();
    public static final Map<String, PatientProfile> patientDatabase = new HashMap<>();
    public static final List<String> globalAuditLogs = new ArrayList<>();

    static {
        loadDatabase();
    }

    private static final String USERS_FILE = "users.db";
    private static final String PATIENTS_FILE = "patients.db";

    public static synchronized void saveDatabase() {
        try {
            java.io.PrintWriter userWriter = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(USERS_FILE), java.nio.charset.StandardCharsets.UTF_8));
            for (UserAccount acc : userDatabase.values()) {
                userWriter.println(acc.username + "|||" + acc.password + "|||" + acc.roleType + "|||" + acc.fullName + "|||" + acc.roleTitle);
            }
            userWriter.close();

            java.io.PrintWriter patientWriter = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(PATIENTS_FILE), java.nio.charset.StandardCharsets.UTF_8));
            for (PatientProfile pat : patientDatabase.values()) {
                String notesJoined = String.join(":::", pat.clinicalNotes);
                patientWriter.println(pat.name + "|||" + pat.meta + "|||" + pat.heartRate + "|||" + pat.temperature + "|||" + pat.oxygen + "|||" +
                                      pat.weight + "|||" + pat.sleepHours + "|||" + pat.sleepMinutes + "|||" + pat.moveCal + "|||" + pat.exerciseMin + "|||" +
                                      pat.standHr + "|||" + pat.height + "|||" + pat.bmi + "|||" + pat.stateOfMind + "|||" + pat.stateOfMindBadge + "|||" +
                                      pat.fitPlanGoal + "|||" + pat.fitPlanFocus + "|||" + pat.fitPlanDays + "|||" + pat.fitPlanTime + "|||" +
                                      pat.calorieBurnt + "|||" + pat.calorieConsumed + "|||" + pat.weightHistory + "|||" + pat.mealsCalorieMap + "|||" +
                                      pat.stepCount + "|||" + pat.stepGoal + "|||" + pat.stepHistory + "|||" +
                                      pat.heartRateMinMaxRange + "|||" + pat.heartRateHistory + "|||" +
                                      pat.age + "|||" + pat.gender + "|||" + pat.bloodType + "|||" +
                                      pat.phone + "|||" + pat.email + "|||" + pat.address + "|||" + 
                                      pat.oxygenHistory + "|||" + pat.currentCondition + "|||" + notesJoined);
            }
            patientWriter.close();
            logActivity("Database synchronized and written to disk.");
        } catch (java.io.IOException e) {
            System.err.println("Error saving database: " + e.getMessage());
        }
    }

    public static synchronized void loadDatabase() {
        java.io.File userFile = new java.io.File(USERS_FILE);
        java.io.File patientFile = new java.io.File(PATIENTS_FILE);

        if (userFile.exists() && patientFile.exists()) {
            try {
                userDatabase.clear();
                patientDatabase.clear();

                java.io.BufferedReader userReader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(userFile), java.nio.charset.StandardCharsets.UTF_8));
                String line;
                while ((line = userReader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(java.util.regex.Pattern.quote("|||"), -1);
                    if (parts.length >= 5) {
                        UserAccount acc = new UserAccount(parts[0], parts[1], parts[2], parts[3], parts[4]);
                        userDatabase.put(acc.username, acc);
                    }
                }
                userReader.close();

                java.io.BufferedReader patientReader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(patientFile), java.nio.charset.StandardCharsets.UTF_8));
                while ((line = patientReader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(java.util.regex.Pattern.quote("|||"), -1);
                    if (parts.length >= 14) {
                        String name = parts[0];
                        String meta = parts[1];
                        if (meta != null && meta.contains("Age:")) {
                            int pipeIndex = meta.indexOf("|");
                            if (pipeIndex != -1) {
                                meta = meta.substring(pipeIndex + 1).trim();
                            } else {
                                meta = "";
                            }
                        }
                        String hr = parts[2];
                        String temp = parts[3];
                        String ox = parts[4];
                        String weight = parts[5];
                        String sleepHrs = parts[6];
                        String sleepMins = parts[7];
                        String moveCal = parts[8];
                        String exerciseMin = parts[9];
                        String standHr = parts[10];

                        // Flexible parsing with fallbacks for migration
                        String height = "170";
                        String bmi = "24.2";
                        String stateOfMind = "Calm";
                        String stateOfMindBadge = "Neutral";
                        String fitPlanGoal = "0";
                        String fitPlanFocus = "";
                        String fitPlanDays = "";
                        String fitPlanTime = "";
                        String calorieBurnt = "156";
                        String calorieConsumed = "0";
                        String weightHistory = weight + "," + weight + "," + weight;
                        String mealsCalorieMap = "breakfast=0;lunch=0;dinner=0";
                        String stepCount = "5241";
                        String stepGoal = "10000";
                        String stepHistory = "4500,5000,5500,5241";
                        String heartRateMinMaxRange = "60-185";
                        String heartRateHistory = "60,65,70,85,110,140,175,170,110,98";
                        String notesJoined = "";
                        String age = "";
                        String gender = "";
                        String bloodType = "";
                        String phone = "";
                        String email = "";
                        String address = "";
                        String oxygenHistory = "98,97,98,99,98,98";
                        String currentCondition = "Healthy";

                        if (parts.length >= 37) {
                            height = parts[11];
                            bmi = parts[12];
                            stateOfMind = parts[13];
                            stateOfMindBadge = parts[14];
                            fitPlanGoal = parts[15];
                            fitPlanFocus = parts[16];
                            fitPlanDays = parts[17];
                            fitPlanTime = parts[18];
                            calorieBurnt = parts[19];
                            calorieConsumed = parts[20];
                            weightHistory = parts[21];
                            mealsCalorieMap = parts[22];
                            stepCount = parts[23];
                            stepGoal = parts[24];
                            stepHistory = parts[25];
                            heartRateMinMaxRange = parts[26];
                            heartRateHistory = parts[27];
                            age = parts[28];
                            gender = parts[29];
                            bloodType = parts[30];
                            phone = parts[31];
                            email = parts[32];
                            address = parts[33];
                            oxygenHistory = parts[34];
                            currentCondition = parts[35];
                            notesJoined = parts.length > 36 ? parts[36] : "";
                        } else if (parts.length >= 36) {
                            height = parts[11];
                            bmi = parts[12];
                            stateOfMind = parts[13];
                            stateOfMindBadge = parts[14];
                            fitPlanGoal = parts[15];
                            fitPlanFocus = parts[16];
                            fitPlanDays = parts[17];
                            fitPlanTime = parts[18];
                            calorieBurnt = parts[19];
                            calorieConsumed = parts[20];
                            weightHistory = parts[21];
                            mealsCalorieMap = parts[22];
                            stepCount = parts[23];
                            stepGoal = parts[24];
                            stepHistory = parts[25];
                            heartRateMinMaxRange = parts[26];
                            heartRateHistory = parts[27];
                            age = parts[28];
                            gender = parts[29];
                            bloodType = parts[30];
                            phone = parts[31];
                            email = parts[32];
                            address = parts[33];
                            oxygenHistory = parts[34];
                            notesJoined = parts.length > 35 ? parts[35] : "";
                        } else if (parts.length >= 35) {
                            height = parts[11];
                            bmi = parts[12];
                            stateOfMind = parts[13];
                            stateOfMindBadge = parts[14];
                            fitPlanGoal = parts[15];
                            fitPlanFocus = parts[16];
                            fitPlanDays = parts[17];
                            fitPlanTime = parts[18];
                            calorieBurnt = parts[19];
                            calorieConsumed = parts[20];
                            weightHistory = parts[21];
                            mealsCalorieMap = parts[22];
                            stepCount = parts[23];
                            stepGoal = parts[24];
                            stepHistory = parts[25];
                            heartRateMinMaxRange = parts[26];
                            heartRateHistory = parts[27];
                            age = parts[28];
                            gender = parts[29];
                            bloodType = parts[30];
                            phone = parts[31];
                            email = parts[32];
                            address = parts[33];
                            notesJoined = parts.length > 34 ? parts[34] : "";
                        } else if (parts.length >= 28) {
                            height = parts[11];
                            bmi = parts[12];
                            stateOfMind = parts[13];
                            stateOfMindBadge = parts[14];
                            fitPlanGoal = parts[15];
                            fitPlanFocus = parts[16];
                            fitPlanDays = parts[17];
                            fitPlanTime = parts[18];
                            calorieBurnt = parts[19];
                            calorieConsumed = parts[20];
                            weightHistory = parts[21];
                            mealsCalorieMap = parts[22];
                            stepCount = parts[23];
                            stepGoal = parts[24];
                            stepHistory = parts[25];
                            heartRateMinMaxRange = parts[26];
                            heartRateHistory = parts[27];
                            notesJoined = parts.length > 28 ? parts[28] : "";
                        } else if (parts.length >= 26) {
                            height = parts[11];
                            bmi = parts[12];
                            stateOfMind = parts[13];
                            stateOfMindBadge = parts[14];
                            fitPlanGoal = parts[15];
                            fitPlanFocus = parts[16];
                            fitPlanDays = parts[17];
                            fitPlanTime = parts[18];
                            calorieBurnt = parts[19];
                            calorieConsumed = parts[20];
                            weightHistory = parts[21];
                            mealsCalorieMap = parts[22];
                            stepCount = parts[23];
                            stepGoal = parts[24];
                            stepHistory = parts[25];
                            notesJoined = parts.length > 26 ? parts[26] : "";
                        } else if (parts.length >= 23) {
                            height = parts[11];
                            bmi = parts[12];
                            stateOfMind = parts[13];
                            stateOfMindBadge = parts[14];
                            fitPlanGoal = parts[15];
                            fitPlanFocus = parts[16];
                            fitPlanDays = parts[17];
                            fitPlanTime = parts[18];
                            calorieBurnt = parts[19];
                            calorieConsumed = parts[20];
                            weightHistory = parts[21];
                            mealsCalorieMap = parts[22];
                            notesJoined = parts.length > 23 ? parts[23] : "";
                        } else if (parts.length >= 19) {
                            height = parts[11];
                            bmi = parts[12];
                            stateOfMind = parts[13];
                            stateOfMindBadge = parts[14];
                            fitPlanGoal = parts[15];
                            fitPlanFocus = parts[16];
                            fitPlanDays = parts[17];
                            fitPlanTime = parts[18];
                            notesJoined = parts.length > 19 ? parts[19] : "";
                            weightHistory = weight + "," + weight + "," + weight;
                        } else {
                            // Migrate older formats
                            String val11 = parts[11];
                            if (val11.matches("\\d+")) {
                                height = val11;
                            }
                            String val12 = parts[12];
                            if (val12.matches("\\d+(\\.\\d+)?")) {
                                bmi = val12;
                            } else {
                                stateOfMind = val12;
                            }
                            if (parts.length > 13) {
                                String val13 = parts[13];
                                if (val13.matches("\\d+(\\.\\d+)?")) {
                                    bmi = val13;
                                } else {
                                    stateOfMindBadge = val13;
                                }
                            }
                            notesJoined = parts[parts.length - 1];
                            weightHistory = weight + "," + weight + "," + weight;
                        }

                        PatientProfile pat = new PatientProfile(name, meta, hr, temp, ox, weight, sleepHrs, sleepMins, moveCal, exerciseMin, standHr, height, bmi, stateOfMind, stateOfMindBadge, fitPlanGoal, fitPlanFocus, fitPlanDays, fitPlanTime, calorieBurnt, calorieConsumed, weightHistory, mealsCalorieMap, stepCount, stepGoal, stepHistory);
                        pat.heartRateMinMaxRange = heartRateMinMaxRange;
                        pat.heartRateHistory = heartRateHistory;
                        pat.age = age;
                        pat.gender = gender;
                        pat.bloodType = bloodType;
                        pat.phone = phone;
                        pat.email = email;
                        pat.address = address;
                        pat.oxygenHistory = oxygenHistory;
                        pat.currentCondition = currentCondition;

                        if (!notesJoined.isEmpty()) {
                            pat.clinicalNotes.clear();
                            String[] notes = notesJoined.split(":::");
                            for (String note : notes) {
                                if (!note.trim().isEmpty()) {
                                    pat.clinicalNotes.add(note);
                                }
                            }
                        }
                        patientDatabase.put(pat.name, pat);
                    }
                }
                patientReader.close();
                logActivity("Database loaded successfully from disk.");
            } catch (java.io.IOException e) {
                System.err.println("Error loading database: " + e.getMessage());
                seedDefaultData();
            }
        } else {
            seedDefaultData();
        }
    }

    private static void seedDefaultData() {
        userDatabase.clear();
        patientDatabase.clear();
        
        userDatabase.put("admin", new UserAccount("admin", "admin123", "ADMIN", "Admin", "Chief Medical Officer"));
        userDatabase.put("root", new UserAccount("root", "root123", "ADMIN", "Admin Operator", "Infrastructure Support"));
        
        userDatabase.put("alice", new UserAccount("alice", "patient123", "PATIENT", "Alice Jenkins", "Active User"));
        userDatabase.put("bob", new UserAccount("bob", "patient123", "PATIENT", "Bob Thompson", "Critical Care User"));

        PatientProfile alicePat = new PatientProfile("Alice Jenkins", "Ward B, Bed 4", "98", "36.6", "98",
                "68", "7", "15", "420", "30", "6", "160", "26.6", "Calm and Focused", "Relaxed", "60.0", "Abs, Glutes, Legs", "Mon, Wed, Thu, Sat", "18:30", "156", "0", "68,67.5,67,66.8", "breakfast=0;lunch=0;dinner=0", "5241", "10000", "4500,5000,5500,5241");
        alicePat.heartRateMinMaxRange = "60-185";
        alicePat.heartRateHistory = "60,65,70,85,110,140,175,170,110,98";
        alicePat.age = "34";
        alicePat.gender = "Female";
        alicePat.bloodType = "A+";
        alicePat.phone = "(555) 019-2834";
        alicePat.email = "alice.jenkins@gmail.com";
        alicePat.address = "123 Maple Street, Ward B, Bed 4";
        alicePat.currentCondition = "Healthy";
        patientDatabase.put("Alice Jenkins", alicePat);

        PatientProfile bobPat = new PatientProfile("Bob Thompson", "ICU, Bed 1", "112", "38.9", "92",
                "82", "5", "45", "150", "5", "2", "180", "25.3", "Fatigued", "Tired", "75.0", "Whole body", "Mon, Wed, Fri", "08:00", "156", "0", "82,81.5,82,81.8", "breakfast=0;lunch=0;dinner=0", "3120", "8000", "2800,3200,2900,3120");
        bobPat.heartRateMinMaxRange = "70-170";
        bobPat.heartRateHistory = "80,85,90,105,120,135,160,150,130,112";
        bobPat.age = "62";
        bobPat.gender = "Male";
        bobPat.bloodType = "O-";
        bobPat.phone = "(555) 014-9921";
        bobPat.email = "bob.thompson@gmail.com";
        bobPat.address = "456 Oak Avenue, ICU, Bed 1";
        bobPat.currentCondition = "Fever";
        patientDatabase.put("Bob Thompson", bobPat);

        saveDatabase();
        logActivity("Database seeded and initialized default records.");
    }

    public static void logActivity(String action) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        globalAuditLogs.add("[" + now.format(formatter) + "] " + action);
    }

    public static class UserAccount {
        public String username, password, roleType, fullName, roleTitle; // roleType: ADMIN or PATIENT

        public UserAccount(String username, String password, String roleType, String fullName, String roleTitle) {
            this.username = username;
            this.password = password;
            this.roleType = roleType;
            this.fullName = fullName;
            this.roleTitle = roleTitle;
        }
    }

    public static class PatientProfile {
        public String name, meta, heartRate, temperature, oxygen;
        public String weight;
        public String sleepHours, sleepMinutes;
        public String moveCal, exerciseMin, standHr;
        public String height;
        public String bmi;
        public String stateOfMind, stateOfMindBadge;
        public String currentCondition = "Healthy";
        public String fitPlanGoal;
        public String fitPlanFocus;
        public String fitPlanDays;
        public String fitPlanTime;
        public String calorieBurnt;
        public String calorieConsumed;
        public String weightHistory;
        public String mealsCalorieMap;
        public String stepCount;
        public String stepGoal;
        public String stepHistory;
        public String heartRateMinMaxRange = "60-185";
        public String heartRateHistory = "60,65,70,85,110,140,175,170,110,98";
        public String age = "";
        public String gender = "";
        public String bloodType = "";
        public String phone = "";
        public String email = "";
        public String address = "";
        public String oxygenHistory = "98,97,98,99,98,98";
        public List<String> clinicalNotes = new ArrayList<>();

        // Old constructor for compatibility (e.g. registration)
        public PatientProfile(String name, String meta, String hr, String temp, String ox) {
            this(name, meta, hr, temp, ox, "0", "0", "0", "0", "0", "0", "170", "0.0", "Calm", "Neutral", "0", "", "", "", "156", "0", "68,67.5,67,66.8", "breakfast=0;lunch=0;dinner=0", "5241", "10000", "4500,5000,5500,5241");
        }

        // Mid constructor for earlier updates
        public PatientProfile(String name, String meta, String hr, String temp, String ox,
                              String weight, String sleepHours, String sleepMinutes,
                              String moveCal, String exerciseMin, String standHr,
                              String height, String bmi, String stateOfMind, String stateOfMindBadge,
                              String fitPlanGoal, String fitPlanFocus, String fitPlanDays, String fitPlanTime) {
            this(name, meta, hr, temp, ox, weight, sleepHours, sleepMinutes, moveCal, exerciseMin, standHr, height, bmi, stateOfMind, stateOfMindBadge, fitPlanGoal, fitPlanFocus, fitPlanDays, fitPlanTime, "156", "0", weight + "," + weight + "," + weight, "breakfast=0;lunch=0;dinner=0", "5241", "10000", "4500,5000,5500,5241");
        }

        // Backwards compatibility for previous detailed updates
        public PatientProfile(String name, String meta, String hr, String temp, String ox,
                              String weight, String sleepHours, String sleepMinutes,
                              String moveCal, String exerciseMin, String standHr,
                              String height, String bmi, String stateOfMind, String stateOfMindBadge,
                              String fitPlanGoal, String fitPlanFocus, String fitPlanDays, String fitPlanTime,
                              String calorieBurnt, String calorieConsumed, String weightHistory, String mealsCalorieMap) {
            this(name, meta, hr, temp, ox, weight, sleepHours, sleepMinutes, moveCal, exerciseMin, standHr, height, bmi, stateOfMind, stateOfMindBadge, fitPlanGoal, fitPlanFocus, fitPlanDays, fitPlanTime, calorieBurnt, calorieConsumed, weightHistory, mealsCalorieMap, "5241", "10000", "4500,5000,5500,5241");
        }

        // New constructor for detailed data
        public PatientProfile(String name, String meta, String hr, String temp, String ox,
                              String weight, String sleepHours, String sleepMinutes,
                              String moveCal, String exerciseMin, String standHr,
                              String height, String bmi, String stateOfMind, String stateOfMindBadge,
                              String fitPlanGoal, String fitPlanFocus, String fitPlanDays, String fitPlanTime,
                              String calorieBurnt, String calorieConsumed, String weightHistory, String mealsCalorieMap,
                              String stepCount, String stepGoal, String stepHistory) {
            this.name = name; this.meta = meta; this.heartRate = hr; this.temperature = temp; this.oxygen = ox;
            this.weight = weight;
            this.sleepHours = sleepHours;
            this.sleepMinutes = sleepMinutes;
            this.moveCal = moveCal;
            this.exerciseMin = exerciseMin;
            this.standHr = standHr;
            this.height = height;
            this.bmi = bmi;
            this.stateOfMind = stateOfMind;
            this.stateOfMindBadge = stateOfMindBadge;
            this.fitPlanGoal = fitPlanGoal;
            this.fitPlanFocus = fitPlanFocus;
            this.fitPlanDays = fitPlanDays;
            this.fitPlanTime = fitPlanTime;
            this.calorieBurnt = calorieBurnt;
            this.calorieConsumed = calorieConsumed;
            this.weightHistory = weightHistory;
            this.mealsCalorieMap = mealsCalorieMap;
            this.stepCount = stepCount;
            this.stepGoal = stepGoal;
            this.stepHistory = stepHistory;
            this.clinicalNotes.add("User data matrix verified and mounted.");
        }
    }
}
