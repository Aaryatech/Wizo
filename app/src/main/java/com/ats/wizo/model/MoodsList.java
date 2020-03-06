package com.ats.wizo.model;

import java.util.List;

public class MoodsList {


    private MoodHeader moodHeader;
    private List<MoodDetailList> moodDetailList = null;


    public MoodHeader getMoodHeader() {
        return moodHeader;
    }

    public void setMoodHeader(MoodHeader moodHeader) {
        this.moodHeader = moodHeader;
    }

    public List<MoodDetailList> getMoodDetailList() {
        return moodDetailList;
    }

    public void setMoodDetailList(List<MoodDetailList> moodDetailList) {
        this.moodDetailList = moodDetailList;
    }

    @Override
    public String toString() {
        return "MoodsList{" +
                "moodHeader=" + moodHeader +
                ", moodDetailList=" + moodDetailList +
                '}';
    }
}
