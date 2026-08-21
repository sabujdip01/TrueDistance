# True Distance — Final Design Specification

> This document is the single source of truth for all visual design decisions in the
> True Distance Android app. Every color, font size, radius, shadow, spacing value,
> animation, and component variant is defined here so that implementation matches the
> intended design with pixel-level fidelity.

---

## 1. Design Philosophy

### 1.1 Core Principles

| Principle | Description |
|---|---|
| **Soft & Calm** | Muted pastel backgrounds, never stark white or harsh black. The app should feel calm and premium — like a well-made instrument, not a loud billboard. |
| **Card-First** | All primary content lives inside elevated, rounded-corner cards floating above the background. Cards create visual hierarchy and group related information. |
| **Bold Data** | Key measurements (distance, speed, time) use oversized bold numerals — the most important number on each screen should be readable from arm's length. |
| **Minimal Chrome** | Thin-stroke icons, no heavy borders, no toolbars with opaque backgrounds. Navigation is subtle; content is king. |
| **Color as Meaning** | Color is used sparingly and intentionally — accent colors signal live/active states, pastel tints differentiate list item categories, and the primary violet is reserved for interactive elements. |
| **Consistent Motion** | Every transition and state change has a smooth, purposeful animation. Nothing teleports; nothing jitters. |

### 1.2 Design Inspiration

The visual language draws from modern card-based mobile UIs:
- **Soft blush/cream backgrounds** with floating white cards (similar to premium fintech apps)
- **Large bold numerals** for key data (inspired by balance/amount displays in finance apps)
- **Gradient-tinted list items** with pastel hues (mint, peach, lavender) for visual variety
- **Map-first tracking screens** with floating info overlays (inspired by delivery/ride tracking apps)
- **Rounded pill-shaped bottom navigation** bar, detached from the screen edge
- **Minimal line-style iconography** with consistent stroke weight

---

## 2. Color System

### 2.1 Light Mode Palette

#### Primary Colors

| Token | Hex | Usage |
|---|---|---|
| `primary_violet` | `#7C4DFF` | Primary brand color. Buttons, active nav icons, links, accent highlights, splash screen app name |
| `primary_violet_dark` | `#651FFF` | Pressed/focused state of primary buttons |
| `primary_violet_light` | `#B388FF` | Subtle tints, progress indicators, selection highlights |
| `primary_lavender` | `#E1BEE7` | Secondary accent. Ripple effects, subtle backgrounds, inactive toggle tracks |

#### Background Colors

| Token | Hex | Usage |
|---|---|---|
| `background_soft` | `#F9F4F8` | Primary screen background — pale pinkish-lavender white |
| `bg_gradient_start` | `#FFFFFF` | Top of optional vertical gradient |
| `bg_gradient_end` | `#F3E5F5` | Bottom of optional vertical gradient — very soft lavender |
| `background_page` | `#F5F0F4` | Settings and secondary pages — slightly more contrast than `background_soft` |

#### Surface / Card Colors

| Token | Hex | Usage |
|---|---|---|
| `card_white` | `#FFFFFF` | Primary card surfaces, bottom navigation bar, dialogs |
| `card_white_translucent` | `#E6FFFFFF` | 90% opacity white — glass-effect overlays on maps |
| `glass_white` | `#CCFFFFFF` | 80% opacity white — lighter glass effect |
| `card_elevated` | `#FFFFFF` | Same as card_white but with higher elevation shadow |

#### Semantic / Status Colors

| Token | Hex | Usage |
|---|---|---|
| `success_mint` | `#B2DFDB` | Positive states, saved confirmations, "tracking started" |
| `success_mint_surface` | `#E0F2F1` | Light mint background for success cards/banners |
| `warning_peach` | `#FFCCBC` | Caution states, GPS signal warnings |
| `warning_peach_surface` | `#FFF3E0` | Light peach background for warning cards |
| `error_coral` | `#EF5350` | Destructive actions (delete), error states |
| `error_coral_surface` | `#FFEBEE` | Light red background for error banners |
| `info_blue` | `#E1F5FE` | Informational banners, neutral notices |

#### Text Colors

| Token | Hex | Usage |
|---|---|---|
| `text_charcoal` | `#37474F` | Primary text — headings, body, labels |
| `text_gray_purple` | `#90A4AE` | Secondary text — hints, captions, timestamps, disabled labels |
| `text_white` | `#FFFFFF` | Text on dark/colored backgrounds (buttons, banners) |
| `text_on_primary` | `#FFFFFF` | Text on primary_violet backgrounds |

#### Accent / Decorative Colors

| Token | Hex | Usage |
|---|---|---|
| `accent_warm` | `#7C4DFF` | Live/active tracking polyline, recording indicators |
| `accent_teal` | `#B2DFDB` | Completed/success state accents |
| `pill_dark` | `#37474F` | Dark pill backgrounds (active nav indicator in some variants) |

#### List Item Gradient Pairs (Saved Locations / History)

| Card Style | Start Color | End Color | Usage |
|---|---|---|---|
| Mint | `#B2DFDB` | `#E0F2F1` | Saved location items (index % 3 == 0) |
| Peach | `#FFCCBC` | `#FFF3E0` | Saved location items (index % 3 == 1) |
| Lavender | `#E1BEE7` | `#F3E5F5` | Saved location items (index % 3 == 2) |
| History Blue | `#BBDEFB` | `#E3F2FD` | History entry items (index % 3 == 0) |
| History Peach | `#FFCCBC` | `#FFF3E0` | History entry items (index % 3 == 1) |
| History Lavender | `#E1BEE7` | `#F3E5F5` | History entry items (index % 3 == 2) |

> **Gradient direction**: all list item gradients use a 45° angle (bottom-left to top-right).

---

### 2.2 Dark Mode Palette

