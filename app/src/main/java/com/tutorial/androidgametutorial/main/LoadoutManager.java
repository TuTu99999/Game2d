package com.tutorial.androidgametutorial.main;

import android.content.Context;
import android.content.SharedPreferences;

import com.tutorial.androidgametutorial.R;

public class LoadoutManager {

    private static final String PREFS_NAME = "player_loadout";
    private static final String KEY_CHARACTER = "character";
    private static final String KEY_WEAPON = "weapon";
    private static final String KEY_SKILL_PREFIX = "skill_";

    public enum CharacterType {
        WARRIOR("Warrior", 600, 1.00f, 1.00f, 0, 2,
                R.drawable.warrior_portrait),
        ROGUE("Rogue", 500, 1.18f, 1.12f, 1, 2,
                R.drawable.rogue_portrait),
        GUARDIAN("Guardian", 700, 0.90f, 0.92f, 2, 2,
                R.drawable.guardian_portrait),
        ADVENTURER("Adventurer", 550, 1.08f, 1.08f, 0, 0,
                R.drawable.adventurer_portrait);

        private final String displayName;
        private final int maxHealth;
        private final float speedMultiplier;
        private final float damageMultiplier;
        private final int iconColumn;
        private final int iconRow;
        private final int iconResourceId;

        CharacterType(String displayName, int maxHealth, float speedMultiplier,
                      float damageMultiplier, int iconColumn, int iconRow,
                      int iconResourceId) {
            this.displayName = displayName;
            this.maxHealth = maxHealth;
            this.speedMultiplier = speedMultiplier;
            this.damageMultiplier = damageMultiplier;
            this.iconColumn = iconColumn;
            this.iconRow = iconRow;
            this.iconResourceId = iconResourceId;
        }

        public String getDisplayName() { return displayName; }
        public int getMaxHealth() { return maxHealth; }
        public float getSpeedMultiplier() { return speedMultiplier; }
        public float getDamageMultiplier() { return damageMultiplier; }
        public int getIconColumn() { return iconColumn; }
        public int getIconRow() { return iconRow; }
        public int getIconResourceId() { return iconResourceId; }
    }

    public enum WeaponType {
        BROADSWORD("Broadsword", 18, 450L, 1.00f, 0, 0),
        TWIN_DAGGERS("Twin Daggers", 11, 230L, 1.55f, 1, 0),
        FROST_SPEAR("Frost Spear", 28, 800L, 0.75f, 2, 0);

        private final String displayName;
        private final int damage;
        private final long cooldownMs;
        private final float soundRate;
        private final int iconColumn;
        private final int iconRow;

        WeaponType(String displayName, int damage, long cooldownMs, float soundRate,
                   int iconColumn, int iconRow) {
            this.displayName = displayName;
            this.damage = damage;
            this.cooldownMs = cooldownMs;
            this.soundRate = soundRate;
            this.iconColumn = iconColumn;
            this.iconRow = iconRow;
        }

        public String getDisplayName() { return displayName; }
        public int getDamage() { return damage; }
        public long getCooldownMs() { return cooldownMs; }
        public float getSoundRate() { return soundRate; }
        public int getIconColumn() { return iconColumn; }
        public int getIconRow() { return iconRow; }
    }

    public enum SkillType {
        CHARGED_BLAST("Charged Blast", "45 damage", 45, 2500L, 0,
                R.drawable.charged1),
        SPARK_STORM("Spark Storm", "Multi-hit lightning", 30, 5000L, 0,
                R.drawable.spark_preview1),
        ARCANE_BURST("Arcane Burst", "80 area damage", 80, 6500L, 0,
                R.drawable.hits_45),
        FROST_PULSE("Frost Pulse", "3-way frost shot + slow", 75, 5000L, 0,
                R.drawable.pulse1),
        SACRIFICE_NOVA("Sacrifice Nova", "220 damage, costs 100 HP", 220, 12000L, 100,
                R.drawable.hits_14),
        FLAME_RIFT("Flame Rift", "130 area damage", 130, 8500L, 0,
                R.drawable.hits_42),
        VOID_DRAIN("Void Drain", "95 damage + heal 60 HP", 95, 9000L, 0,
                R.drawable.hits_12),
        GLACIAL_RING("Glacial Ring", "8-way frost shot + slow", 55, 7500L, 0,
                R.drawable.pulse3),
        COMET_SHOT("Comet Shot", "4-comet forward barrage", 150, 9000L, 0,
                R.drawable.charged5),
        OVERLOAD("Overload", "6-shot lightning stream, costs 130 HP", 260, 15000L, 130,
                R.drawable.spark_preview4);

