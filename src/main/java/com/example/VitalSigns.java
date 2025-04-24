package com.example;


import java.time.LocalDateTime;
import java.util.Objects;

public class VitalSigns {

    private LocalDateTime timestamp;
    private double bodyTemperature; // in Celsius
    private int pulseRate; // beats per minute
    private int respiratoryRate; // breaths per minute
    private BloodPressure bloodPressure;
    private double oxygenSaturation; // SpO2 percentage
    private Double height; // in centimeters (optional)
    private Double weight; // in kilograms (optional)
    private Double bmi; // calculated if height/weight provided
    private PainLevel painLevel;




    // Constructor for initializing mandatory fields
    public VitalSigns(LocalDateTime timestamp, double bodyTemperature, int pulseRate, int respiratoryRate,
     BloodPressure bloodPressure, double oxygenSaturation, Double height, Double weight) {
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.bodyTemperature = validateTemperature(bodyTemperature);
        this.pulseRate = validatePulseRate(pulseRate);
        this.respiratoryRate = validateRespiratoryRate(respiratoryRate);
        this.bloodPressure = Objects.requireNonNull(bloodPressure, "Blood pressure cannot be null");
        this.oxygenSaturation = validateOxygenSaturation(oxygenSaturation);
        this.painLevel = PainLevel.NONE; // Default value
        this.height=height;
        this.weight=weight;
        this.bmi=calculateBMI(this.height, this.weight);
    }



    //  VALIDATION METHODS 
    private double validateTemperature(double temperature) {
        if (temperature < 25.0 || temperature > 43.0) { // Extreme human survival limits
            throw new IllegalArgumentException("Invalid body temperature: " + temperature + "°C");
        }
        return temperature;
    }

    private int validatePulseRate(int pulse) {
        if (pulse < 20 || pulse > 250) { // Extreme physiological limits
            throw new IllegalArgumentException("Invalid pulse rate: " + pulse + " bpm");
        }
        return pulse;
    }

    private int validateRespiratoryRate(int rate) {
        if (rate < 4 || rate > 60) { // Extreme physiological limits
            throw new IllegalArgumentException("Invalid respiratory rate: " + rate + " breaths/min");
        }
        return rate;
    }

    private double validateOxygenSaturation(double spo2) {
        if (spo2 < 50 || spo2 > 100) {
            throw new IllegalArgumentException("Invalid oxygen saturation: " + spo2 + "%");
        }
        return spo2;
    }



    //calculation bmi

    private Double calculateBMI(Double height, Double weight) {
        if (height == null || weight == null) return null;
        if (height <= 0 || weight <= 0) return null;
        return weight / Math.pow(height / 100, 2); // Convert cm to m
    }



    //  CRITICAL VALUE CHECK
    public boolean isCritical() {
        return bodyTemperature > 39.0 || bodyTemperature < 35.0 || // Hyper/hypothermia
               pulseRate > 120 || pulseRate < 50 || // Tachycardia/bradycardia
               respiratoryRate > 24 || respiratoryRate < 10 || // Tachypnea/bradypnea
               bloodPressure.isCritical() ||
               oxygenSaturation < 92.0;
    }




    // GETTERS AND SETTERS
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getBodyTemperature() {
        return bodyTemperature;
    }

    

    public int getPulseRate() {
        return pulseRate;
    }


    public int getRespiratoryRate() {
        return respiratoryRate;
    }


    public BloodPressure getBloodPressure() {
        return bloodPressure;
    }


    public double getOxygenSaturation() {
        return oxygenSaturation;
    }



    public Double getHeight() {
        return height;
    }


    public Double getWeight() {
        return weight;
    }



    public Double getBmi() {
        return bmi;
    }

    public PainLevel getPainLevel() {
        return painLevel;
    }

