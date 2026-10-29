# PRESENTATION SLIDES OUTLINE
## PY-04: Fake News and Spam Message Detector
**Logic Legend — Minor Project Viva Presentation**

---

### Slide 1: Title & Overview
- **Project**: Fake News and Spam Message Detector (PY-04)
- **Domain**: Machine Learning, NLP & Cybersecurity
- **Technologies**: Python, Flask, scikit-learn, TF-IDF, Pandas, Joblib
- **Theme**: *Logic Legend — Ideas Today, Impact Tomorrow*

---

### Slide 2: Problem Statement & Motivation
- Millions of internet users encounter deceptive news articles and phishing SMS messages daily.
- Deceptive information leads to financial fraud and societal panic.
- Many amateur detectors combine unrelated tasks or present predictions without confidence metrics or disclaimers.

---

### Slide 3: Objectives & Solution Overview
- A dual-domain NLP classification platform:
  - **News Domain**: Classifies headlines/articles as `real` vs `fake`.
  - **Message Domain**: Classifies SMS/Email text as `ham` vs `spam`.
- Rigorous comparative evaluation of three ML algorithms (Naive Bayes, Logistic Regression, Support Vector Machine).
- Live calculation of Accuracy, Precision, Recall, F1, and Confusion Matrices.

---

### Slide 4: NLP Pipeline & Data Leakage Prevention
- **Text Cleaning**: Lowercase normalization, URL stripping, special character removal, stopword filtering.
- **TF-IDF Vectorization**: Term Frequency-Inverse Document Frequency feature weighting.
- **Scientific Rigor**: Vectorizer fitted strictly on training partition ($80\%$) and applied to test fold ($20\%$).

---

### Slide 5: Machine Learning Models Compared
1. **Multinomial Naive Bayes**: Fast probabilistic classifier based on word occurrence frequencies.
2. **Logistic Regression**: Linear decision boundary optimized via cross-entropy loss with calibrated class probabilities.
3. **Linear Support Vector Machine (SVM)**: Maximum-margin hyperplane separating classes with high generalization in high-dimensional text space.

---

### Slide 6: Model Evaluation Benchmark
- Detailed metrics on held-out test data:
  - **Accuracy**: Overall fraction of correct predictions.
  - **Precision**: Proportion of true positive detections among all predicted positives.
  - **Recall**: Proportion of actual positive samples identified.
  - **F1-Score**: Harmonic mean of Precision and Recall.
  - **Confusion Matrix**: Full breakdown of TP, FP, TN, and FN.

---

### Slide 7: Web Application Interface
- Modern Flask web application:
  - Dedicated tabs for News and Spam domains.
  - Model selection dropdown with single model or "Compare All 3 Models" mode.
  - Quick-demo sample pills for instantaneous live demonstration.
  - Visual confidence gauge and prominent statistical disclaimer.

---

### Slide 8: Demonstration Highlights
- Testing authentic news ("RBI repo rate policy") -> `REAL` with high confidence.
- Testing viral health hoax ("Boiled lemon juice cures cancer") -> `FAKE`.
- Testing lottery fraud SMS ("You won 1,000,000 cash prize") -> `SPAM`.
- Simultaneous cross-model consensus view showing agreement across all 3 architectures.

---

### Slide 9: Testing & Quality Assurance
- Automated test suite validating:
  - Text preprocessing and URL filtering.
  - Integrity of model evaluation metrics.
  - Exact domain label assignment (`real`/`fake` for news; `spam`/`ham` for messages).
  - Multi-model comparison logic.

---

### Slide 10: Limitations & Future Enhancements
- **Limitations**: Classical n-gram TF-IDF models do not capture deep contextual nuances or sarcastic linguistic subtleties.
- **Future Scope**:
  - Fine-tuning transformer models (BERT, RoBERTa) and comparing performance against classical baselines.
  - Multi-language news verification (Hindi, regional Indian languages).
  - Browser plugin for automated social media feed flagging.

---

### Slide 11: Conclusion & Q&A
- Developed a comprehensive, scientifically evaluated, dual-domain text classification platform.
- Ready for viva evaluation and technical questions.