        private final String displayName;
        private final String description;
        private final int damage;
        private final long cooldownMs;
        private final int healthCost;
        private final int iconResourceId;

        SkillType(String displayName, String description, int damage, long cooldownMs,
                  int healthCost, int iconResourceId) {
            this.displayName = displayName;
            this.description = description;
            this.damage = damage;
            this.cooldownMs = cooldownMs;
            this.healthCost = healthCost;
            this.iconResourceId = iconResourceId;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getDamage() { return damage; }
        public long getCooldownMs() { return cooldownMs; }
        public int getHealthCost() { return healthCost; }
        public int getIconResourceId() { return iconResourceId; }
    }

    private final SharedPreferences preferences;
    private CharacterType character;
    private WeaponType weapon;
    private final SkillType[] skills = new SkillType[3];

    public LoadoutManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        character = readEnum(KEY_CHARACTER, CharacterType.WARRIOR, CharacterType.class);
        weapon = readEnum(KEY_WEAPON, WeaponType.BROADSWORD, WeaponType.class);
        skills[0] = readEnum(KEY_SKILL_PREFIX + 0, SkillType.CHARGED_BLAST, SkillType.class);
        skills[1] = readEnum(KEY_SKILL_PREFIX + 1, SkillType.SPARK_STORM, SkillType.class);
        skills[2] = readEnum(KEY_SKILL_PREFIX + 2, SkillType.ARCANE_BURST, SkillType.class);
        removeDuplicateSkills();
    }

    private <T extends Enum<T>> T readEnum(String key, T fallback, Class<T> enumClass) {
        try {
            return Enum.valueOf(enumClass, preferences.getString(key, fallback.name()));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void removeDuplicateSkills() {
        SkillType[] defaults = {
                SkillType.CHARGED_BLAST, SkillType.SPARK_STORM, SkillType.ARCANE_BURST
        };
        for (int i = 0; i < skills.length; i++) {
            for (int j = 0; j < i; j++) {
                if (skills[i] == skills[j]) skills[i] = defaults[i];
            }
        }
    }

    public CharacterType getCharacter() { return character; }
    public WeaponType getWeapon() { return weapon; }
    public SkillType getSkill(int slot) { return skills[Math.max(0, Math.min(2, slot))]; }

    public int findSkillSlot(SkillType skill) {
        for (int i = 0; i < skills.length; i++) {
            if (skills[i] == skill) return i;
        }
        return -1;
    }

    public void setCharacter(CharacterType character) {
        this.character = character;
        preferences.edit().putString(KEY_CHARACTER, character.name()).apply();
    }

    public void setWeapon(WeaponType weapon) {
        this.weapon = weapon;
        preferences.edit().putString(KEY_WEAPON, weapon.name()).apply();
    }

    public void setSkill(int slot, SkillType skill) {
        if (slot < 0 || slot >= skills.length || findSkillSlot(skill) >= 0) return;
        skills[slot] = skill;
        preferences.edit().putString(KEY_SKILL_PREFIX + slot, skill.name()).apply();
    }

    public void assignSkillToSlot(int targetSlot, SkillType skill) {
        if (targetSlot < 0 || targetSlot >= skills.length || skill == null) return;

        int currentSlot = findSkillSlot(skill);
        if (currentSlot == targetSlot) return;

        SharedPreferences.Editor editor = preferences.edit();
        if (currentSlot >= 0) {
            SkillType skillBeingReplaced = skills[targetSlot];
            skills[targetSlot] = skill;
            skills[currentSlot] = skillBeingReplaced;
            editor.putString(KEY_SKILL_PREFIX + currentSlot, skillBeingReplaced.name());
        } else {
            skills[targetSlot] = skill;
        }
        editor.putString(KEY_SKILL_PREFIX + targetSlot, skill.name()).apply();
    }
}
