package com.rndmodgames.darkblade.screens.tables;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.localization.Language;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.ui.UiTheme;

/**
 * Mods Settings Table v1
 * 
 * @author Geomancer86
 */
public class ModsSettingsTable extends VisTable {

	public ModsSettingsTable(Game game) {
		
		VisLabel modsSettingsLabel = new VisLabel(LanguageModLoader.getValue("mods_settings"));
		
		VisLabel languageLabel = new VisLabel(LanguageModLoader.getValue("language"));
		
		/**
		 * Language Selectbox
		 */
		final VisSelectBox<Language> languagesSelectBox = new VisSelectBox<Language>();
		languagesSelectBox.setItems(LanguageModLoader.getAllAvailableLanguages());
		
		languagesSelectBox.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            
	        	Language language = languagesSelectBox.getSelected();
	        	
	        	
	        	
	        	if (language != null) {
	        	
	        		System.out.println("SELECTED LANGUAGE: " + language.getName());
	        		
//	        		((DarkBlade) game).updateLanguage(language.getId());
	        		
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