| Token | Light Value | Dark Value | Notes |
|---|---|---|---|
| `background_soft` | `#F9F4F8` | `#1C1B1A` | Deep warm charcoal, not pure black |
| `background_page` | `#F5F0F4` | `#121110` | Slightly deeper than background_soft |
| `card_white` | `#FFFFFF` | `#2A2826` | Elevated dark gray with warm undertone |
| `card_elevated` | `#FFFFFF` | `#353230` | Higher elevation cards — slightly lighter |
| `text_charcoal` | `#37474F` | `#E8E0DC` | Light warm cream for dark mode readability |
| `text_gray_purple` | `#90A4AE` | `#8A8480` | Muted warm gray |
| `primary_violet` | `#7C4DFF` | `#B388FF` | Slightly lighter violet for dark backgrounds |
| `primary_lavender` | `#E1BEE7` | `#4A148C` | Deep purple in dark mode |
| `success_mint` | `#B2DFDB` | `#1B5E20` | Deep green tint |
| `warning_peach` | `#FFCCBC` | `#BF360C` | Deep orange tint |
| `error_coral` | `#EF5350` | `#FF8A80` | Slightly lighter coral for contrast |
| `pill_dark` | `#37474F` | `#E8E0DC` | Inverted for dark mode |
| `glass_white` | `#CCFFFFFF` | `#CC2A2826` | Dark glass effect |

#### Dark Mode List Item Gradients

In dark mode, list item gradients use deeply desaturated versions at 15% opacity
over the card surface, preserving the color coding without being harsh:

| Card Style | Dark Start | Dark End |
|---|---|---|
| Mint | `#1A2E3530` | `#1A3E4A40` |
| Peach | `#1A3E2820` | `#1A4E3830` |
| Lavender | `#1A2A1840` | `#1A3A2850` |

---

### 2.3 Map Styling

Google Maps is styled with a custom JSON style to desaturate the default map colors
so the map integrates with the app's soft palette rather than looking like stock Google Maps.

**Light Mode Map Adjustments:**
- Reduce overall saturation by 30%
- Increase lightness by 15%
- Road labels: `#90A4AE` (matches `text_gray_purple`)
- Water: `#E1F5FE` (matches `info_blue`)
- Parks/green: `#E0F2F1` (matches `success_mint_surface`)
- Points of interest: hidden (reduce visual noise)
- Transit lines: hidden

**Dark Mode Map Adjustments:**
- Use Google Maps' built-in dark mode (`MapStyleOptions.loadRawResourceStyle`)
- Further reduce saturation by 20%
- Road labels: `#8A8480`
- Water: `#1A237E` (deep blue)

**Tracking Polyline:**
- Color: `primary_violet` (`#7C4DFF`)
- Width: `6dp`
- Pattern: Solid (not dashed)
- Cap: Round
- Opacity: 90%

**Speedometer Trip Polyline (V2):**
- Color: `accent_teal` (`#B2DFDB`)
- Width: `5dp`
- Pattern: Solid
- Cap: Round

---

## 3. Typography System

### 3.1 Font Family

| Weight | Family | Usage |
|---|---|---|
| Regular (400) | **Inter** or **Roboto** (system default) | Body text, labels, captions |
| Medium (500) | Inter Medium / Roboto Medium | Section headers, nav labels, settings labels |
| Bold (700) | Inter Bold / Roboto Bold | Primary headings, key data values, button text |

> If using the system default Roboto, no additional font import is needed.
> If using Inter, import via Google Fonts dependency or bundle the TTF.

### 3.2 Type Scale

| Style Name | Size | Weight | Line Height | Letter Spacing | Usage |
|---|---|---|---|---|---|
| `display_large` | 48sp | Bold | 56sp | -0.5sp | Splash screen distance (future hero data) |
| `display_medium` | 36sp | Bold | 44sp | 0sp | Tracking screen live distance value |
| `display_small` | 32sp | Bold | 40sp | 0sp | Splash screen app name |
| `headline_large` | 28sp | Bold | 36sp | 0sp | Screen titles (Saved Locations, Distance History) |
| `headline_medium` | 24sp | Bold | 32sp | 0sp | Card section headings |
| `headline_small` | 20sp | Bold | 28sp | 0sp | Settings section headers |
| `title_large` | 18sp | Medium | 24sp | 0sp | Sub-section titles, dialog titles |
| `title_medium` | 16sp | Medium | 22sp | 0.15sp | List item primary text (destination name) |
| `title_small` | 14sp | Medium | 20sp | 0.1sp | List item secondary emphasis |
| `body_large` | 16sp | Regular | 24sp | 0.5sp | Body text, descriptions |
| `body_medium` | 14sp | Regular | 20sp | 0.25sp | From/To labels, settings descriptions |
| `body_small` | 13sp | Regular | 18sp | 0.4sp | Settings labels, hint text |
| `label_large` | 14sp | Medium | 20sp | 0.1sp | Button text |
| `label_medium` | 12sp | Medium | 16sp | 0.5sp | Chip labels, badge text, stale indicator |
| `label_small` | 11sp | Medium | 16sp | 0.5sp | Overline labels, timestamps in history |
| `caption` | 12sp | Regular | 16sp | 0.4sp | Secondary info in list items (address, coordinates) |

### 3.3 Text Color Assignments

| Text Role | Light Color | Dark Color |
|---|---|---|
| Primary heading | `text_charcoal` | `#E8E0DC` |
| Body text | `text_charcoal` | `#E8E0DC` |
| Secondary / hint | `text_gray_purple` | `#8A8480` |
| Disabled | `#B0BEC5` | `#5A5550` |
| On primary button | `text_white` | `text_white` |
| On colored banner | `text_white` | `text_white` |
| Link / interactive | `primary_violet` | `#B388FF` |
| Error text | `error_coral` | `#FF8A80` |

### 3.4 Numeral Treatment for Key Data

The most important number on each screen gets special treatment:

| Screen | Data | Size | Weight | Color |
|---|---|---|---|---|
| Tracking Screen | Live distance | `36sp` | Bold | `text_charcoal` |
| Tracking Notification | Distance | System default | — | — |
| Speedometer (V2) | Current speed | `48sp` | Bold | `text_charcoal` |
| Speedometer Stats (V2) | Avg/Max Speed | `20sp` | Bold | `text_charcoal` |
| History Item (collapsed) | Initial distance | `13sp` | Regular | `text_gray_purple` |
| History Item (expanded) | Snapshot distances | `13sp` | Regular | `text_gray_purple` |

