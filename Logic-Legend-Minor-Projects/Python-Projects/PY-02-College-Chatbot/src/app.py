"""
Logic Legend - PY-02 AI Chatbot for College Queries
Flask Web Application & API Controller
"""

import os
import sys
import uuid
from datetime import datetime
from flask import Flask, render_template, request, jsonify, session, redirect, url_for

# Ensure src in path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from database import ChatbotDatabase
from nlp_engine import NLPEngine

app = Flask(__name__)
app.secret_key = os.environ.get("SECRET_KEY", "logic_legend_chatbot_secret_2026")

base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
db_path = os.path.join(base_dir, "data", "chatbot.db")
kb_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "knowledge_base.json")

db = ChatbotDatabase(db_path=db_path)
nlp = NLPEngine(kb_path=kb_path, confidence_threshold=float(os.environ.get("CONFIDENCE_THRESHOLD", 0.30)))

SUGGESTED_PROMPTS = [
    "How to apply for admission?",
    "What is the fee structure for B.Tech?",
    "What are the placement statistics and highest package?",
    "What are college hours and timetable?",
    "What campus and hostel facilities are available?",
    "Are there any merit scholarships?",
    "Talk to college admin"
]

@app.route("/")
def index():
    if "session_id" not in session:
        session["session_id"] = str(uuid.uuid4())
    return render_template("index.html", suggestions=SUGGESTED_PROMPTS)

@app.route("/api/chat", methods=["POST"])
def api_chat():
    data = request.get_json() or {}
    user_msg = data.get("message", "").strip()
    session_id = data.get("session_id") or session.get("session_id") or "anonymous"

    if not user_msg:
        return jsonify({"error": "Empty message"}), 400

    # Predict intent
    prediction = nlp.predict_intent(user_msg)
    timestamp = datetime.now().strftime("%I:%M %p")

    # Log interaction
    db.log_interaction(
        session_id=session_id,
        user_message=user_msg,
        bot_response=prediction["response"],
        detected_intent=prediction["tag"],
        confidence=prediction["confidence"]
    )

    return jsonify({
        "response": prediction["response"],
        "tag": prediction["tag"],
        "confidence": prediction["confidence"],
        "verified": prediction["verified"],
        "suggest_escalation": prediction["suggest_escalation"],
        "timestamp": timestamp
    })

@app.route("/api/escalate", methods=["POST"])
def api_escalate():
    data = request.get_json() or {}
    name = data.get("name", "").strip()
    email = data.get("email", "").strip()
    phone = data.get("phone", "").strip()
    query = data.get("query", "").strip()

    if not name or not email or not query:
        return jsonify({"error": "Name, email, and query are required."}), 400

    esc_id = db.create_escalation(name, email, phone, query)
    return jsonify({
        "success": True,
        "escalation_id": esc_id,
        "message": f"Escalation ticket #{esc_id} created. A college administrator will contact you shortly."
    })

@app.route("/api/suggestions")
def api_suggestions():
    return jsonify({"suggestions": SUGGESTED_PROMPTS})

# ----------------- ADMIN ROUTES -----------------
@app.route("/admin", methods=["GET", "POST"])
def admin_portal():
    if request.method == "POST":
        username = request.form.get("username", "").strip()
        password = request.form.get("password", "").strip()
        user = db.verify_admin(username, password)
        if user:
            session["admin_user"] = user["username"]
            session["admin_role"] = user["role"]
            return redirect(url_for("admin_portal"))
        else:
            return render_template("admin.html", error="Invalid admin credentials.")

    if "admin_user" not in session:
        return render_template("admin.html", login_required=True)

    escalations = db.get_escalations()
    logs = db.get_chat_history(limit=25)
    intents = nlp.get_all_intents()

    return render_template(
        "admin.html",
        login_required=False,
        admin_user=session["admin_user"],
        escalations=escalations,
        logs=logs,
        intents=intents
    )

@app.route("/admin/resolve/<int:esc_id>", methods=["POST"])
def admin_resolve_escalation(esc_id):
    if "admin_user" not in session:
        return jsonify({"error": "Unauthorized"}), 401
    db.resolve_escalation(esc_id)
    return redirect(url_for("admin_portal"))

@app.route("/admin/kb/add", methods=["POST"])
def admin_add_intent():
    if "admin_user" not in session:
        return jsonify({"error": "Unauthorized"}), 401

    tag = request.form.get("tag", "").strip()
    patterns = [p.strip() for p in request.form.get("patterns", "").split("\n") if p.strip()]
    responses = [r.strip() for r in request.form.get("responses", "").split("\n") if r.strip()]

    nlp.add_intent(tag, patterns, responses)
    return redirect(url_for("admin_portal"))

@app.route("/admin/kb/delete/<tag>", methods=["POST"])
def admin_delete_intent(tag):
    if "admin_user" not in session:
        return jsonify({"error": "Unauthorized"}), 401

    nlp.delete_intent(tag)
    return redirect(url_for("admin_portal"))

@app.route("/admin/logout")
def admin_logout():
    session.pop("admin_user", None)
    session.pop("admin_role", None)
    return redirect(url_for("admin_portal"))

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=True)

