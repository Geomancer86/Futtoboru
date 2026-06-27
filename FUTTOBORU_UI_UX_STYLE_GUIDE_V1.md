# Futtoboru UI/UX Style Guide

**Version:** 1.0  
**Date:** 2025-12-20  
**Status:** Active - All UI development must follow this guide  
**Priority:** CRITICAL - Consistency is essential

---

## Executive Summary

This document defines the UI/UX standards for the Futtoboru game. **ALL future UI development must follow these guidelines** to maintain consistency across the game. The game currently uses a plain, barebones style that will be enhanced with a custom skin later. Until then, all screens must match the existing visual style.

---

## 1. Core Visual Style

### 1.1 Background Colors

**CRITICAL RULE:** All screens MUST use pure black background.

```java
// CORRECT - All game screens use this
Gdx.gl.glClearColor(0, 0, 0, 1);
Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

// WRONG - Never use gray or other colors
Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1); // ❌ DO NOT USE
```

**Rationale:**
- Consistent with all existing game screens
- Provides proper contrast for UI elements
- Matches the game's current barebones aesthetic
- Will work seamlessly when custom skin is applied

**Examples:**
- `MenuScreen.java` - Uses `(0, 0, 0, 1)`
- `NewGameOverviewScreen.java` - Uses `(0, 0, 0, 1)`
- `SettingsScreen.java` - Uses `(0, 0, 0, 1)`
- `MainGameScreen.java` - Uses `(0, 0, 0, 1)`

---

### 1.2 Text Colors

**Default Text:** White (`fontColor: white`)
- All labels use white text by default
- Provides contrast against black background

**Secondary Text:** Gray for less prominent information
```java
label.setColor(0.7f, 0.7f, 0.7f, 1f); // Gray for secondary info
```

**Color Palette (from tixel.json):**
- `white`: `{r: 1, g: 1, b: 1, a: 1}` - Primary text
- `grey` / `t-light`: `{r: 0.4, g: 0.4, b: 0.4, a: 1}` - Secondary text
- `t-highlight`: `{r: 0.85, g: 0.5, b: 0.05, a: 1}` - Links, highlights
- `red` / `t-warn`: `{r: 1, g: 0, b: 0, a: 1}` - Warnings, errors

---

### 1.3 Component Visibility

**CRITICAL:** All UI components must be clearly visible against black background.

**VisSelectBox (Dropdown/Combo Box):**
- Uses default skin styling from `tixel.json`
- Background: `default-select` drawable
- Font: `default-font` (white)
- List background: `default-rect`
- **Must be visible** - if invisible, check skin configuration

**VisTextField:**
- Background: `textfield` drawable
- Font: `default-font` (white)
- Border visible on focus

**VisSlider:**
- Background: `default-slider` drawable
- Knob: `default-slider-knob` drawable
- Must be clearly visible

**VisButton:**
- Up state: `button` drawable
- Down state: `button-down` drawable
- Over state: `button-over` drawable
- Font: `default-font` (white)

---

## 2. Layout Patterns

### 2.1 Screen Structure

**Standard Screen Layout:**
```java
public class MyScreen implements Screen {
    Game game;
    Stage stage;
    VisTable mainTable;
    
    public MyScreen(Game parent) {
        this.game = parent;
        stage = new Stage(new ScreenViewport());
        
        // Main table fills screen
        mainTable = new VisTable(true);
        mainTable.setFillParent(true);
        mainTable.pad(20);
        
        // Build content...
        
        stage.addActor(mainTable);
    }
    
    @Override
    public void render(float delta) {
        // ALWAYS use black background
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }
}
```

---

### 2.2 Form Layout Pattern

**Two-Column Form (Standard Pattern):**
```java
// Label | Input pattern
table.row();
table.add(label).left().width(150);
table.add(input).left().fillX();
```

**Sectioned Layout:**
```java
// Section separator
table.addSeparator().colspan(2).pad(10);
table.row();

// Section header
VisLabel sectionLabel = new VisLabel("Section Title");
sectionLabel.setFontScale(1.2f);
table.add(sectionLabel).colspan(2).left().padBottom(10);
table.row();
```

---

### 2.3 Spacing and Padding

**Standard Padding:**
- Screen padding: `mainTable.pad(20)`
- Section padding: `.pad(10)` or `.padBottom(10)`
- Component padding: `.pad(5)` for tight spacing

**Row Spacing:**
- Use `super(true)` in VisTable constructor for automatic spacing
- Manual spacing: `.padTop(10)`, `.padBottom(10)`

**Column Spacing:**
- Label width: `.width(150)` for consistent alignment
- Input fill: `.fillX()` to expand

---

## 3. Component Usage Standards

### 3.1 VisSelectBox (Dropdown)

