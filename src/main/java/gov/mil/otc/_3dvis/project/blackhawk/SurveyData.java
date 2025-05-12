package gov.mil.otc._3dvis.project.blackhawk;

public class SurveyData {

    private final String mission;
    private final int tailNumber;
    private final String pin;
    private final String role;
    private final String seat;
    private final String question;
    private final String answer;

    public SurveyData(String mission, int tailNumber, String pin, String role, String seat,
                         String question, String answer) {
        this.mission = mission;
        this.tailNumber = tailNumber;
        this.pin = pin;
        this.role = role;
        this.seat = seat;
        this.question = question;
        this.answer = answer;
    }

    public String getMission() {
        return mission;
    }

    public int getTailNumber() {
        return tailNumber;
    }

    public String getPin() {
        return pin;
    }

    public String getRole() {
        return role;
    }

    public String getSeat() {
        return seat;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}
