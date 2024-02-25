package me.ronygomes.ums.api.model;

public enum Grade {

    A_PLUS("A+", 4),
    A("A", 3.75f),
    A_MINUS("A-", 3.5f),
    B_PLUS("B+", 3.25f),
    B("B", 3),
    B_MINUS("B-", 2.75f),
    C_PLUS("C+", 2.5f),
    C("C", 2.25f),
    C_MINUS("C-", 2),
    F("F", 0);

    private final String letter;
    private final float gpa;

    Grade(String letter, float gpa) {
        this.letter = letter;
        this.gpa = gpa;
    }

    public String getLetter() {
        return letter;
    }

    public float getGpa() {
        return gpa;
    }

    public Grade convertPercentage(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0-100. Got: " + score);
        }

        if (score >= 80) {
            return Grade.A_PLUS;
        } else if (score >= 75) {
            return Grade.A;
        } else if (score >= 70) {
            return Grade.A_MINUS;
        } else if (score >= 65) {
            return Grade.B_PLUS;
        } else if (score >= 60) {
            return Grade.B;
        } else if (score >= 55) {
            return Grade.B_MINUS;
        } else if (score >= 50) {
            return Grade.C_PLUS;
        } else if (score >= 45) {
            return Grade.C;
        } else if (score >= 40) {
            return Grade.C_MINUS;
        } else {
            return Grade.F;
        }
    }
}
