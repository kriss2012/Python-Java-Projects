# Viva Examination Questions and Answers: PY-10 Sentiment Analysis of Product Reviews

### Q1: What is the difference between VADER and traditional machine learning classifiers like Naive Bayes?
**Answer**: Traditional ML classifiers (e.g., Naive Bayes or SVM) require a large labeled training dataset to learn feature weights, and their bag-of-words representations often ignore grammatical context, capitalization, and punctuation. VADER (Valence Aware Dictionary and sEntiment Reasoner) is a rule-based lexicon specifically engineered for social media and review text. It incorporates five heuristic rules (contrastive conjunctions like "but", degree modifiers like "extremely", capitalization, exclamation marks, and negation tri-grams) to calculate sentiment intensity without needing prior model training.

### Q2: What is the VADER compound score, and how is it normalized?
**Answer**: The compound score is a single metric that quantifies the overall sentiment of a text between -1.0 (extremely negative) and +1.0 (extremely positive). It is calculated by summing the valence ratings of each word in the text and normalizing the sum $x$ using the formula $c = \frac{x}{\sqrt{x^2 + \alpha}}$, where $\alpha = 15$ is a standard normalization parameter.

### Q3: What is Aspect-Based Sentiment Analysis (ABSA), and why is it necessary?
**Answer**: Traditional sentiment analysis assigns one global sentiment label to an entire review. However, customer reviews frequently express mixed opinions about different components (e.g., *"The display is gorgeous, but the battery dies in two hours"*). ABSA parses individual sentences, identifies which product aspects (Battery, Display, Sound, Build, etc.) are being discussed, and computes targeted sentiment scores for each aspect individually.

### Q4: Explain the difference between Polarity and Subjectivity in TextBlob.
**Answer**:
- **Polarity**: A floating-point value lying in the range $[-1.0, 1.0]$ where $-1.0$ indicates extreme negativity, $0.0$ indicates neutrality, and $+1.0$ indicates extreme positivity.
- **Subjectivity**: A floating-point value in the range $[0.0, 1.0]$ where $0.0$ indicates an entirely objective statement of fact (e.g., *"The laptop has an aluminum chassis"*), and $1.0$ indicates an entirely subjective personal opinion (e.g., *"I think this is the most gorgeous laptop in existence"*).

### Q5: What is Net Sentiment Score (NSS), and how is it calculated?
**Answer**: Net Sentiment Score is modeled after the Net Promoter Score (NPS) used in market research. It is calculated as $\text{NSS} = \% \text{Positive Reviews} - \% \text{Negative Reviews}$, resulting in a score ranging from $-100\%$ to $+100\%$. It provides business leaders with an instantaneous, high-level pulse on whether customer sentiment is overwhelmingly favorable or unfavorable.

### Q6: How does the system handle words following contrastive conjunctions like "but" or "however"?
**Answer**: Contrastive conjunctions signal a shift in sentiment where the clause following the conjunction carries greater psychological weight than the preceding clause. VADER applies an emphasis multiplier (typically boosting valence by 50% or shifting focus) to the subsequent clause, ensuring sentences like *"The camera is decent, but the battery life is terrible"* result in an accurate net negative compound score.

### Q7: Why do we extract n-grams (e.g., bigrams) rather than just single words (unigrams)?
**Answer**: Single words often lose crucial context. For example, the unigram "life" or "support" has neutral valence, whereas the bigrams "battery life" or "customer support" convey specific product dimensions. Furthermore, bigrams like "poor quality" capture compound sentiment that single unigrams would miss or misclassify.

### Q8: What fallback mechanisms exist if NLTK's `vader_lexicon` cannot be downloaded due to an offline testing environment?
**Answer**: `SentimentEngine` implements a built-in rule-based fallback lexicon (`_fallback_vader_scores`) containing curated positive and negative valence words. If the external NLTK download fails due to an offline viva environment, the application falls back seamlessly to the local lexicon without crashing.

### Q9: How are stopwords handled in text preprocessing?
**Answer**: Common English functional words (e.g., "the", "is", "at", "which") occur with high frequency but carry negligible sentiment value. In `TextPreprocessor`, we filter out stopwords during tokenization and n-gram extraction so that only informative nouns, verbs, and adjectives are analyzed.

### Q10: How does the application scale to process large CSV files?
**Answer**: In `AnalyzerService.analyze_dataframe()`, vector operations and list comprehensions iterate over the review column without using slow Python row-by-row DataFrame appends. The computed sentiment scores are added as vectorized columns in $O(N)$ linear time.

### Q11: What visualization framework is used in the Streamlit dashboard, and why?
**Answer**: We use **Plotly Express and Plotly Graph Objects**. Unlike static Matplotlib plots, Plotly generates interactive, responsive JavaScript visualizations with hover tooltips, zoom capabilities, and dynamic filtering directly inside the browser.

### Q12: How can this system detect sarcasm or ironic statements?
**Answer**: Lexicon-based methods inherently struggle with sarcasm because words with positive literal definitions (e.g., *"Oh brilliant, another software crash"*) carry negative contextual meaning. For advanced sarcasm detection, transformer-based architectures (like BERT or RoBERTa) fine-tuned on sarcastic corpora are required.

### Q13: What is the purpose of the Product Ranking feature?
**Answer**: It aggregates reviews by product name, calculating average star ratings, positive review percentages, and mean compound scores. This allows brand managers to objectively benchmark product variants against each other and identify underperforming items.

### Q14: How does the application ensure data privacy when users upload CSV files?
**Answer**: Uploaded files are processed purely in-memory using Streamlit's temporary memory buffer. Files are not written to shared public servers or transmitted to third-party APIs, ensuring complete local confidentiality.

### Q15: How can a student run this project in an environment without a graphical desktop?
**Answer**: The application includes a full CLI interface in `src/main.py`. By passing `--cli` along with `--summary` or `--analyze "<text>"`, the entire analysis runs and displays clean ASCII tables and statistics directly in the terminal.