---

## 4. Spacing & Layout Grid

### 4.1 Base Unit

All spacing is based on a **4dp grid**. Common spacing values:

| Token | Value | Usage |
|---|---|---|
| `space_xxs` | 2dp | Hairline gaps, icon-to-text micro spacing |
| `space_xs` | 4dp | Tight internal padding, between stacked labels |
| `space_sm` | 8dp | Small gaps between related elements, banner padding |
| `space_md` | 12dp | Medium gap, padding between label groups |
| `space_lg` | 16dp | Standard content padding (card internal, screen horizontal margins) |
| `space_xl` | 20dp | Generous spacing between major sections |
| `space_xxl` | 24dp | Large gaps (bottom nav margins, section separators, card top margin) |
| `space_xxxl` | 32dp | Very large gaps (splash screen bottom margin, between card groups) |
| `space_huge` | 48dp | Hero spacing (above splash logo centering) |

### 4.2 Screen-Level Layout

| Property | Value | Notes |
|---|---|---|
| Horizontal content padding | `16dp` | Applied to card margins, list padding |
| Max content width | `600dp` | On tablets/foldables, content is centered with this max width |
| Content alignment | Center horizontal | Content column is always centered on wider screens |
| Bottom nav margin (horizontal) | `16dp` | Space between nav bar and screen edges |
| Bottom nav margin (bottom) | `16dp` | Space between nav bar and system nav bar area |
| Bottom nav max width | `600dp` | Capped to prevent stretching on tablets |

### 4.3 Card Internal Padding

| Card Type | Padding | Notes |
|---|---|---|
| Location selection card | `16dp` all sides | Main screen destination card |
| Distance readout overlay | `16dp` all sides | Floating card on tracking map |
| List item card (saved location) | `20dp` on 80% content area | 80/20 split: 80% content, 20% centered delete button |
| List item card (history entry) | `20dp` on 80% content area | 80/20 split: 80% content, 20% centered delete button |
| Settings content area | `16dp` all sides | Settings form padding |
| Dialog content | `24dp` all sides | Alert dialogs |

### 4.4 List Item Spacing

| Property | Value |
|---|---|
| Vertical gap between list items | `6dp` (via marginVertical on each item) |
| Horizontal margin of list items | `16dp` (via marginHorizontal on each item) |
| RecyclerView top padding | `8dp` with `clipToPadding="false"` |
| Section header top padding | `16dp` |
| Section header bottom padding | `4dp` |

---

## 5. Corner Radii & Elevation

### 5.1 Corner Radius Scale

| Token | Value | Usage |
|---|---|---|
| `radius_sm` | 8dp | Small chips, tag badges |
| `radius_md` | 12dp | Buttons, input fields, spinner backgrounds |
| `radius_lg` | 16dp | Map Card frame (tracking & picker), distance readout overlay card |
| `radius_xl` | 20dp | Main location card, settings cards |
| `radius_xxl` | 24dp | Bottom navigation bar, list item cards, dialogs, nav pill |
| `radius_full` | 50% / `oval` | Icon chip buttons (bookmark, history, map pick) — circular |

### 5.2 Elevation / Shadow Scale

| Level | Elevation | Shadow Alpha | Usage |
|---|---|---|---|
| Level 0 | 0dp | — | Flat elements (banners, dividers) |
| Level 1 | 2dp | 8% black | Map Card frame, list item cards |
| Level 2 | 4dp | 10% black | Location selection card, distance readout overlay |
| Level 3 | 6dp | 12% black | Floating action buttons |
| Level 4 | 8dp | 15% black | Bottom navigation bar |
| Level 5 | 12dp | 18% black | Dialogs, bottom sheets |

> **Dark mode shadows**: elevation is conveyed through surface color lightness
> changes rather than drop shadows (per Material Design dark theme guidelines).
> Higher elevation = slightly lighter card color.

---

## 6. Icon System

### 6.1 Icon Style

| Property | Value |
|---|---|
| Style | Outlined / line-style (thin stroke) |
| Stroke weight | 1.5dp–2dp |
| Grid size | 24x24dp |
| Touch target | 48x48dp minimum (via padding or icon chip background) |
| Color (default) | `text_charcoal` (light mode), `#E8E0DC` (dark mode) |
| Color (active/selected) | `primary_violet` |
| Color (disabled) | `#B0BEC5` |
| Color (on dark surface) | `text_white` |

### 6.2 Icon Inventory

| Icon Name | Resource | Usage | Screen |
|---|---|---|---|
| Bookmark | `ic_bookmark` | Saved Locations header button | Main Screen |
| History/Clock | `ic_history` | Distance History header button | Main Screen |
| Map Pin | `ic_map_pin` | Map picker button, destination marker | Main Screen, Map Picker |
| Chevron Down | `ic_chevron_down` | Saved locations dropdown trigger | Main Screen |
| Add/Plus | `ic_add` | FAB — add new saved location | Saved Locations |
| Delete/Trash | `ic_delete` | Delete button on list items | Saved Locations, History |
| Recenter | `ic_recenter` | Recenter map FAB | Tracking Screen |
| Distance/Ruler | `ic_tab_distance` | Bottom nav — True Distance tab | Bottom Nav |
| Speedometer | `ic_speedometer` | Bottom nav — Speedometer tab | Bottom Nav |
| Settings/Gear | `ic_settings` | Bottom nav — Settings tab | Bottom Nav |

### 6.3 Icon Chip Component

The circular icon chip is used for header action buttons (Saved Locations, History, Map Pick):

```
Shape:       Oval (circular)
Size:        48x48dp (bookmark/history) or 40x40dp (inline action icons)
Background:  card_white (#FFFFFF)
Border:      1dp, #1A000000 (10% black) — very subtle
Icon size:   24x24dp (centered)
Icon color:  text_charcoal
Shadow:      None (border provides visual separation)
```

**Pressed state**: Background tints to `primary_lavender` at 30% opacity.

---

## 7. Component Library

### 7.1 Buttons

#### Primary Button (Filled)

