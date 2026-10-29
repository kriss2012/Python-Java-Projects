"""
Logic Legend - PY-06 Personal Voice Assistant
Speech Recognition & Text-to-Speech Engine
"""

import threading
import speech_recognition as sr
import pyttsx3

class VoiceEngine:
    def __init__(self, voice_enabled=True, rate=175, volume=0.9):
        self.voice_enabled = voice_enabled
        self.rate = rate
        self.volume = volume
        self.tts_lock = threading.Lock()
        self._init_tts()
        self.recognizer = sr.Recognizer()

    def _init_tts(self):
        try:
            self.engine = pyttsx3.init()
            self.engine.setProperty("rate", self.rate)
            self.engine.setProperty("volume", self.volume)
        except Exception:
            self.engine = None

    def speak(self, text):
        """Converts text to synthesized speech safely."""
        if not self.voice_enabled or not self.engine or not text:
            return

        def _worker():
            with self.tts_lock:
                try:
                    self.engine.say(text)
                    self.engine.runAndWait()
                except Exception:
                    pass

        t = threading.Thread(target=_worker, daemon=True)
        t.start()

    def listen(self, timeout=5, phrase_time_limit=8):
        """
        Listens to microphone input and converts to text using SpeechRecognition.
        Returns {'text': '...'} or {'error': '...'}
        """
        try:
            with sr.Microphone() as source:
                self.recognizer.adjust_for_ambient_noise(source, duration=0.6)
                audio = self.recognizer.listen(source, timeout=timeout, phrase_time_limit=phrase_time_limit)
                text = self.recognizer.recognize_google(audio)
                return {"text": text.strip()}
        except sr.WaitTimeoutError:
            return {"error": "Listening timed out. No speech detected."}
        except sr.UnknownValueError:
            return {"error": "Could not decipher speech. Please repeat or type."}
        except Exception as e:
            return {"error": f"Microphone unavailable or hardware access error: {str(e)}"}

