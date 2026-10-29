# Viva Presentation Slides: PY-10 Sentiment Analysis of Product Reviews

---

### Slide 1: Title Slide
- **Project Title**: Sentiment Analysis of Product Reviews
- **Project Code**: PY-10 (Logic Legend Minor Project Suite)
- **Domain**: Natural Language Processing, Sentiment Mining & Web Analytics
- **Presenter**: Engineering Candidate
- **Advisor/Mentor**: Project Evaluation Committee

---

### Slide 2: Problem Statement
- Customer reviews contain vital feedback regarding product performance, ergonomics, and reliability.
- Manual evaluation of thousands of text reviews is unscalable.
- Overall 1-5 star ratings obscure nuanced feedback where a customer loves the display but despises the battery life.

---

### Slide 3: Proposed Solution
- An autonomous, interactive NLP dashboard built using Streamlit, Plotly, Pandas, VADER, and TextBlob.
- Multi-dimensional scoring: VADER compound intensity, TextBlob polarity (-1 to +1), and subjectivity (0 to 1).
- Aspect-Based Sentiment Analysis (ABSA) for 7 hardware and service dimensions.
- Brand and product performance ranking matrix.
- Interactive single-review inspector and custom CSV batch ingestion.

---

### Slide 4: System Architecture
- **Layer 1: User Interface**: Streamlit web dashboard + headless CLI.
- **Layer 2: Analytics Service**: `AnalyzerService` aggregating metrics, rankings, and exports.
- **Layer 3: NLP & Sentiment Engine**: `SentimentEngine` coordinating VADER, TextBlob, and ABSA rules.
- **Layer 4: Text Preprocessor**: `TextPreprocessor` for sanitization, stopword filtering, and n-gram extraction.

---

### Slide 5: Dual-Model Sentiment Engine
- **VADER (Rule-Based)**:
  - Specially designed for review and informal text.
  - Recognizes capitalization ("AMAZING"), punctuation ("great!!!"), degree adverbs ("extremely fast"), and negation ("not bad").
- **TextBlob (Linguistic Model)**:
  - Polarity: Quantifies positive vs negative sentiment orientation.
  - Subjectivity: Determines whether text is an objective statement or personal opinion.

---

### Slide 6: Aspect-Based Sentiment Analysis (ABSA)
- Extracts domain-specific sentiment across 7 core aspects:
  1. Battery & Power
  2. Audio & Sound
  3. Display & Visuals
  4. Performance & Speed
  5. Build & Design
  6. Price & Value
  7. Customer Support
- Isolates relevant sentences and computes independent aspect satisfaction scores.

---

### Slide 7: Corpus Analytics & Visualizations
- **Summary Metrics**: Total Reviews, % Positive, % Negative, and Net Sentiment Score (NSS).
- **Plotly Donut Chart**: Sentiment class distribution.
- **Histogram**: Frequency distribution of VADER compound scores.
- **Category Comparison**: Positive vs negative percentages across tech categories.
- **N-Gram Mining**: Most common positive and negative 2-word phrases.

---

### Slide 8: Live Demonstration Highlights
- Overview of pre-loaded tech reviews dataset (25 multi-category products).
- Inspecting the Net Sentiment Score (+24.0%) and aspect rankings.
- Testing the **Single Review Live Inspector** with arbitrary customer comments.
- Uploading custom CSV files and downloading the augmented output report.

---

### Slide 9: Automated Verification & Testing
- 8/8 Automated unit tests passing:
  - Text preprocessor cleaning, stopword removal, and n-grams.
  - VADER scoring on polarized sentences.
  - TextBlob polarity and subjectivity calculations.
  - Sentence-level aspect extraction and classification.
  - Batch DataFrame processing and feature appending.
  - Statistical summary metrics and Net Sentiment Score (NSS).
  - Category breakdown and product satisfaction rankings.
  - CSV report generation and validation.

---

### Slide 10: Conclusion & Future Scope
- **Conclusion**: A modular, accurate, zero-configuration sentiment intelligence tool suitable for college evaluation and enterprise deployment.
- **Future Scope**:
  - Fine-tuning BERT/RoBERTa transformers for contextual sarcasm detection.
  - Direct live scraping from Amazon/Flipkart review APIs.
  - Multi-language sentiment analysis for Indian vernacular languages.