**Standard Pattern:**
```java
VisSelectBox<Type> selectBox = new VisSelectBox<>();
selectBox.setItems(itemsArray);
selectBox.setSelectedIndex(0); // Set default
selectBox.addListener(new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        Type selected = selectBox.getSelected();
        // Handle selection
    }
});
```

**Layout:**
```java
table.add(label).left().width(150);
table.add(selectBox).fillX();
```

**Visibility Check:**
- If dropdown appears invisible, verify skin is loaded correctly
- Check that `default-select` drawable exists in skin
- Ensure font color is white for visibility

---

### 3.2 VisSlider (Range Input)

**Standard Pattern:**
```java
VisSlider slider = new VisSlider(min, max, step, false);
slider.setValue(defaultValue);
VisLabel valueLabel = new VisLabel(String.valueOf(defaultValue));

slider.addListener(new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        float value = slider.getValue();
        valueLabel.setText(String.valueOf((int)value));
    }
});

// Layout with label showing value
VisTable sliderTable = new VisTable(true);
sliderTable.add(slider).fillX();
sliderTable.add(valueLabel).padLeft(10);
table.add(sliderTable).fillX();
```

---

### 3.3 VisTextField (Text Input)

**Standard Pattern:**
```java
VisTextField textField = new VisTextField();
textField.setText("Default value");
textField.setDisabled(false);

// For validated inputs
VisValidatableTextField validField = new VisValidatableTextField();
```

**Layout:**
```java
table.add(label).left().width(150);
table.add(textField).fillX();
```

---

### 3.4 VisCheckBox (Boolean Input)

**Standard Pattern:**
```java
VisCheckBox checkBox = new VisCheckBox("Label");
checkBox.setChecked(true);
checkBox.addListener(new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        boolean checked = checkBox.isChecked();
    }
});
```

---

### 3.5 VisTextButton (Button)

**Standard Pattern:**
```java
VisTextButton button = new VisTextButton("Label");
button.addCaptureListener(new InputListener() {
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        return true;
    }
    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (button.isPressed()) {
            // Handle click
        }
    }
});
button.setDisabled(false); // Enable/disable as needed
```

---

### 3.6 VisScrollPane (Scrollable Content)

**Standard Pattern:**
```java
VisTable contentTable = new VisTable(true);
contentTable.pad(20);

// Build content...

VisScrollPane scrollPane = new VisScrollPane(contentTable);
scrollPane.setFadeScrollBars(false); // Keep scrollbars visible

mainTable.add(scrollPane).grow().fill();
```

---

## 4. Typography

### 4.1 Font Sizes

**Standard Sizes:**
- Default: `1.0f` (no scaling)
- Section headers: `1.2f` - `1.5f`
- Title: `1.5f` - `2.0f`
- Secondary text: `0.9f` - `1.0f`
- Small text: `0.7f` - `0.8f`

**Usage:**
```java
VisLabel title = new VisLabel("Title");
title.setFontScale(1.5f);

VisLabel section = new VisLabel("Section");
section.setFontScale(1.2f);

VisLabel secondary = new VisLabel("Secondary");
secondary.setFontScale(0.9f);
secondary.setColor(0.7f, 0.7f, 0.7f, 1f); // Gray
```

---

### 4.2 Text Alignment

**Standard Alignments:**
- Labels: `.left()` - Left aligned
- Headers: `.center()` or `.left()` depending on context
- Values: `.left()` or `.right()` based on layout

---

## 5. Color Usage Guidelines

### 5.1 Primary Colors

**White (Primary Text):**
- Default for all labels
- Default for all buttons
- Default for all inputs

**Gray (Secondary Text):**
- Version info
- Build info
- Less prominent information
- `setColor(0.7f, 0.7f, 0.7f, 1f)`

**Highlight/Orange (Links, Highlights):**
- Link labels
- Important highlights
- `t-highlight`: `{r: 0.85, g: 0.5, b: 0.05, a: 1}`

**Red (Warnings, Errors):**
- Error messages
- Warnings
- `t-warn`: `{r: 1, g: 0, b: 0, a: 1}`

---

### 5.2 Background Colors

**Screen Background:**
- **ALWAYS** `(0, 0, 0, 1)` - Pure black
- Never use gray, white, or other colors

**Component Backgrounds:**
- Handled by skin drawables
- Buttons, inputs, etc. use skin-defined backgrounds
- Do not manually set component background colors

---

## 6. Component Visibility Checklist

Before committing any UI changes, verify:

