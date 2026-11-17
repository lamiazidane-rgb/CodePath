package com.pylo.data;

import com.pylo.service.ActivityItem;
import com.pylo.service.ActivityType;
import com.pylo.service.Badge;
import com.pylo.service.LearnerDashboardService;
import com.pylo.service.LevelProgress;
import com.pylo.service.LevelStatus;
import com.pylo.service.Period;
import com.pylo.service.PeriodStats;

import java.util.List;

public final class MockLearnerDashboardService implements LearnerDashboardService {

    @Override
    public String getCurrentCourse() {
        return "Les fonctions en Python";
    }

    @Override
    public PeriodStats getStats(Period period) {
        return switch (period) {
            case WEEK -> new PeriodStats(2, 6, 8, 15);
            case MONTH -> new PeriodStats(8, 12, 24, 40);
            case YEAR -> new PeriodStats(24, 30, 96, 120);
        };
    }

    @Override
    public List<LevelProgress> getLevels() {
        return List.of(
                new LevelProgress("Débutant", "Les bases de Python", "fth-code",
                        LevelStatus.VALIDATED, 100),
                new LevelProgress("Intermédiaire", "Construire des programmes", "fth-layers",
                        LevelStatus.IN_PROGRESS, 65),
                new LevelProgress("Avancé", "Maîtriser Python", "fth-award",
                        LevelStatus.LOCKED, 0));
    }

    @Override
    public List<ActivityItem> getRecentActivities() {
        return List.of(
                new ActivityItem("Exercice réussi · Les fonctions", "Aujourd'hui · 14:32",
                        ActivityType.EXERCISE),
                new ActivityItem("Cours terminé · Les listes", "Hier · 18:10",
                        ActivityType.COURSE),
                new ActivityItem("Test validé · Débutant", "12 juin · 11:45",
                        ActivityType.TEST));
    }

    @Override
    public List<Badge> getBadges() {
        return List.of(
                new Badge("Premier pas", "fth-award"),
                new Badge("Curieux", "fth-compass"));
    }
}
