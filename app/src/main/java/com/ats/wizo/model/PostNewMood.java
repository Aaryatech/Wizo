package com.ats.wizo.model;

import java.util.List;

public class PostNewMood {

    private MoodHeader  moodHeader;

    private List<MoodDetail> moodDetailList;


    public MoodHeader getMoodHeader() {
        return moodHeader;
    }

    public void setMoodHeader(MoodHeader moodHeader) {
        this.moodHeader = moodHeader;
    }

    public List<MoodDetail> getMoodDetailList() {
        return moodDetailList;
    }

    public void setMoodDetailList(List<MoodDetail> moodDetailList) {
        this.moodDetailList = moodDetailList;
    }


    @Override
    public String toString() {
        return "PostNewMood{" +
                "moodHeader=" + moodHeader +
                ", moodDetailList=" + moodDetailList +
                '}';
    }
}