```
Height:           48dp (minimum)
Corner radius:    12dp
Background:       primary_violet (#7C4DFF)
Text color:       text_white (#FFFFFF)
Text size:        14sp, Medium weight (label_large)
Text transform:   ALL CAPS
Padding:          horizontal 24dp
Ripple color:     #33FFFFFF (20% white)
Inset:            0dp top/bottom (no Material default insets)

Disabled state:
  Background:     #E0E0E0
  Text color:     #9E9E9E

Pressed state:
  Background:     primary_violet_dark (#651FFF)

Full-width variant:
  Width:          match_parent
  Used in:        Start Tracking, Stop Tracking, Confirm Location
```

#### Text Button

```
Background:       Transparent
Text color:       primary_violet (#7C4DFF)
Text size:        14sp, Medium weight
Text transform:   Sentence case
Ripple:           primary_lavender at 15% opacity
Used in:          Dialog positive/negative buttons, "See All" links
```

#### Destructive Button (Stop Tracking)

```
Height:           48dp
Corner radius:    12dp
Background:       primary_violet (#7C4DFF) or error_coral (#EF5350)
Text color:       text_white
Text size:        14sp, Medium weight
Full-width:       Yes
```

#### Floating Action Button (FAB)

```
Size:             56x56dp (standard)
Shape:            Circular
Background:       primary_violet (#7C4DFF)
Icon color:       text_white
Icon size:        24x24dp
Elevation:        6dp
Shadow:           Material default

Mini FAB (Recenter):
  Size:           40x40dp
  Background:     card_white with primary_violet icon
  Elevation:      4dp
```

### 7.2 Cards

#### Primary Content Card (Location Selection)

```
Background:       card_white (#FFFFFF)
Corner radius:    20dp
Elevation:        4dp
Margin:           16dp horizontal, 24dp top
Max width:        600dp
Internal padding: 16dp all sides
Content:          Search box, From/To labels, Start Tracking button
```

#### Distance Readout Overlay Card

```
Background:       card_white_translucent (#E6FFFFFF) for glass effect
Corner radius:    16dp
Elevation:        4dp
Max width:        600dp
Position:         Top-center of map, 24dp from map top
Internal padding: 16dp all sides, center-aligned
Content:          Distance text (36sp bold), optional stale indicator below
```

#### List Item Card (Saved Location)

```
Background:       Gradient (45 deg) — cycles through mint/lavender/peach
Corner radius:    24dp
Elevation:        0dp (gradient provides visual lift)
Margin:           16dp horizontal, 6dp vertical
Layout Split:     80% Left (Content, padding=20dp) | 20% Right (Delete Button centered)

Row 1:            Location Name (17sp bold, deep card color in light mode: #00695C / #6A1B9A / #BF360C)
Row 2:            Address (13sp, same hue at 85% opacity in light mode)
Delete button:    36x36dp, centered horizontally & vertically in 20% right container
Dark mode:        Uses deep desaturated gradient + text_charcoal / text_gray_purple
```

#### List Item Card (History Entry)

```
Background:       Gradient (45 deg) — cycles through blue/peach/lavender
Corner radius:    24dp
Elevation:        0dp
Margin:           16dp horizontal, 6dp vertical
Layout Split:     80% Left (Content, padding=20dp) | 20% Right (Delete Button centered)

Row 1:            Destination Name (17sp bold) + Tracked Distance (13sp) [Deep card color in light mode: #1565C0 / #BF360C / #6A1B9A]
Row 2:            Start Timestamp (13sp) | Stop Timestamp (13sp) | Elapsed (13sp) [Same hue at 85% opacity in light mode]
Delete button:    36x36dp, centered horizontally & vertically in 20% right container

Expanded:         Single-card exclusive expand (expanding one collapses others)
                  3-Column Table:
                  - Col 1 (12sp Bold): Elapsed mark ("+0:00 (Start)", "+2:30", "+5:00 (End)")
                  - Col 2 (12sp Regular, Center): Clock time ("9:45 PM")
                  - Col 3 (12sp Regular, End): Distance ("12.4 km")
Dark mode:        Uses deep desaturated gradient + text_charcoal / text_gray_purple
```

#### History Date Section Header

```
Background:       Transparent
Padding:          16dp horizontal, 16dp top, 4dp bottom
Text:             14sp, Bold, text_secondary color
Content:          "Today", "Yesterday", "Older"
```

### 7.3 Bottom Navigation Bar

```
Shape:            Rounded rectangle
Corner radius:    24dp
Background:       card_white (#FFFFFF)
Elevation:        8dp
Margin:           16dp horizontal, 16dp bottom
Max width:        600dp
Height:           wrap_content (Material default ~56dp + label)
Position:         Constrained to bottom of parent, centered horizontally

Tab count:        3 (True Distance, Speedometer, Settings)
Icon size:        24x24dp
Label size:       12sp
Label visibility: Always shown (labeled)

Active tab:
  Icon color:     primary_violet (#7C4DFF)
  Label color:    primary_violet (#7C4DFF)

Inactive tab:
  Icon color:     text_gray_purple (#90A4AE)
  Label color:    text_gray_purple (#90A4AE)

Ripple:           primary_lavender at 15% opacity
Active indicator: Material3 default pill (optional) or color-only
Badge (V2):       Small 8dp circle, primary_violet, positioned top-right of icon
```

### 7.4 Search / Input Fields

#### Destination Search Box (AutoCompleteTextView)

```
Height:           wrap_content
Width:            0dp (weight 1 in horizontal LinearLayout)
Background:       Default underline (MaterialComponents style)
Text color:       text_charcoal (#37474F)
Hint color:       text_gray_purple (#90A4AE)
Text size:        16sp
Hint text:        "Search Destination"
Completion:       threshold = 1 character
Input type:       text
IME action:       Search
Autofill:         Disabled (importantForAutofill="no")

Dropdown style:
  Background:     card_white
  Corner radius:  12dp
  Elevation:      4dp
  Item height:    48dp
  Item text:      14sp, text_charcoal
  Divider:        None (items separated by padding)
```

### 7.5 Spinners (Settings)

