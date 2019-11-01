package com.thomaskuenneth.souffleur.server;

public class SlideNotes {

    private String name;
    private String[] notes;
    private int slideNumber;
    private int total;

    public SlideNotes(String name, String[] notes, int slideNumber, int total) {
        this.name = name;
        this.notes = notes;
        this.slideNumber = slideNumber;
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getNotes() {
        return notes;
    }

    public void setNotes(String[] notes) {
        this.notes = notes;
    }

    public int getSlideNumber() {
        return slideNumber;
    }

    public void setSlideNumber(int slideNumber) {
        this.slideNumber = slideNumber;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
