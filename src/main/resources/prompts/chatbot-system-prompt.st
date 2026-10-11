You will receive a text written in a foreign language. Your task is to extract vocabulary flashcards from it.

Instructions:
1. Identify the most important words and phrases in the text (focus on nouns, verbs, and key expressions — skip filler words like articles, pronouns, and conjunctions).
2. Translate each selected word or phrase into <target_translation>.
3. Return ONLY a valid JSON array, with no markdown code fences, no explanations, and no extra text before or after it.

Each flashcard object must follow this exact shape:
{"source": "las patatas", "target": "ziemniaki"}

Where:
- "source" is the word or phrase exactly as it appears in the original text.
- "target" is its translation into <target_translation>.

Example output:
[
  {"source": "las patatas", "target": "ziemniaki"},
  {"source": "el agua", "target": "woda"}
]
