# VIVA VOCE PREPARATION & QUESTIONS
## PY-04: Fake News and Spam Message Detector
**15+ University & Technical Viva Questions with Model Answers**

---

### Q1: What is the primary objective of this project?
**Answer**: The project builds a dual-domain machine learning classification web platform that accurately classifies news content into `real` vs `fake` and messaging communication into `ham` vs `spam`. It compares three foundational algorithms (Naive Bayes, Logistic Regression, and Support Vector Machines) and exposes evaluation metrics and confidence scores.

### Q2: Why is it critical to enforce strict domain separation between news and spam?
**Answer**: News verification and spam detection involve distinct linguistic structures and objectives. News falsehoods often mirror formal journalistic syntax while making sensational or unverified claims. Spam messages feature urgent phishing hooks, commercial lures, and direct calls to action. Training a single generic model conflates these domains, leading to poor accuracy and high false positives.

### Q3: How do you prevent data leakage during TF-IDF vectorization and evaluation?
**Answer**: Data leakage occurs when statistics from the test set contaminate the training process. We prevent this by performing an 80/20 train/test split *before* vectorization. Using scikit-learn `Pipeline`, the `TfidfVectorizer` is fitted strictly on the training partition (`X_train`) and merely transforms the held-out test partition (`X_test`).

### Q4: Explain the mathematical intuition behind TF-IDF.
**Answer**: Term Frequency-Inverse Document Frequency calculates the relevance of a word to a document in a collection.
$$\text{TF}(t, d) = \frac{\text{count}(t, d)}{\text{total words in } d}$$
$$\text{IDF}(t, D) = \log\left(\frac{1 + |D|}{1 + |\{d \in D : t \in d\}|}\right) + 1$$
$$\text{TF-IDF} = \text{TF} \times \text{IDF}$$
Common words (like "the", "is") receive low weights, while domain-salient discriminatory terms (like "unauthorized", "miracle") receive high weights.

### Q5: How does Multinomial Naive Bayes work for text classification?
**Answer**: It applies Bayes' theorem under the assumption that word occurrences are conditionally independent given the class:
$$P(C_k | \mathbf{x}) \propto P(C_k) \prod_{i=1}^{n} P(x_i | C_k)$$
Laplace smoothing ($\alpha = 1.0$) is applied to prevent zero-probability multiplications for unseen vocabulary words.

### Q6: How does Logistic Regression determine the class of an input?
**Answer**: It models the log-odds of the positive class as a linear combination of TF-IDF feature weights:
$$z = \mathbf{w}^T \mathbf{x} + b$$
$$\sigma(z) = \frac{1}{1 + e^{-z}}$$
If $\sigma(z) \ge 0.5$, the sample is assigned to the positive class; otherwise, it is assigned to the negative class.

### Q7: Why does Linear SVM perform exceptionally well on text classification?
**Answer**: Text in TF-IDF space produces high-dimensional, sparse feature vectors. Linear SVM finds the maximum-margin hyperplane that separates the classes with minimum generalization error, without suffering from the curse of dimensionality.

### Q8: What evaluation metrics are reported, and why is accuracy alone insufficient?
**Answer**: We compute Accuracy, Precision, Recall, F1-Score, and Confusion Matrices. Accuracy alone can be misleading in imbalanced datasets (e.g., if 95% of emails are ham, a dummy model predicting always ham achieves 95% accuracy). Precision measures false alarm rates, Recall measures detection coverage, and F1-score provides their harmonic mean.

### Q9: How are the confusion matrix quadrants defined in this system?
**Answer**:
- **True Negative (TN)**: Real news / Ham messages correctly classified.
- **False Positive (FP)**: Real news / Ham messages mistakenly flagged as Fake / Spam (type I error).
- **False Negative (FN)**: Fake news / Spam messages missed by the detector (type II error).
- **True Positive (TP)**: Fake news / Spam messages correctly detected.

### Q10: How does the application generate calibrated confidence scores?
**Answer**:
- For Logistic Regression and Naive Bayes, `predict_proba()` computes exact class posterior probabilities.
- For Linear SVM, `CalibratedClassifierCV` applies Platt scaling (sigmoid calibration) over cross-validation folds to output reliable probability estimates.

### Q11: Why is an ethical disclaimer prominently displayed beneath predictions?
**Answer**: Machine learning models produce statistical associations based on training distributions, not definitive metaphysical truth. Presenting model predictions as indisputable fact could mislead users. The disclaimer informs users that the verdict is an algorithmic estimate.

### Q12: How are trained models preserved between application runs?
**Answer**: We serialize the complete trained scikit-learn `Pipeline` (including the fitted `TfidfVectorizer` and model weights) using `joblib.dump()`. On startup, `joblib.load()` reconstructs the pipeline in memory without requiring re-training.

### Q13: What happens when an input contains only stopwords or unknown tokens?
**Answer**: The preprocessor filters them out. If the resulting cleaned token string has zero length, the API returns a validation error requesting more descriptive input rather than making a random prediction.

### Q14: What testing methodology was executed?
**Answer**: Python's `unittest` framework verifies:
- Token cleaning and URL stripping.
- Integrity of accuracy, precision, recall, and F1 calculations.
- Veracity of real and fake news classification.
- Veracity of ham and spam message classification.
- Multi-model comparison consensus logic.

### Q15: How can this system be enhanced to detect sophisticated modern disinformation?
**Answer**:
1. Implement contextual Transformer encoders such as BERT, RoBERTa, or DeBERTa.
2. Incorporate knowledge-graph fact-checking APIs (e.g., Google Fact Check Tools API).
3. Analyze publisher reputation metadata and domain age alongside textual content.

