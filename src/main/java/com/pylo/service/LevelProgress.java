package com.pylo.service;

public record LevelProgress(
        String name,
        String subtitle,
        String icon,
        LevelStatus status,
        int percent) {
}
