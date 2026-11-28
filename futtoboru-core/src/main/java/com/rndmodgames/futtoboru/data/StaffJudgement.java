package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * R-09 (docs/design/RECRUITMENT_AND_TRIALS.md §4.1, §7): how well a person judges a footballer, for the perception of
 * R-04. Four judging skills on the attribute scale (3..23), one bias and the regions he knows men in. Seeded once per
 * person ({@code engine.recruitment.StaffRoles}) and saved with him (an optional field: old saves get it on load).
 */
public class StaffJudgement implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The biases of §4.1: a committee man's local lad, the trainer's pace, size, the Scots, flair. */
    public static final String BIAS_LOCAL = "local";
    public static final String BIAS_PACE = "pace";
    public static final String BIAS_SIZE = "size";
    public static final String BIAS_SCOTS = "scots";
    public static final String BIAS_FLAIR = "flair";
    public static final List<String> BIASES = List.of(BIAS_LOCAL, BIAS_PACE, BIAS_SIZE, BIAS_SCOTS, BIAS_FLAIR);

    public static final int MIN = 3;
    public static final int MAX = 23;

    private Integer physical;
    private Integer technical;
    private Integer mental;
    private Integer keeper;
    private String bias;
    /** pool region data ids he knows men in (his own region first) */
    private List<String> network = new ArrayList<>();

    public StaffJudgement() {
    }

    public StaffJudgement(int physical, int technical, int mental, int keeper, String bias, List<String> network) {
        this.physical = physical;
        this.technical = technical;
        this.mental = mental;
        this.keeper = keeper;
        this.bias = bias;
        this.network = new ArrayList<>(network);
    }

    public int getPhysical() {
        return physical != null ? physical : MIN;
    }

    public int getTechnical() {
        return technical != null ? technical : MIN;
    }

    public int getMental() {
        return mental != null ? mental : MIN;
    }

    public int getKeeper() {
        return keeper != null ? keeper : MIN;
    }

    /** @return one of {@link #BIASES} */
    public String getBias() {
        return bias;
    }

    public List<String> getNetwork() {
        if (network == null) {
            network = new ArrayList<>();
        }
        return network;
    }

    @Override
    public String toString() {
        return "StaffJudgement[" + physical + "/" + technical + "/" + mental + "/" + keeper + " " + bias + " "
                + network + "]";
    }
}
