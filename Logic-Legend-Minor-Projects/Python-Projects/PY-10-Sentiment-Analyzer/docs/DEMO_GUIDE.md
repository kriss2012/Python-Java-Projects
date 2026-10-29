# Evaluator Live Demonstration Guide: PY-10 Sentiment Analysis of Product Reviews

This guide provides a rapid step-by-step walkthrough for faculty evaluators, external examiners, and project mentors to inspect, test, and evaluate the PY-10 application.

---

## 1. Quick Verification Checklist
| Step | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Run Unit Tests | 8 tests pass in < 0.1s (`Ran 8 tests ... OK`) | [ ] |
| 2 | CLI Single Review Analysis | Instant VADER, TextBlob, and Aspect breakdown | [ ] |
| 3 | CLI Corpus Summary & Ranking | Category table, product rankings, and Net Sentiment Score | [ ] |
| 4 | CSV Report Export | Exported to `exports/sentiment_analysis_report.csv` | [ ] |
| 5 | Launch Streamlit Web UI | Interactive web dashboard opens on port 8501 | [ ] |

---

## 2. Command Line Demonstration (Fastest & 100% Deterministic)

### Step 1: Execute Automated Test Suite
```bash
python tests/test_sentiment.py
```
**Verification Note**: Look for:
```
Ran 8 tests in 0.044s
OK
```

### Step 2: Test Single Review Text Analysis via CLI
```bash
python src/main.py --analyze "The sound quality is fantastic and crystal clear, but the battery drains fast." --cli
```
**Output Highlights to Observe**:
- `Classification : POSITIVE`
- `VADER Compound : +0.4767 (Pos: 0.272, Neu: 0.728, Neg: 0.0)`
- `TextBlob Polarity : +0.2750 (-1.0 to +1.0)`
- `Subjectivity : 0.5708`
- `Aspect-Based Sentiment Breakdown`:
  - `Audio & Sound` : POSITIVE
  - `Battery & Power` : POSITIVE

### Step 3: Run Full Corpus Summary, Product Rankings & CSV Export
```bash
python src/main.py --summary --export --cli
```
**Output Highlights to Observe**:
1. **Corpus Statistics**:
   - Total Reviews Analyzed: 25
   - Positive Sentiment Count: 15 (60.0%)
   - Negative Sentiment Count: 9 (36.0%)
   - Net Sentiment Score (NSS): +24.0%
   - Average Compound Score: +0.212
2. **Category Breakdown Table**:
   - Audio (8 reviews), Displays (3 reviews), Laptops (3 reviews), Wearables (3 reviews), etc.
3. **Product Ranking Table**:
   - Top-ranked: *AeroWatch Active Smartwatch* (+0.519 avg compound)
   - Bottom-ranked: *SwiftCharge 65W GaN Charger* (-0.475 avg compound)
4. **Aspect Aggregation**:
   - Detailed mention counts and average scores for Battery, Audio, Build, Price, Performance, Display, and Support.
5. **CSV Export Confirmation**:
   - `[EXPORT] Full analyzed dataset exported to: exports/sentiment_analysis_report.csv`

---

## 3. Web Dashboard Demonstration (Streamlit Web UI)

### Step 1: Launch Application
```bash
streamlit run src/app.py
```
Open your browser to `http://localhost:8501`.

### Step 2: Test Dashboard Tabs
1. **Tab 1: 📊 Corpus Overview**:
   - Inspect the KPI summary cards (Total Reviews, Positive %, Negative %, Net Sentiment Score, Avg Rating).
   - View the interactive Plotly Donut chart showing sentiment proportions.
   - Hover over the VADER Compound distribution histogram.
   - Inspect the Category-wise bar chart and Product Ranking table.
2. **Tab 2: 🔍 Aspect-Based Sentiment (ABSA)**:
   - View horizontal bar chart comparing satisfaction scores across *Battery, Sound, Display, Performance, Build, Price, Support*.
3. **Tab 3: 🔤 Key Phrases & N-Grams**:
   - Inspect top recurring positive and negative 2-word phrases mined from the review corpus.
4. **Tab 4: ✍️ Live Review Inspector**:
   - Type or paste custom review text into the input box.
   - Watch the predicted sentiment badge, compound score, polarity, and aspect breakdown update in real time.
5. **Tab 5: 💾 Data Export**:
   - Click **📥 Download Augmented Dataset (CSV)** to download the complete processed dataset.

---

## 4. Evaluator Viva Questions to Ask the Student
1. *"Why did you choose VADER over simple bag-of-words or TF-IDF classifiers?"*
   - Student should explain that VADER incorporates heuristics for capitalization, punctuation, degree adverbs, and negation without requiring large training corpora.
2. *"What is the significance of the Net Sentiment Score (NSS)?"*
   - Student should explain that NSS (% Positive - % Negative) provides a single, intuitive metric directly indicating customer advocacy momentum.
3. *"How does the system isolate sentiment for specific aspects like battery life versus display?"*
   - Student should explain sentence-level segmentation and aspect dictionary keyword matching in `SentimentEngine.analyze_aspects()`.

