# Logic Legend — PY-10: Sentiment Analysis of Product Reviews

An advanced Natural Language Processing (NLP) web dashboard and batch analysis platform built in Python using Streamlit, Plotly, Pandas, NLTK (VADER), and TextBlob. The system ingests e-commerce customer reviews, computes composite sentiment metrics (polarity, subjectivity, VADER compound score), performs fine-grained Aspect-Based Sentiment Analysis (ABSA) across hardware features, ranks products by customer satisfaction, and exports actionable analytical reports.

---

## 1. Project Title and Identifier
- **Project Name**: Sentiment Analysis of Product Reviews
- **Project Code**: `PY-10`
- **Application Category**: Natural Language Processing, Sentiment Mining & Web Analytics
- **Target Audience**: E-commerce product managers, brand analysts, data science students, and faculty evaluators.

---

## 2. Executive Summary
Online customer reviews represent a goldmine of consumer sentiment, yet unstructured text cannot be efficiently parsed at scale through manual inspection. General star ratings are often insufficient because a single 3-star review may praise a laptop's display while condemning its battery life and cooling fans. The **Product Review Sentiment Analyzer** bridges this gap by combining rule-based lexical analysis (VADER), linguistic subjectivity modeling (TextBlob), and sentence-level Aspect-Based Sentiment Analysis (ABSA). It enables stakeholders to instantly quantify overall brand sentiment, pinpoint specific product flaws, benchmark competing products, and derive data-driven product improvement roadmaps.

---

## 3. Key Features
- **Dual-Model Sentiment Engine**:
  - **VADER (Valence Aware Dictionary and sEntiment Reasoner)**: Specifically tuned for social/review text, emoticons, capitalizations, and intensity modifiers.
  - **TextBlob**: Evaluates semantic polarity (-1.0 to +1.0) and subjectivity (0.0=pure objective fact to 1.0=pure subjective opinion).
- **Aspect-Based Sentiment Analysis (ABSA)**: Parses sentences to isolate reviews mentioning specific hardware domains:
  - Battery & Power, Audio & Sound, Display & Visuals, Performance & Speed, Build & Design, Price & Value, and Customer Support.
- **Corpus-Wide Statistical Analytics**:
  - Net Sentiment Score (NSS = % Positive - % Negative).
  - VADER compound score distribution histogram.
  - Interactive Plotly donut chart displaying class distribution (Positive, Neutral, Negative).
  - Category-wise sentiment comparison bar chart.
- **Product Satisfaction Ranking**: Automatically ranks products and brands by average sentiment compound score and positive review percentage.
- **Key Phrase & N-Gram Extraction**: Identifies the most frequent positive and negative bigrams (e.g., "battery life", "customer support", "sound quality").
- **Live Single Review Inspector**: Real-time interactive text area allowing evaluators to paste arbitrary customer feedback and immediately view compound gauges and extracted aspect breakdowns.
- **Custom CSV Upload & Export**: Users can upload arbitrary CSV files containing review columns, preview the live NLP augmentation, and download the full analysis as a formatted CSV report.

---

## 4. System Architecture
The application is structured into four modular layers:
1. **Presentation Layer (`src/app.py`, `src/main.py`)**: Multi-tab Streamlit dashboard with Plotly charts and dual-mode CLI for headless analysis.
2. **Business Logic & Analytics Layer (`src/analyzer_service.py`)**: Computes corpus aggregations, Net Sentiment Score, category breakdowns, product rankings, and CSV reports.
3. **NLP & Sentiment Layer (`src/sentiment_engine.py`)**: VADER intensity scoring, TextBlob polarity/subjectivity, and aspect-based sentence sentiment parsing.
4. **Text Preprocessing Layer (`src/text_preprocessor.py`)**: Sanitization, URL/HTML stripping, stopword filtering, tokenization, and n-gram extraction.

---

## 5. Technology Stack and Prerequisites
- **Language**: Python 3.10+
- **NLP Libraries**: `nltk >= 3.8.0`, `textblob >= 0.17.0`
- **Data Manipulation**: `pandas >= 2.0.0`, `numpy >= 1.24.0`
- **Visualization**: `plotly >= 5.18.0`, `matplotlib >= 3.7.0`
- **Web Dashboard**: `streamlit >= 1.28.0`

---

## 6. Directory Structure
```
PY-10-Sentiment-Analyzer/
├── .env.example              # Configuration template for port and sample paths
├── .gitignore                # Git exclusion rules for caches and user uploads
├── requirements.txt          # Python dependency specifications
├── README.md                 # 19-section comprehensive documentation
├── data/                     # Dataset storage directory
│   └── sample_product_reviews.csv # Curated multi-category product reviews
├── exports/                  # Exported CSV sentiment reports
├── docs/                     # Academic deliverables
│   ├── ACADEMIC_REPORT.md    # Detailed college report with Mermaid diagrams
│   ├── PRESENTATION_SLIDES.md# Viva presentation slide deck
│   ├── VIVA_QUESTIONS.md     # 15+ viva examination Q&As
│   └── DEMO_GUIDE.md         # Evaluator test procedure
├── src/
│   ├── __init__.py           # Package marker
│   ├── text_preprocessor.py  # Text cleaning, stopwords, and n-grams
│   ├── sentiment_engine.py   # VADER, TextBlob, and Aspect-Based Sentiment logic
│   ├── analyzer_service.py   # Statistical aggregations and product ranking
│   ├── app.py                # Interactive Streamlit dashboard
│   └── main.py               # Application entry point (GUI + CLI)
└── tests/
    ├── __init__.py           # Test package marker
    └── test_sentiment.py     # Automated unit test suite
```

