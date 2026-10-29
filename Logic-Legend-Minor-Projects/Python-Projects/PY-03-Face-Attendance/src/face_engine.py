"""
Logic Legend - PY-03 Face Recognition Attendance System
OpenCV Face Detection & Matching Engine
"""

import os
import cv2
import numpy as np

class FaceEngine:
    def __init__(self, confidence_threshold=0.60):
        self.confidence_threshold = confidence_threshold
        # Load Haar Cascade
        cascade_path = cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
        self.face_cascade = cv2.CascadeClassifier(cascade_path)
        if self.face_cascade.empty():
            raise RuntimeError("Failed to load OpenCV Haar Cascade Classifier.")

    def detect_faces(self, frame):
        """
        Detects bounding boxes of faces in a BGR frame.
        Returns list of (x, y, w, h).
        """
        if frame is None or frame.size == 0:
            return []
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        faces = self.face_cascade.detectMultiScale(
            gray,
            scaleFactor=1.1,
            minNeighbors=5,
            minSize=(60, 60)
        )
        return faces

    def extract_face_signature(self, face_bgr):
        """
        Extracts a normalized, equalized grayscale 100x100 face representation
        and its flattened color/intensity histogram.
        """
        if face_bgr is None or face_bgr.size == 0:
            return None
        resized = cv2.resize(face_bgr, (100, 100))
        gray = cv2.cvtColor(resized, cv2.COLOR_BGR2GRAY)
        equalized = cv2.equalizeHist(gray)
        # 32-bin normalized histogram
        hist = cv2.calcHist([equalized], [0], None, [32], [0, 256])
        cv2.normalize(hist, hist, alpha=0, beta=1, norm_type=cv2.NORM_MINMAX)
        return {
            "image": equalized,
            "histogram": hist
        }

    def compute_similarity(self, sig1, sig2):
        """
        Computes similarity between two face signatures using
        histogram correlation combined with normalized pixel correlation.
        Returns score between 0.0 and 1.0.
        """
        if sig1 is None or sig2 is None:
            return 0.0

        # 1. Histogram correlation
        hist_sim = cv2.compareHist(sig1["histogram"], sig2["histogram"], cv2.HISTCMP_CORREL)
        hist_sim = max(0.0, float(hist_sim))

        # 2. Template matching score on normalized grayscale
        res = cv2.matchTemplate(sig1["image"], sig2["image"], cv2.TM_CCOEFF_NORMED)
        template_sim = max(0.0, float(res[0][0]))

        combined_score = round(0.5 * hist_sim + 0.5 * template_sim, 2)
        return min(1.0, combined_score)

    def match_against_enrolled(self, captured_face_bgr, enrolled_dict):
        """
        Compares a captured face against dictionary of enrolled student signatures.
        enrolled_dict = { student_id: signature }
        Returns (best_student_id, best_confidence) or (None, best_confidence).
        """
        captured_sig = self.extract_face_signature(captured_face_bgr)
        if captured_sig is None or not enrolled_dict:
            return None, 0.0

        best_id = None
        best_score = 0.0

        for student_id, enrolled_sig in enrolled_dict.items():
            score = self.compute_similarity(captured_sig, enrolled_sig)
            if score > best_score:
                best_score = score
                best_id = student_id

        if best_score >= self.confidence_threshold:
            return best_id, best_score
        return None, best_score

    @staticmethod
    def generate_synthetic_face(seed_color=(180, 150, 120)):
        """Creates a realistic synthetic face image for offline testing/demo."""
        img = np.full((200, 200, 3), 240, dtype=np.uint8)
        # Face oval
        cv2.ellipse(img, (100, 100), (60, 80), 0, 0, 360, seed_color, -1)
        # Eyes
        cv2.circle(img, (80, 80), 8, (50, 50, 50), -1)
        cv2.circle(img, (120, 80), 8, (50, 50, 50), -1)
        # Mouth
        cv2.ellipse(img, (100, 130), (25, 10), 0, 0, 180, (40, 40, 150), 3)
        return img