- [ ] Screen background is pure black `(0, 0, 0, 1)`
- [ ] All text is visible (white or gray, not black on black)
- [ ] All dropdowns (VisSelectBox) are visible
- [ ] All text fields are visible
- [ ] All buttons are visible
- [ ] All sliders are visible
- [ ] All checkboxes are visible
- [ ] Scroll panes are functional
- [ ] Spacing is consistent with other screens
- [ ] Padding matches existing patterns

---

## 7. Common Mistakes to Avoid

### ❌ DO NOT:

1. **Use gray backgrounds:**
   ```java
   Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1); // ❌ WRONG
   ```

2. **Use black text on black background:**
   ```java
   label.setColor(0, 0, 0, 1); // ❌ WRONG - invisible on black
   ```

3. **Forget to set component visibility:**
   ```java
   // Always ensure components are visible
   component.setVisible(true);
   ```

4. **Use inconsistent spacing:**
   ```java
   // Use standard padding values
   table.pad(20); // Screen
   table.pad(10); // Sections
   table.pad(5);  // Components
   ```

5. **Ignore existing patterns:**
   - Always check similar screens for patterns
   - Follow established layout conventions
   - Match spacing and padding

---

## 8. Future Skin Development

**Current State:**
- Plain, barebones UI
- Black background, white text
- Basic component styling

**Future Enhancement:**
- Custom skin will be developed
- All current UI will be enhanced automatically
- No need to change code, just update skin assets

**Development Strategy:**
- Keep UI code simple and consistent
- Use standard VisUI components
- Avoid custom styling in code
- Let skin handle visual appearance

---

## 9. Screen-Specific Guidelines

### 9.1 Menu Screens

- Black background
- Centered or left-aligned content
- Consistent button spacing
- Clear visual hierarchy

### 9.2 Form Screens

- Two-column layout (Label | Input)
- Section separators
- Clear section headers
- Consistent input widths

### 9.3 List/Table Screens

- Scrollable content
- Clear column headers
- Consistent row spacing
- Visible scrollbars

### 9.4 Detail Screens

- Scrollable content
- Sectioned information
- Clear visual separation
- Back/navigation buttons

---

## 10. Testing Checklist

Before considering UI complete:

1. **Visual Consistency:**
   - [ ] Matches existing screen styles
   - [ ] Uses black background
   - [ ] Text is visible
   - [ ] Components are visible

2. **Functionality:**
   - [ ] All inputs work
   - [ ] All buttons work
   - [ ] Navigation works
   - [ ] Scrolling works (if applicable)

3. **Layout:**
   - [ ] Spacing is consistent
   - [ ] Alignment is correct
   - [ ] No overlapping elements
   - [ ] Responsive to window resize

4. **Code Quality:**
   - [ ] Follows existing patterns
   - [ ] Uses standard components
   - [ ] Proper event handling
   - [ ] Clean, readable code

---

## 11. Reference Examples

### Good Examples (Follow These):

- `MenuScreen.java` - Clean, simple menu
- `NewGameOverviewScreen.java` - Form layout
- `SettingsScreen.java` - Settings form
- `MainGameScreen.java` - Main game interface

### Pattern Library:

**Two-Column Form:**
```java
table.row();
table.add(label).left().width(150);
table.add(input).fillX();
```

**Section Header:**
```java
table.addSeparator().colspan(2).pad(10);
table.row();
VisLabel header = new VisLabel("Section");
header.setFontScale(1.2f);
table.add(header).colspan(2).left().padBottom(10);
table.row();
```

**Action Buttons:**
```java
VisTable buttonTable = new VisTable(true);
buttonTable.add(button1).padRight(10);
buttonTable.add().expandX(); // Spacer
buttonTable.add(button2).padRight(10);
buttonTable.add(button3);
mainTable.add(buttonTable).growX().fillX().padTop(10);
```

---

## 12. Quick Reference

### Background Color
```java
Gdx.gl.glClearColor(0, 0, 0, 1); // ALWAYS black
```

### Text Colors
```java
// Primary
label.setColor(1, 1, 1, 1); // White

// Secondary
label.setColor(0.7f, 0.7f, 0.7f, 1f); // Gray
```

### Standard Padding
```java
mainTable.pad(20);        // Screen
section.pad(10);          // Section
component.pad(5);          // Component
```

### Standard Widths
```java
label.width(150);         // Form labels
input.fillX();            // Form inputs
```

---

## Conclusion

**Remember:**
1. **Black background always** - `(0, 0, 0, 1)`
2. **White text default** - Visible on black
3. **Follow existing patterns** - Check similar screens
4. **Keep it simple** - Plain, barebones style
5. **Test visibility** - All components must be visible

**Before committing any UI code:**
- Read this guide
- Check existing screens for patterns
- Test visibility of all components
- Verify black background
- Ensure consistency

This guide will be updated as the UI evolves, but the core principles (black background, white text, consistency) remain constant.