```
Width:            match_parent
Height:           wrap_content
Background:       Default Material spinner
Text size:        14sp
Text color:       text_charcoal
Dropdown style:   Default Material popup
Items:            String arrays from resources
```

### 7.6 Switches (Settings)

```
Track (on):       primary_violet at 50% opacity
Thumb (on):       primary_violet
Track (off):      #B0BEC5 at 30% opacity
Thumb (off):      #FAFAFA
Size:             Material default
```

### 7.7 Dividers

```
Height:           1dp
Color:            text_gray_purple (#90A4AE) at 30% opacity
Margin vertical:  20dp
Used in:          Settings screen (between Preferences and About sections)
```

### 7.8 Banners (No Internet / No GPS)

```
Width:            match_parent
Height:           wrap_content
Background:       primary_violet (#7C4DFF) — prominent but on-brand
Padding:          8dp all sides
Text:             14sp, text_white, center-aligned
Visibility:       GONE by default, shown when condition triggers
Position:         Top of screen, above all other content
Clickable:        Yes (GPS banner opens location settings)

Alternative style for softer appearance:
  Background:     warning_peach (#FFCCBC)
  Text color:     text_charcoal
```

### 7.9 Dialogs

#### Confirmation Dialog (Clear All History)

```
Shape:            Rounded rectangle, 24dp corner radius
Background:       card_white (#FFFFFF)
Elevation:        Level 5 (12dp)
Padding:          24dp all sides

Title:            18sp, Bold, text_charcoal
Message:          14sp, Regular, text_gray_purple
Button bar:       Right-aligned, text buttons
Positive button:  "Clear" — primary_violet text
Negative button:  "Cancel" — primary_violet text
```

#### Add Saved Location Dialog

```
Shape:            Same as confirmation dialog
Content:
  - Method selection (Search / Map picker)
  - Location name input (EditText)
  - Save button (primary filled)
Input field:
  Hint:           "Name this Location"
  Style:          Outlined or underline
```

### 7.10 Empty States

```
Position:         Center of screen (horizontally and vertically)
Text:             18sp, text_secondary, center-aligned
Icon:             Optional — relevant illustration (not implemented in V1)
Examples:
  - "No Saved Locations Yet"
  - "No Tracking History yet"
```

---

## 8. Screen-by-Screen Design Specifications

### 8.1 Splash Screen

```
Duration:         3 seconds total
  - System splash (icon):    1.5 seconds (via keepOnScreenCondition)
  - Custom layout:           1.5 seconds (logo + name + version)
Transition:       Fade out -> Fade in (android.R.anim.fade_in/fade_out)

Layout:
  Root:           ConstraintLayout, match_parent
  Background:     background_soft (#F9F4F8)

  Logo:
    Size:         140x140dp
    Source:       @mipmap/ic_launcher
    Position:     Centered (vertical chain with app name, packed)

  App Name:
    Text:         "True Distance"
    Size:         32sp, Bold
    Color:        primary_violet (#7C4DFF)
    Margin top:   20dp (from logo)
    Position:     Below logo in chain, centered horizontally

  Version Text:
    Text:         "v1.0.0 (1)" (dynamic from BuildConfig)
    Size:         13sp, Regular
    Color:        text_gray_purple (#90A4AE)
    Margin bottom: 32dp
    Position:     Bottom of screen, centered horizontally
    (NOT in the chain — pinned to bottom independently)
```

### 8.2 Main Screen (True Distance Tab — Default)

```
Root:             ScrollView -> ConstraintLayout
Background:       background_soft (#F9F4F8)
System bars:      fitsSystemWindows="true" on activity root

+-------------------------------------+
|  [GPS/Internet Banners - if shown]  |  <- Full width, primary_violet bg
+-------------------------------------+
|                                     |
|  (B) Bookmark        History (H)    |  <- Icon chips, 48dp circular
|                                     |
|  +-----------------------------+    |
|  |  [Search Destination] P  V  |    |  <- AutoComplete + icon chips
|  |                             |    |
|  |  From: Current Location     |    |  <- body_medium, bold
|  |  To: (Choose a Destination) |    |  <- body_medium, regular
|  |                             |    |
|  |  +---------------------+   |    |
|  |  |   START TRACKING    |   |    |  <- Primary button, disabled until
|  |  +---------------------+   |    |     destination selected
|  +-----------------------------+    |  <- CardView, 20dp radius, 4dp elev
|                                     |
|         (empty space below)         |
|                                     |
+-------------------------------------+
|  TD  True Distance | SP Speedo | ST |  <- Bottom nav, 24dp radius, floating
+-------------------------------------+

Card max width:   600dp
Button state:
  Enabled:        primary_violet background, white text
  Disabled:       #E0E0E0 background, #9E9E9E text
```

### 8.3 Tracking Screen

```
Root:             ConstraintLayout, match_parent
Background:       background_soft (#F9F4F8 / #1C1B1A)
Layout:           Map wrapped in rounded card frame, controls below map

+-------------------------------------+
|  +-- Map Card (16dp radius) ------+ |
|  |                                | |
|  |          GOOGLE MAP            | |  <- SupportMapFragment inside CardView
|  |      (desaturated style)       | |     12dp margin, 2dp elevation
|  |                                | |
|  |    +-- Glass Overlay card -+   | |
|  |    |        12.4 km        |   | |  <- Distance readout (36sp bold)
|  |    |     (signal lost...)  |   | |     card_white_translucent, 16dp radius
|  |    +-----------------------+   | |
|  |                                | |
|  |                         [R]    | |  <- Recenter FAB, bottom-right of map
|  +--------------------------------+ |
+-------------------------------------+
|                                     |
|  +-----------------------------+    |
|  |       STOP TRACKING         |    |  <- Full-width button, primary_violet
|  +-----------------------------+    |
|                                     |
+-------------------------------------+
  (No bottom nav bar on this screen)

Distance text:    36sp, Bold, text_charcoal
Stale indicator:  12sp, error_coral, shown when GPS lost
Controls bar:     16dp padding, max width 600dp
```

**Map Markers:**

| Marker | Style | Label |
|---|---|---|
| Current location | Red dot / marker pin | "You" |
| Destination | Green marker pin | "Destination" |

