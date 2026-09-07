package com.tutorial.androidgametutorial.main;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

/** Runtime EN/VI localization independent from the device language. */
public final class LanguageManager {

    private static final String PREFS_NAME = "language_settings";
    private static final String KEY_VIETNAMESE = "vietnamese_enabled";
    private static final Locale VIETNAMESE_LOCALE = new Locale("vi", "VN");

    private final SharedPreferences preferences;
    private volatile boolean vietnamese;

    public LanguageManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(
                PREFS_NAME, Context.MODE_PRIVATE
        );
        vietnamese = preferences.getBoolean(KEY_VIETNAMESE, false);
    }

    public boolean isVietnamese() {
        return vietnamese;
    }

    public void toggle() {
        setVietnamese(!vietnamese);
    }

    public void setVietnamese(boolean enabled) {
        if (vietnamese == enabled) return;
        vietnamese = enabled;
        preferences.edit().putBoolean(KEY_VIETNAMESE, enabled).apply();
    }

    public String text(String english, String vietnameseText) {
        return vietnamese ? vietnameseText : english;
    }

    public String upper(String english, String vietnameseText) {
        Locale locale = vietnamese ? VIETNAMESE_LOCALE : Locale.US;
        return text(english, vietnameseText).toUpperCase(locale);
    }

    public String languageSettingLabel() {
        return vietnamese ? "NGÔN NGỮ: TIẾNG VIỆT" : "LANGUAGE: ENGLISH";
    }

    public String difficulty(Game.Difficulty difficulty) {
        if (!vietnamese) return difficulty.name();
        return difficulty == Game.Difficulty.HARD ? "KHÓ" : "DỄ";
    }

    public String characterName(LoadoutManager.CharacterType character) {
        if (!vietnamese) return character.getDisplayName();
        return switch (character) {
            case WARRIOR -> "Chiến binh";
            case ROGUE -> "Sát thủ";
            case GUARDIAN -> "Hộ vệ";
            case ADVENTURER -> "Nhà thám hiểm";
        };
    }

    public String weaponName(LoadoutManager.WeaponType weapon) {
        if (!vietnamese) return weapon.getDisplayName();
        return switch (weapon) {
            case BROADSWORD -> "Đại kiếm";
            case TWIN_DAGGERS -> "Song đao";
            case FROST_SPEAR -> "Thương băng";
        };
    }

    public String skillName(LoadoutManager.SkillType skill) {
        if (!vietnamese) return skill.getDisplayName();
        return switch (skill) {
            case CHARGED_BLAST -> "Xung kích";
            case SPARK_STORM -> "Bão sét";
            case ARCANE_BURST -> "Bùng nổ bí thuật";
            case FROST_PULSE -> "Xung băng";
            case SACRIFICE_NOVA -> "Tân tinh hiến tế";
            case FLAME_RIFT -> "Khe nứt lửa";
            case VOID_DRAIN -> "Hút hư không";
            case GLACIAL_RING -> "Vòng băng giá";
            case COMET_SHOT -> "Mưa sao chổi";
            case OVERLOAD -> "Quá tải";
        };
    }

    public String skillDescription(LoadoutManager.SkillType skill) {
        if (!vietnamese) return skill.getDescription();
        return switch (skill) {
            case CHARGED_BLAST -> "45 sát thương";
            case SPARK_STORM -> "Sét đánh nhiều lần";
            case ARCANE_BURST -> "80 sát thương diện rộng";
            case FROST_PULSE -> "3 tia băng + làm chậm";
            case SACRIFICE_NOVA -> "220 sát thương, tốn 100 máu";
            case FLAME_RIFT -> "130 sát thương diện rộng";
            case VOID_DRAIN -> "95 sát thương + hồi 60 máu";
            case GLACIAL_RING -> "8 tia băng + làm chậm";
            case COMET_SHOT -> "4 sao chổi bắn thẳng";
            case OVERLOAD -> "6 tia sét liên tiếp, tốn 130 máu";
        };
    }
}
