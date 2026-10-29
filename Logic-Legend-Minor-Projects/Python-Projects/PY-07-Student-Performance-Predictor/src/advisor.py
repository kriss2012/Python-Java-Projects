"""
Logic Legend - PY-07 Student Performance Predictor
Personalized Study Recommendations & Academic Advisor Engine
"""

class AcademicAdvisor:
    @staticmethod
    def generate_recommendations(features, prediction_result, target_goal=75.0):
        """
        Generates explainable, actionable academic advice tailored to individual weaknesses.
        """
        rec_list = []
        pred_score = prediction_result.get("predicted_score", 50.0)
        attendance = features.get("attendance_pct", 100.0)
        study_hours = features.get("study_hours_weekly", 10.0)
        past_marks = features.get("past_exam_score", 50.0)
        internals = features.get("internal_test_score", 50.0)
        assignments = features.get("assignments_completed", 100.0)

        # 1. Attendance Check
        if attendance < 75.0:
            deficit = round(75.0 - attendance, 1)
            rec_list.append({
                "category": "⚠️ Attendance Deficit (High Priority)",
                "urgency": "CRITICAL",
                "message": f"Your current attendance ({attendance}%) is {deficit}% below the university minimum (75%). You risk exam debarment. Prioritize attending all remaining lecture hours."
            })
        elif attendance < 85.0:
            rec_list.append({
                "category": "Attendance Buffer",
                "urgency": "MEDIUM",
                "message": f"Your attendance is {attendance}%. Aim to build a safe buffer above 85% to accommodate any emergency medical absences."
            })

        # 2. Study Hours Allocation
        if study_hours < 8.0:
            rec_list.append({
                "category": "📚 Study Hours Scheduling",
                "urgency": "HIGH",
                "message": f"Current weekly study commitment ({study_hours} hrs/week) is critically low. Recommended baseline is at least 14 hours/week (2 hours/day). Implement the Pomodoro technique (25 min study, 5 min break)."
            })
        elif study_hours < 14.0:
            rec_list.append({
                "category": "📚 Study Hours Optimization",
                "urgency": "MEDIUM",
                "message": f"Increasing weekly revision from {study_hours} to 16 hours/week is projected to elevate your predicted exam score by 6 to 9 percentage points."
            })

        # 3. Continuous Assessment & Tutorials
        if assignments < 65.0:
            rec_list.append({
                "category": "📝 Assignment Submissions",
                "urgency": "HIGH",
                "message": f"Only {assignments}% of assignments were completed. Timely tutorial submission accounts for internal credit and reinforces core problem-solving schemas."
            })

        if internals < 50.0:
            rec_list.append({
                "category": "🔍 Concept Remediation",
                "urgency": "HIGH",
                "message": f"Internal test performance ({internals}%) indicates conceptual bottlenecks. Attend departmental tutoring or faculty doubt-clearing sessions."
            })

        # 4. Target Gap Analysis
        if pred_score < target_goal:
            gap = round(target_goal - pred_score, 1)
            rec_list.append({
                "category": "🎯 Target Goal Alignment",
                "urgency": "LOW",
                "message": f"Your projected score ({pred_score}%) is currently {gap}% away from your goal of {target_goal}%. Focus on high-weightage topics and past 5-year question papers."
            })
        else:
            rec_list.append({
                "category": "🌟 Performance Maintenance",
                "urgency": "LOW",
                "message": f"Excellent! Your predicted score ({pred_score}%) meets or exceeds your goal ({target_goal}%). Maintain regular weekly revision schedules."
            })

        return rec_list

