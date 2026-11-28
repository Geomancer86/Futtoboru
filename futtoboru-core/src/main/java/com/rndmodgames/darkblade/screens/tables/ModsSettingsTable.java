package com.rndmodgames.darkblade.screens.tables;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.localization.Language;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Mods Settings Table v1
 *
 * The game language (U-12): the box shows the language in use; picking another saves it, loads it at once and runs
 * {@code afterLanguageChange} (Settings builds itself again in the new language).
 *
 * @author Geomancer86
 */
public class ModsSettingsTable extends VisTable {

	public ModsSettingsTable(Game game, Runnable afterLanguageChange) {

		VisLabel modsSettingsLabel = new VisLabel(LanguageModLoader.getValue("mods_settings"));

		VisLabel languageLabel = new VisLabel(LanguageModLoader.getValue("language"));

		/**
		 * Language Selectbox
		 */
		final VisSelectBox<Language> languagesSelectBox = new VisSelectBox<Language>();
		languagesSelectBox.setItems(LanguageModLoader.getAllAvailableLanguages());
		// the language in use, before the listener (setting it is not a change)
		languagesSelectBox.setSelected(LanguageModLoader.getCurrentLanguage());

		languagesSelectBox.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {

	        	Language language = languagesSelectBox.getSelected();

	        	if (language == null || language == LanguageModLoader.getCurrentLanguage()) {
	        		return;
	        	}

	        	Log.info(Log.UI_MENU, "SELECTED LANGUAGE: " + language.getId());

	        	if (game instanceof Futtoboru) {
	        		((Futtoboru) game).updateLanguage(language.getId());
	        	}
	        	if (afterLanguageChange != null) {
	        		afterLanguageChange.run();
	        	}
	        }
	    });

		row();

		add(modsSettingsLabel).pad(UiTheme.space("xs")).colspan(2).expandX().align(Align.center);

		row();

		// SET DEFAULT MASTER SOUND VALUE
		add(languageLabel).pad(UiTheme.space("xs")).fill().align(Align.center);
		add(languagesSelectBox).pad(UiTheme.space("xs")).fill().align(Align.center);

		row();
	}
}
