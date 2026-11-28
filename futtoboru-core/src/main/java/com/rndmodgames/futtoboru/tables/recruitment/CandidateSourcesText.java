package com.rndmodgames.futtoboru.tables.recruitment;

import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.recruitment.Newspaper;
import com.rndmodgames.futtoboru.data.recruitment.ShortlistEntry;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.recruitment.Shortlists;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.data.RecruitmentData;
import com.rndmodgames.localization.LanguageModLoader;

/** R-05: the shown text of a shortlist entry's source and suggester, a man's name, a paper in the advert box. */
final class CandidateSourcesText {

    /** bundle key prefix of a source ({@code recruit_source_letter}) */
    static final String SOURCE_KEY = "recruit_source_";

    private CandidateSourcesText() {
    }

    static String name(Person person) {
        return ((person.getName() == null ? "" : person.getName()) + " "
                + (person.getLastname() == null ? "" : person.getLastname())).trim();
    }

    /** "His letter", "Advert (Athletic News)", "Tip", "Agent", "Not retained (Stoke)", "Our choice" */
    static String source(ShortlistEntry e) {
        String source = e.getSource() != null ? e.getSource() : ShortlistEntry.OWN;
        String label = LanguageModLoader.getValue(SOURCE_KEY + source);
        if (ShortlistEntry.ADVERT.equals(source) || ShortlistEntry.RELEASED.equals(source)) {
            String detail = e.getDetail();
            if (ShortlistEntry.ADVERT.equals(source)) {
                RecruitmentData data = DatabaseLoader.getInstance().getRecruitmentData();
                Newspaper n = data != null && detail != null ? data.getNewspapers().get(detail) : null;
                detail = n != null ? n.name() : detail;
            }
            return detail == null ? label : LanguageModLoader.format("recruit_source_with", label, detail);
        }
        return label;
    }

    /** "Trainer Jack Hill", "Mr J. McIntyre", the paper, or "-" for his own letter or our choice */
    static String suggestedBy(ShortlistEntry e) {
        if (e.getSuggestedBy() == null) {
            return "-";
        }
        if (e.getSuggestedRole() != null) {
            return LanguageModLoader.format("recruit_suggested_role",
                    LanguageModLoader.getValue(PerceptionCard.ROLE_KEY + e.getSuggestedRole()), e.getSuggestedBy());
        }
        return e.getSuggestedBy();
    }

    /** "Athletic News (Manchester, Lancashire) 0.15" */
    static String paper(Newspaper n) {
        RecruitmentData data = DatabaseLoader.getInstance().getRecruitmentData();
        String region = data != null && data.getPools().get(n.region()) != null ? data.getPools().get(n.region()).name()
                : n.region();
        return LanguageModLoader.format("shortlist_advert_paper_line", n.name(), n.place(), region,
                Money.format(Shortlists.pounds(n.costPence())));
    }
}