**Polyline:**

```
Color:            #00796B (Dark Teal) / primary_violet (#7C4DFF)
Width:            8px (thick solid)
Cap:              Round
Joint:            Round
Pattern:          Solid
Z-index:          Above roads, below markers
```

### 8.4 Saved Locations Screen

```
Root:             ConstraintLayout
Background:       background_soft (#F9F4F8 / #1C1B1A)

Layout (80% Content | 20% Centered Delete):
+-------------------------------------+
|                                     |
|  +-- Mint gradient (80% | 20%) -+   |
|  |  989H+W3X                 | X|   |  <- Name (17sp bold, deep teal)
|  |  989H+W3X, Rahatpur...    |  |   |  <- Address (13sp, 85% opacity tone)
|  +------------------------------+   |
|                                     |
|  +-- Lavender gradient ---------+   |
|  |  Sealdah Station Road     | X|   |  <- Name (17sp bold, deep purple)
|  |  Sealdah Station Rd, ...  |  |   |  <- Address (13sp, 85% opacity tone)
|  +------------------------------+   |
|                                     |
|  +-- Peach gradient ------------+   |
|  |  WQXW+M73                 | X|   |  <- Name (17sp bold, deep orange)
|  |  WQXW+M73, Dhakuria-...   |  |   |  <- Address (13sp, 85% opacity tone)
|  +------------------------------+   |
|                                     |
|                              (+)    |  <- FAB, 24dp margin from edges
|                                     |
+-------------------------------------+

RecyclerView:
  Max width:      600dp
  Padding top:    8dp
  clipToPadding:  false
  Item margins:   16dp horizontal, 6dp vertical

FAB position:     Bottom-right, constrained to RecyclerView end
```

**Tap behavior**: Tapping the card body (not delete) returns to Main Screen with that
location pre-filled as the destination.

### 8.5 Distance History Screen

```
Root:             ConstraintLayout
Background:       background_soft (#F9F4F8 / #1C1B1A)

Layout (80% Content | 20% Centered Delete):
+-------------------------------------+
|                                     |
|  Today                              |  <- Section header (14sp bold, secondary)
|                                     |
|  +-- Blue gradient (80% | 20%) -+   |
|  |  Chandpara        23.18 KM| X|   |  <- Row 1: Destination (17sp) + Tracked Dist (13sp)
|  |  Aug 21 8:05 PM   5m 23s  |  |   |  <- Row 2: Start TS | Stop TS | Elapsed (13sp)
|  |  -------------------------+  |   |
|  |  +0:00 (Start) 8:05 PM 23.18KM   |  <- Expanded 3-Column Table (12sp)
|  |  + 2:30        8:07 PM 12.50KM   |     Col 1: Elapsed | Col 2: Clock | Col 3: Dist
|  |  + 5:23 (End)   8:10 PM  0.05KM   |
|  +------------------------------+   |
|                                     |
|  Yesterday                          |
|  +-- Peach gradient (80% | 20%) +   |
|  |  Sealdah          14.20 KM| X|   |
|  |  Aug 20 6:15 PM   12m 10s |  |   |
|  +------------------------------+   |
|                                     |
|  Older                              |
|  ...                                |
+-------------------------------------+

Expand/collapse:  Tap on entry row toggles expansion. Only ONE card expanded at a time.
Snapshot logic:   Post-hoc time-based tier selection (A: 11 rows, B: 8, C: 3, D: 2)
Overflow menu:    "Clear All" with confirmation dialog
```

### 8.6 Settings Screen

```
Root:             ScrollView -> ConstraintLayout -> LinearLayout (vertical)
Background:       background_soft (#F9F4F8)
Max content width: 600dp

Layout:
+-------------------------------------+
|                                     |
|  Preferences                        |  <- Section header (16sp bold, charcoal)
|                                     |
|  Theme                              |  <- Label (13sp, gray_purple)
|  [Light / Dark / System v]          |  <- Spinner
|                                     |
|  Units                              |
|  [KM (Default) / Miles / Both v]    |
|                                     |
|  Show meters under 1 km    [====]   |  <- Switch row
|                                     |
|  GPS Accuracy                       |
|  [High Accuracy v]                  |
|                                     |
|  Update Frequency                   |
|  [Every 3s v]                       |
|                                     |
|  Background Tracking       [====]   |  <- Switch row
|                                     |
|  -----------------------------------| <- Divider (1dp, gray at 30%)
|                                     |
|  About                              |  <- Section header
|                                     |
|  Privacy Policy                     |  <- Clickable row
|                                     |
|  Version                            |
|  v1.0.0 (1)                         |  <- Dynamic from BuildConfig
|                                     |
|  Credits                            |
|  Built with Google Maps Platform.   |
|                                     |
+-------------------------------------+

Row height:       48dp minimum (for touch targets)
Switch row:       Horizontal — label (weight 1) + Switch
Spinner row:      Label above, Spinner below
```

### 8.7 Speedometer Screen (V1 Placeholder)

```
Root:             ConstraintLayout
Background:       background_soft (#F9F4F8)

Layout:
+-------------------------------------+
|                                     |
|                                     |
|          Coming Soon                |  <- Centered, 18sp, text_secondary
|                                     |
|                                     |
+-------------------------------------+
```

### 8.8 Map Picker Screen

```
Root:             ConstraintLayout
Background:       background_soft (#F9F4F8 / #1C1B1A)

Layout:
+-------------------------------------+
|  +-- Map Card (16dp radius) ------+ |
|  |                                | |
|  |          GOOGLE MAP            | |  <- SupportMapFragment inside CardView
|  |      (tap to drop pin)         | |     12dp margin, 2dp elevation
|  |                                | |
|  |             PIN                | |  <- Dropped pin at tap location
|  |                                | |
|  +--------------------------------+ |
+-------------------------------------+
|                                     |
|  +-----------------------------+    |
|  |     CONFIRM LOCATION        |    |  <- Primary button, 16dp margin
|  +-----------------------------+    |     max width 600dp
|                                     |
+-------------------------------------+
```

---

## 9. Status Bar & System UI

