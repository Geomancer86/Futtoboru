package com.rndmodgames.darkblade.screens.tables;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * SoundSettingsTable v1
 * 
 * @author Geomancer86
 */
public class SoundSettingsTable extends VisTable {

	public static final float DEFAULT_MASTER_SOUND = 100f;
		
	public SoundSettingsTable(Game game) {

		VisLabel soundSettingsLabel = new VisLabel(LanguageModLoader.getValue("sound_settings"));
		VisLabel masterSoundLabel = new VisLabel(LanguageModLoader.getValue("master_sound"));
		VisLabel masterSoundValueLabel = new VisLabel(DEFAULT_MASTER_SOUND + "%");
		masterSoundValueLabel.setAlignment(Align.right);
		
		VisLabel musicSoundLabel = new VisLabel(LanguageModLoader.getValue("music_sound"));
		VisLabel effectsSoundLabel = new VisLabel(LanguageModLoader.getValue("effects_sound"));
		VisLabel uiSoundLabel = new VisLabel(LanguageModLoader.getValue("ui_sound"));

		/**
		 * Master Sound Slider
		 * 
		 * float min, float max, float stepSize, boolean vertical
		 */
		VisSlider masterSoundSlider = new VisSlider(0, 100, 1, false);
		
		masterSoundSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				masterSoundValueLabel.setText(masterSoundSlider.getValue() + "%");
				
				/**
				 * SAVE MASTER SOUND SETTING
				 */
//				((DarkBlade) game).updateMasterSoundLevel(masterSoundSlider.getValue());
			}
		});
		
		// SET DEFAULT MASTER SOUND VALUE
//		masterSoundSlider.setValue(DarkBlade.MASTER_VOLUME);
		masterSoundValueLabel.setText(masterSoundSlider.getValue() + "%");
		
		row();
		
		add(soundSettingsLabel).pad(5).colspan(2).expandX().align(Align.center);
		
		row();
		add(masterSoundLabel).pad(5).fill().align(Align.center);
		add(masterSoundValueLabel).pad(5).fill().align(Align.center);
		
		row();
		add(masterSoundSlider).pad(5).colspan(2).fill();
		
		row();
		add(musicSoundLabel);
		
		row();
		add(effectsSoundLabel);
		
		row();
		add(uiSoundLabel);
	}
}