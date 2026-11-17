package com.pylo.service;

import com.pylo.data.MockLearnerDashboardService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * État observable du tableau de bord apprenant.
 * Toute page (Exercices, Cours...) peut le modifier : le dashboard se met à jour tout seul.
 * Exemple : DashboardState.getInstance().addActivity("Exercice réussi", ActivityType.EXERCISE);
 */
public final class DashboardState {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale.FRENCH);

    // Pour passer à la vraie BD : remplacer MockLearnerDashboardService
    private static final DashboardState INSTANCE = new DashboardState(new MockLearnerDashboardService());

    private final LearnerDashboardService service;
    private final ObservableList<LevelProgress> levels = FXCollections.observableArrayList();
    private final ObservableList<ActivityItem> activities = FXCollections.observableArrayList();
    private final ObservableList<Badge> badges = FXCollections.observableArrayList();
    private final ObjectProperty<Period> period = new SimpleObjectProperty<>(Period.WEEK);
    private final ObjectProperty<PeriodStats> stats = new SimpleObjectProperty<>();
    private final StringProperty currentCourse = new SimpleStringProperty();

    private DashboardState(LearnerDashboardService service) {
        this.service = service;
        reload();
    }

    public static DashboardState getInstance() { return INSTANCE; }

    /* ---------- Lecture ---------- */

    public ObservableList<LevelProgress> getLevels() { return levels; }
    public ObservableList<ActivityItem> getActivities() { return activities; }
    public ObservableList<Badge> getBadges() { return badges; }
    public ObjectProperty<Period> periodProperty() { return period; }
    public ObjectProperty<PeriodStats> statsProperty() { return stats; }
    public StringProperty currentCourseProperty() { return currentCourse; }

    public int currentLevelPercent() {
        int index = currentLevelIndex();
        if (index >= 0) {
            return levels.get(index).percent();
        }
        return allValidated() ? 100 : 0;
    }

    public String currentLevelName() {
        int index = currentLevelIndex();
        if (index >= 0) {
            return levels.get(index).name();
        }
        return allValidated() ? levels.get(levels.size() - 1).name() : "—";
    }

    /* ---------- Actions ---------- */

    /** Recharge tout depuis le service. */
    public void reload() {
        levels.setAll(service.getLevels());
        activities.setAll(service.getRecentActivities());
        badges.setAll(service.getBadges());
        currentCourse.set(service.getCurrentCourse());
        stats.set(service.getStats(period.get()));
    }

    public void setPeriod(Period newPeriod) {
        period.set(newPeriod);
        stats.set(service.getStats(newPeriod));
    }

    /** Ajoute une activité en tête de liste (la plus récente en premier). */
    public void addActivity(String text, ActivityType type) {
        activities.add(0, new ActivityItem(text, LocalDateTime.now().format(DATE_FORMAT), type));
    }

    /** Démo : +10 % au niveau en cours ; à 100 %, il est validé et le suivant se déverrouille. */
    public void simulateProgress() {
        int index = currentLevelIndex();
        if (index < 0) {
            return;
        }
        List<LevelProgress> updated = new ArrayList<>(levels);
        LevelProgress level = levels.get(index);
        int newPercent = Math.min(100, level.percent() + 10);

        if (newPercent >= 100) {
            updated.set(index, withState(level, LevelStatus.VALIDATED, 100));
            if (index + 1 < updated.size()) {
                updated.set(index + 1, withState(updated.get(index + 1), LevelStatus.IN_PROGRESS, 0));
            }
            addActivity("Niveau « " + level.name() + " » validé", ActivityType.TEST);
            badges.add(new Badge("Niveau " + level.name(), "fth-award"));
        } else {
            updated.set(index, withState(level, LevelStatus.IN_PROGRESS, newPercent));
            addActivity("Exercice réussi · " + level.name(), ActivityType.EXERCISE);
        }
        levels.setAll(updated);   // un seul événement de changement

        PeriodStats s = stats.get();
        stats.set(new PeriodStats(s.coursesDone(), s.coursesTotal(),
                Math.min(s.exercisesTotal(), s.exercisesDone() + 1), s.exercisesTotal()));
    }

    /* ---------- Utilitaires ---------- */

    private LevelProgress withState(LevelProgress level, LevelStatus status, int percent) {
        return new LevelProgress(level.name(), level.subtitle(), level.icon(), status, percent);
    }

    private int currentLevelIndex() {
        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).status() == LevelStatus.IN_PROGRESS) {
                return i;
            }
        }
        return -1;
    }

    private boolean allValidated() {
        return !levels.isEmpty() && levels.stream().allMatch(l -> l.status() == LevelStatus.VALIDATED);
    }
}