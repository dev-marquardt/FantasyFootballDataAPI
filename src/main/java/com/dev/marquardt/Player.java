package com.dev.marquardt;

public class Player {
    private String number = null;
    private String firstName = null;
    private String lastName = null;
    private String team = null;
    private String position = null;
    private int age;
    private int draftYear;
    private int seasonsPlayed;

    public Player(){
        number = null;
        firstName = "";
        lastName = "";
        team = "";
        position = "";
        age = 0;
        draftYear = 0;
        seasonsPlayed = 0;
    }

    public Player(final String numberInput, final String firstNameInput, final String lastNameInput, final String teamInput, final String positionInput, final int ageInput, final int draftYearInput, final int seasonsPlayedInput) {
        number = numberInput;
        firstName = firstNameInput;
        lastName = lastNameInput;
        team = teamInput;
        position = positionInput;
        age = ageInput;
        draftYear = draftYearInput;
        seasonsPlayed = seasonsPlayedInput;
    }

    public String getNumber() {
        return number;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTeam() {
        return team;
    }

    public String getPosition() {
        return position;
    }

    public int getAge() {
        return age;
    }

    public int getDraftYear() {
        return draftYear;
    }

    public int getSeasonsPlayed() {
        return seasonsPlayed;
    }

    public void setNumber(String numberInput) {
        number = numberInput;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public void setTeam(final String team) {
        this.team = team;
    }

    public void setPosition(final String position) {
        this.position = position;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public void setDraftYear(final int draftYear) {
        this.draftYear = draftYear;
    }

    public void setSeasonsPlayed(final int seasonsPlayed) {
        this.seasonsPlayed = seasonsPlayed;
    }
}