### 9.1 Status Bar

```
Light mode:
  Background:     Transparent (content draws behind)
  Icon color:     Dark (android:windowLightStatusBar = true)

Dark mode:
  Background:     Transparent
  Icon color:     Light (android:windowLightStatusBar = false)

Edge-to-edge:     fitsSystemWindows="true" on activity root
                   Content respects insets automatically
```

### 9.2 Navigation Bar (System)

```
Light mode:
  Background:     Transparent or background_soft
  Button color:   Dark

Dark mode:
  Background:     Transparent or background_soft_dark
  Button color:   Light
```

### 9.3 Notification (Background Tracking)

```
Channel name:     "Distance Tracking"
Importance:       Default (sound + vibration on first post)
Icon:             App icon (small, monochrome for status bar)
Color:            primary_violet (#7C4DFF)
Title:            "Tracking to [Destination Name]"
Body:             Live distance value, formatted per unit settings
Action:           "Stop Tracking" — stops the foreground service
Ongoing:          Yes (non-dismissible while tracking) — V3 adds toggle
Tap action:       Opens Tracking Screen
```

---

## 10. Animations & Transitions

### 10.1 Screen Transitions

| Transition | Animation | Duration |
|---|---|---|
| Splash to Main | Fade in / Fade out | 300ms (system default) |
| Main to Tracking | Slide up / Slide down | 300ms |
| Main to Saved Locations | Slide right (Navigation default) | 300ms |
| Main to History | Slide right (Navigation default) | 300ms |
| Main to Map Picker | Slide right (Navigation default) | 300ms |
| Tab switching | Crossfade | 150ms |

### 10.2 Component Animations

| Component | Animation | Duration | Easing |
|---|---|---|---|
| Button press | Scale down to 0.96 + ripple | 100ms | Decelerate |
| Card press | Ripple effect | 200ms | Material default |
| FAB press | Elevation change (6dp to 12dp) + ripple | 100ms | Decelerate |
| List item expand | Height change (collapse/expand) | 250ms | FastOutSlowIn |
| Banner appear | Slide down from top | 200ms | Decelerate |
| Banner dismiss | Slide up | 200ms | Accelerate |
| Distance text update | Crossfade | 150ms | Linear |
| Empty state appear | Fade in | 300ms | Decelerate |
| Dialog appear | Fade in + scale from 0.9 | 200ms | Decelerate |
| Dialog dismiss | Fade out | 150ms | Accelerate |

### 10.3 Map Animations

| Action | Animation | Duration |
|---|---|---|
| Initial map load | Zoom to current location | 1000ms |
| Recenter button tap | Smooth camera move to current location | 600ms |
| Polyline update | Immediate (no animation — real-time feel) | — |
| Marker position update | Smooth position interpolation | 500ms |

### 10.4 Tracking Screen Live Updates

```
Distance text:    Updates on every location callback (1-10s interval per settings)
                   No animation between values — instant swap for real-time feel
                   Number format transition: if digits change count, layout adjusts smoothly

Polyline:         Redrawn on every location update
                   Old line removed, new line drawn including new point
                   No visible flicker (double-buffered by Maps SDK)

Current marker:   Smooth interpolation between old and new position (500ms)
                   Avoids "teleporting" effect on fast updates
```

---

## 11. Responsive & Adaptive Layout

### 11.1 Breakpoints

| Category | Width Range | Behavior |
|---|---|---|
| Small phone | < 360dp | Content uses full width, reduced margins (12dp) |
| Standard phone | 360-412dp | Standard layout as designed, 16dp margins |
| Large phone | 412-600dp | Standard layout, content naturally wider |
| Tablet portrait | 600-840dp | Content capped at 600dp max, centered |
| Tablet landscape | > 840dp | Two-column layout possible for tracking/speedometer |

### 11.2 Constraint Strategy

```
All content containers:
  Width:          0dp (match_constraint)
  Max width:      600dp (constraintWidth_max)
  Horizontal:     Constrained start+end to parent (auto-centers)

Text:             All sizes in sp (respects system font scaling)
Touch targets:    48dp minimum (per Material accessibility guidelines)
Icons:            Fixed dp sizes (don't scale with font size)
```

### 11.3 Landscape Adaptations (V2+)

**Tracking Screen in Landscape:**
```
+------------------+------------------+
|                  |                  |
|   GOOGLE MAP     |  Distance card   |
|   (60% width)    |  + Controls      |
|                  |  (40% width)     |
|                  |                  |
+------------------+------------------+
```

**Speedometer Screen in Landscape (V2):**
```
+------------------+------------------+
|                  |                  |
|   Speed Gauge    |   GOOGLE MAP     |
|   + Stats        |   (trip path)    |
|   + Controls     |                  |
|                  |                  |
+------------------+------------------+
```

---

## 12. Accessibility

### 12.1 Color Contrast

All text must meet WCAG 2.1 AA minimum contrast ratios:

| Combination | Ratio Required | Actual Ratio | Pass? |
|---|---|---|---|
| `text_charcoal` on `background_soft` | 4.5:1 | ~8.2:1 | Yes |
| `text_charcoal` on `card_white` | 4.5:1 | ~10.5:1 | Yes |
| `text_gray_purple` on `card_white` | 4.5:1 | ~3.8:1 | Large text only |
| `text_white` on `primary_violet` | 4.5:1 | ~5.2:1 | Yes |
| `text_white` on `error_coral` | 4.5:1 | ~4.6:1 | Yes |

