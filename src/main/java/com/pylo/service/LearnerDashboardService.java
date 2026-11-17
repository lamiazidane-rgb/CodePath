package com.pylo.service;

import java.util.List;

/** Contrat des données du tableau de bord. Plus tard : implémentation JDBC. */
public interface LearnerDashboardService {
    String getCurrentCourse();
    PeriodStats getStats(Period period);
    List<LevelProgress> getLevels();
    List<ActivityItem> getRecentActivities();
    List<Badge> getBadges();
}