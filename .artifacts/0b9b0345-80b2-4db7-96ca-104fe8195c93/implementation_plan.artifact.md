# Implementation Plan - Fix Search UI, Autocomplete, and Map Picker

This plan addresses three main issues:
1.  Widening the 'Search Destination' text box in the saved locations screen.
2.  Adding Google Places Autocomplete to search boxes in both the main screen and saved locations screen.
3.  Improving the map picker with zoom controls, current location marking, and default centering.

## User Review Required

> [!IMPORTANT]
> The Google Places SDK requires a valid API key with the "Places API" enabled in the Google Cloud Console. I've verified that `MAPS_API_KEY` is present in `local.properties`.

## Proposed Changes

### Core
#### [MODIFY] [TrueDistanceApp.kt](file:///C:/Users/sabuj/StudioProjects/TrueDistance/app/src/main/java/sabuj/m/truedistance/TrueDistanceApp.kt)
- Initialize Google Places SDK in `onCreate`.

---

### Main Screen (DistanceFragment)
#### [MODIFY] [fragment_distance.xml](file:///C:/Users/sabuj/StudioProjects/TrueDistance/app/src/main/res/layout/fragment_distance.xml)
- Change `destinationSearchBox` from `EditText` to `AutoCompleteTextView`.

#### [MODIFY] [DistanceFragment.kt](file:///C:/Users/sabuj/StudioProjects/TrueDistance/app/src/main/java/sabuj/m/truedistance/ui/distance/DistanceFragment.kt)
- Set up `PlacesAutocompleteAdapter` for the `destinationSearchBox`.
- Update search logic to handle selection from autocomplete.

---

### Saved Locations Screen
#### [MODIFY] [SavedLocationsFragment.kt](file:///C:/Users/sabuj/StudioProjects/TrueDistance/app/src/main/java/sabuj/m/truedistance/ui/savedlocations/SavedLocationsFragment.kt)
- Update `showSearchDialog` to use `AutoCompleteTextView` instead of `EditText`.
- Ensure the input field occupies the full width of the dialog.
- Integrate Places Autocomplete.

---

### Map Picker
#### [MODIFY] [MapPickerFragment.kt](file:///C:/Users/sabuj/StudioProjects/TrueDistance/app/src/main/java/sabuj/m/truedistance/ui/mappicker/MapPickerFragment.kt)
- Enable zoom controls on the map.
- Enable "My Location" layer (with permission check).
- Center map on current location upon initialization.

---

### Utilities
#### [NEW] [PlacesAutocompleteAdapter.kt](file:///C:/Users/sabuj/StudioProjects/TrueDistance/app/src/main/java/sabuj/m/truedistance/utils/PlacesAutocompleteAdapter.kt)
- A reusable adapter for `AutoCompleteTextView` that fetches predictions from Google Places SDK.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.

### Manual Verification
1.  **Main Screen**: Type in "Search destination" and verify autocomplete suggestions appear. Select one and verify the "To:" label updates.
2.  **Saved Locations**: Open "Search Address" dialog. Verify the text box is wide. Type and verify autocomplete suggestions.
3.  **Map Picker**: Open map picker. Verify +/- buttons are visible. Verify map centers on current location and shows the blue dot (if permission granted).