> **Note**: `text_gray_purple` (#90A4AE) on white backgrounds only passes for
> large text (18sp+ or 14sp+ bold). Use it only for secondary/caption text at
> appropriate sizes, or darken to `#78909C` for better contrast.

### 12.2 Content Descriptions

Every interactive element must have a `contentDescription`:

| Element | Content Description |
|---|---|
| Saved Locations button | "Saved Locations" |
| History button | "Distance History" |
| Map Pick button | "Pick on Map" |
| Saved Dropdown button | "Saved Locations" |
| Add FAB | "Save New Location" |
| Delete button | "Delete" |
| Recenter FAB | "Recenter On My Location" |
| Start Tracking button | Label is visible text |
| Stop Tracking button | Label is visible text |

### 12.3 Touch Targets

All interactive elements: **48x48dp minimum** touch target.
Achieved via:
- Direct sizing (48dp width/height for icon buttons)
- Padding (for smaller visual elements)
- `selectableItemBackgroundBorderless` for expanded ripple area

### 12.4 Font Scaling

- All text sizes use `sp` units
- Layouts use `wrap_content` heights for text containers
- Cards use `wrap_content` height with internal `ScrollView` where needed
- Test at 100%, 150%, and 200% font scale

---

## 13. App Icon & Branding

### 13.1 App Icon Concept

The icon should visually represent "distance between two points":
- Two location pins connected by a straight line or arc
- Or a stylized ruler/compass motif
- Primary colors: `primary_violet` and white
- Clean, geometric, modern

### 13.2 Adaptive Icon Structure

```
Foreground:       Custom vector — pins/line motif in primary_violet on transparent
Background:       Solid or gradient — background_soft (#F9F4F8) or white

Required sizes:
  mdpi:           48x48px
  hdpi:           72x72px
  xhdpi:          96x96px
  xxhdpi:         144x144px
  xxxhdpi:        192x192px
  Play Store:     512x512px (high-res icon)
```

### 13.3 Brand Colors Summary

```
Primary:          #7C4DFF (Electric Violet)
Secondary:        #E1BEE7 (Soft Lavender)
Background:       #F9F4F8 (Blush White)
Text:             #37474F (Blue-Gray Charcoal)
```

---

## 14. Design Tokens — Quick Reference

### All Resource Names at a Glance

#### colors.xml

```xml
<!-- Backgrounds -->
background_soft, background_soft_dark, bg_gradient_start, bg_gradient_end

<!-- Primary -->
primary_violet, primary_lavender

<!-- Semantic -->
success_mint, warning_peach, error_coral, info_blue

<!-- Surfaces -->
card_white, card_white_translucent, card_dark, glass_white

<!-- Text -->
text_charcoal, text_gray_purple, text_primary, text_secondary, text_white

<!-- Accent -->
accent_warm, accent_teal, pill_dark
```

#### Drawable Resources

```
bg_bottom_nav.xml          — Bottom nav rounded rect (24dp radius, card_white)
bg_nav_pill.xml            — Active nav pill (24dp radius, pill_dark)
bg_icon_chip.xml           — Circular icon button (oval, card_white, 1dp border)
bg_card_mint.xml           — Gradient card (45 deg, #B2DFDB to #E0F2F1, 24dp radius)
bg_card_peach.xml          — Gradient card (45 deg, #FFCCBC to #FFF3E0, 24dp radius)
bg_card_lavender.xml       — Gradient card (45 deg, #E1BEE7 to #F3E5F5, 24dp radius)
bg_card_blue.xml           — Gradient card (45 deg, #BBDEFB to #E3F2FD, 24dp radius)
```

#### Theme Styles

```
Theme.TrueDistance               — Main app theme (DayNight.NoActionBar)
Theme.TrueDistance.Splash        — Splash screen theme (Theme.SplashScreen parent)
Theme.TrueDistance.Dialog        — Rounded dialog theme (24dp corners)
Widget.App.Button                — Primary button (12dp radius, violet)
Widget.App.Button.Text           — Text button (violet text)
Widget.App.BottomNavigationView  — Bottom nav style
SettingsSectionHeader            — 16sp bold charcoal
SettingsLabel                    — 13sp gray_purple
SettingsSpinner                  — Full width spinner
SettingsRow                      — Horizontal row with center_vertical gravity
```

---

## 15. Implementation Checklist

Use this checklist to verify design compliance during development:

### Colors & Theming
- [ ] All colors defined in `colors.xml` (no hardcoded hex in layouts)
- [ ] Dark mode `colors.xml` in `values-night/` with proper overrides
- [ ] Theme applied via `DayNight` parent — switches dynamically
- [ ] Status bar transparent with `windowLightStatusBar` set per mode
- [ ] Map style JSON applied for desaturated appearance

### Typography
- [ ] All text sizes in `sp` (not `dp`)
- [ ] Key data numerals use display/headline sizes per spec
- [ ] Font weights match spec (Bold for headings, Medium for labels, Regular for body)
- [ ] No text smaller than 11sp

### Layout & Spacing
- [ ] `fitsSystemWindows="true"` on activity root
- [ ] `constraintWidth_max="600dp"` on all content containers
- [ ] All spacing follows 4dp grid
- [ ] Card internal padding is 16dp
- [ ] List items have 16dp horizontal margin + 8dp vertical margin
- [ ] RecyclerViews have `clipToPadding="false"` with top padding

### Components
- [ ] Buttons use 12dp corner radius, 48dp min height
- [ ] Cards use 20-24dp corner radius
- [ ] Bottom nav uses 24dp corner radius, 8dp elevation
- [ ] Icon chips are circular (oval shape) with 1dp border
- [ ] All touch targets >= 48dp
- [ ] Content descriptions on all interactive elements

### Animations
- [ ] Splash to Main uses fade transition
- [ ] History items expand/collapse smoothly
- [ ] Banners slide in/out
- [ ] Map camera movements are animated
- [ ] No jarring instant state changes

### Responsive
- [ ] Tested on 360dp phone, 412dp phone, 800dp+ tablet
- [ ] Content centers correctly on wide screens
- [ ] Bottom nav doesn't stretch beyond 600dp
- [ ] Portrait and landscape both work

---

## 16. Version History

| Version | Date | Changes |
|---|---|---|
| 1.0 | 2026-08-20 | Initial comprehensive design specification |
| 1.1 | 2026-08-22 | Finalized 80/20 card split, 20dp padding, updated typography (17sp/13sp), tone-matched text colors (85% alpha row 2), dark theme overrides, soft-bordered map card frames (16dp radius), single-card expand with 3-column table, and KM default unit. |

---

> **Note**: This document supersedes all previous design references in
> `project-overview.md` section 9 (Visual Style Guide). When there is a conflict
> between this document and that section, this document takes precedence.