    public void setPainLevel(PainLevel painLevel) {
        this.painLevel = painLevel;
    }






//  UTILITY METHODS
public String getSummary() {
    return String.format(
        "Vital Signs [%s]: Temp %.1f°C, Pulse %d bpm, Resp %d/min, BP %s, SpO2 %.1f%%",
        timestamp.toLocalTime(),
        bodyTemperature,
        pulseRate,
        respiratoryRate,
        bloodPressure.getBloodPressureReading(),
        oxygenSaturation
    );
}


//getting specific component of vital Signs

public double getComponent(Component component) {
    return switch (component) {
        case BODY_TEMPERATURE -> getBodyTemperature();
        case PULSE_RATE -> getPulseRate();
        case RESPIRATORY_RATE -> getRespiratoryRate();
        case SYSTOLIC_BP -> getBloodPressure().getSystolic();
        case DIASTOLIC_BP -> getBloodPressure().getDiastolic();
        case OXYGEN_SATURATION -> getOxygenSaturation();
        case BMI -> getBmi() != null ? getBmi() : 0.0;
    };


}
//enum constants for component
public enum Component {
    BODY_TEMPERATURE,
    PULSE_RATE,
    RESPIRATORY_RATE,
    SYSTOLIC_BP,
    DIASTOLIC_BP,
    OXYGEN_SATURATION,
    BMI
}



@Override
public String toString() {
    StringBuilder sb = new StringBuilder("Vital Signs:\n");
    sb.append(String.format("- Temperature: %.1f°C\n", bodyTemperature));
    sb.append(String.format("- Pulse: %d bpm\n", pulseRate));
    sb.append(String.format("- Respiration: %d breaths/min\n", respiratoryRate));
    sb.append(String.format("- Blood Pressure: %s (%s)\n", 
        bloodPressure.getBloodPressureReading(), 
        bloodPressure.getCategory().getDescription()));
    sb.append(String.format("- Oxygen Saturation: %.1f%%\n", oxygenSaturation));
    if (height != null) sb.append(String.format("- Height: %.1f cm\n", height));
    if (weight != null) sb.append(String.format("- Weight: %.1f kg\n", weight));
    if (bmi != null) sb.append(String.format("- BMI: %.1f\n", bmi));
    sb.append(String.format("- Pain Level: %s\n", painLevel.getDescription()));
    return sb.toString();
}




    // BLOOD PRESSURE INNER CLASS 
    public static class BloodPressure {
        private final int systolic; // mmHg
        private final int diastolic; // mmHg
        private final Category category;

        public BloodPressure(int systolic, int diastolic) {
            if (systolic <= 0 || diastolic <= 0 || systolic < diastolic) {
                throw new IllegalArgumentException("Invalid blood pressure values");
            }
            this.systolic = systolic;
            this.diastolic = diastolic;
            this.category = determineCategory(systolic, diastolic);
        }

        private Category determineCategory(int systolic, int diastolic) {
            if (systolic >= 180 || diastolic >= 120) return Category.HYPERTENSIVE_CRISIS;
            if (systolic >= 140 || diastolic >= 90) return Category.STAGE_2_HYPERTENSION;
            if (systolic >= 130 || diastolic >= 80) return Category.STAGE_1_HYPERTENSION;
            if (systolic >= 120) return Category.ELEVATED;
            return Category.NORMAL;
        }

        public boolean isCritical() {
            return category == Category.HYPERTENSIVE_CRISIS ||
                   systolic < 90 || diastolic < 60; // Hypotension
        }

        public String getBloodPressureReading() {
            return systolic + "/" + diastolic + " mmHg";
        }




        //category constants
        public enum Category {
            NORMAL("Normal"),
            ELEVATED("Elevated"),
            STAGE_1_HYPERTENSION("Stage 1 Hypertension"),
            STAGE_2_HYPERTENSION("Stage 2 Hypertension"),
            HYPERTENSIVE_CRISIS("Hypertensive Crisis");

            private final String description;

            Category(String description) {
                this.description = description;
            }

            public String getDescription() {
                return description;
            }
        }

        // Getters
        public int getSystolic() { return systolic; }
        public int getDiastolic() { return diastolic; }
        public Category getCategory() { return category; }
    }




    //  PAIN LEVEL ENUM
    public enum PainLevel {
        NONE(0, "No pain"),
        MILD(1, "Mild pain"),
        MODERATE(2, "Moderate pain"),
        SEVERE(3, "Severe pain"),
        VERY_SEVERE(4, "Very severe pain"),
        WORST_POSSIBLE(5, "Worst possible pain");

        private final int level;
        private final String description;

        PainLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }

    
}
