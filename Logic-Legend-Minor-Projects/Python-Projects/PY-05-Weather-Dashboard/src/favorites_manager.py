"""
Logic Legend - PY-05 Weather Dashboard and Forecast App
Favorites Storage Manager (JSON Persistence)
"""

import os
import json

DEFAULT_FAVORITES = ["New Delhi", "Mumbai", "Bengaluru", "London", "Tokyo"]

class FavoritesManager:
    def __init__(self, filepath="data/favorites.json"):
        self.filepath = filepath
        dir_name = os.path.dirname(self.filepath)
        if dir_name and not os.path.exists(dir_name):
            os.makedirs(dir_name, exist_ok=True)
        self._ensure_file()

    def _ensure_file(self):
        if not os.path.exists(self.filepath):
            self.save_favorites(DEFAULT_FAVORITES)

    def get_favorites(self):
        try:
            with open(self.filepath, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            return DEFAULT_FAVORITES

    def add_favorite(self, city):
        city = city.strip().title()
        if not city:
            return False, "City name cannot be empty."
        favs = self.get_favorites()
        if city in favs:
            return False, f"'{city}' is already in favorites."
        favs.append(city)
        self.save_favorites(favs)
        return True, f"'{city}' added to favorites."

    def remove_favorite(self, city):
        favs = self.get_favorites()
        if city in favs:
            favs.remove(city)
            self.save_favorites(favs)
            return True, f"'{city}' removed from favorites."
        return False, f"'{city}' not found in favorites."

    def save_favorites(self, favs):
        with open(self.filepath, "w", encoding="utf-8") as f:
            json.dump(favs, f, indent=2)