---

## 7. Installation and Setup
1. Clone or navigate to the project directory:
   ```bash
   cd Logic-Legend-Minor-Projects/Python-Projects/PY-10-Sentiment-Analyzer
   ```
2. Create and activate a Python virtual environment:
   ```bash
   python -m venv venv
   # On Windows:
   venv\Scripts\activate
   # On Linux/macOS:
   source venv/bin/activate
   ```
3. Install required packages:
   ```bash
   python -m pip install -r requirements.txt
   ```

---

## 8. Usage Instructions
### Running the Streamlit Web Application
Launch the interactive web dashboard:
```bash
python src/main.py
# Or directly via streamlit:
streamlit run src/app.py
```
Open your browser to `http://localhost:8501`.

### Running in Headless / CLI Mode
Analyze a single review string from the command line:
```bash
python src/main.py --analyze "The sound quality is fantastic and crystal clear, but the battery drains fast." --cli
```

Compute full corpus statistics, category breakdown, product rankings, and export CSV:
```bash
python src/main.py --summary --export --cli
```

---

## 9. Sentiment Scoring Methodology
### 1. VADER Compound Score
The compound score is computed by summing the valence scores of each word in the lexicon, adjusted according to five heuristic rules (punctuation, capitalization, degree adverbs, contrastive conjunctions, and tri-gram negations), then normalized between -1 (most extreme negative) and +1 (most extreme positive):
$$x = \sum \text{valence}, \quad c = \frac{x}{\sqrt{x^2 + \alpha}}, \quad \text{where } \alpha = 15$$

**Classification Thresholds**:
- **Positive**: $c \ge 0.05$
- **Neutral**: $-0.05 < c < 0.05$
- **Negative**: $c \le -0.05$

### 2. Net Sentiment Score (NSS)
Modeled after the Net Promoter Score (NPS), NSS indicates customer satisfaction momentum:
$$\text{NSS} = \% \text{Positive Reviews} - \% \text{Negative Reviews}$$

---

## 10. Aspect-Based Sentiment Analysis (ABSA) Architecture
Standard sentiment models assign a single label to an entire paragraph, blurring opposing sentiments. Our ABSA engine:
1. Tokenizes text into individual sentence units.
2. Intersects sentence tokens with predefined domain dictionaries (e.g., Battery: *charge, drain, runtime*).
3. Applies VADER sentiment scoring specifically to the isolated aspect sentence.
4. Aggregates scores across all aspect occurrences to compute a domain-specific satisfaction score.

---

## 11. Testing and Verification
Run the automated test suite:
```bash
python tests/test_sentiment.py
```
Test results verify:
- ✅ Text preprocessor URL/HTML removal and bigram extraction
- ✅ VADER intensity scoring and classification accuracy
- ✅ TextBlob polarity and subjectivity calculations
- ✅ Sentence-level Aspect-Based Sentiment extraction (Battery, Sound)
- ✅ Batch DataFrame augmentation
- ✅ Summary metrics and Net Sentiment Score (NSS) calculations
- ✅ Category breakdown and product satisfaction ranking
- ✅ CSV report generation and data integrity

---

## 12. Security and Error Handling
- **Offline Fallback Lexicon**: If NLTK data is unreachable due to institutional firewalls, a built-in sentiment lexicon prevents crashes.
- **Input Sanitization**: Strips HTML tags and script injections from uploaded review text.
- **DataFrame Null Handling**: Safe NaN coercion prevents type mismatch errors on missing review descriptions.

---

## 13. Academic Relevance and College Submission
This project satisfies university curriculum criteria for:
- Natural Language Processing & Computational Linguistics
- Data Science and Business Intelligence
- Web Application Engineering with Streamlit
- Statistical Modeling and Information Extraction
- Big Data Analytics and Opinion Mining

---

## 14. Known Limitations
- Heavy sarcasm or irony (e.g., *"Oh great, another broken wire"*) can occasionally challenge lexicon-based heuristics without context transformers.
- Complex compound sentences with double negatives require deep syntactic dependency parsers.

---

## 15. Future Enhancements
- Integration of HuggingFace transformer pipelines (e.g., RoBERTa or DistilBERT) for zero-shot sentiment classification.
- Real-time API connectors to scrape and analyze live Amazon or Google Play Store reviews.
- Multi-lingual sentiment analysis for regional Indian languages (Hindi, Tamil, Telugu).

---

## 16. Evaluator Quick Start
```bash
# 1. Run unit tests
python tests/test_sentiment.py

# 2. Run CLI summary and export CSV
python src/main.py --summary --export --cli

# 3. Launch Streamlit Web UI
streamlit run src/app.py
```

---

## 17. References
- Hutto, C.J. & Gilbert, E.E. (2014). VADER: A Parsimonious Rule-based Model for Sentiment Analysis of Social Media Text. ICWSM.
- Loria, S. (2020). TextBlob: Simplified Text Processing.
- Bird, S., Klein, E., & Loper, E. (2009). Natural Language Processing with Python. O'Reilly.

---

## 18. License
Educational Open-Source License — Free for academic, learning, and college submission use.

