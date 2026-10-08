# -*- coding: utf-8 -*-
import sys
from generate_all_questions import q, questions

# We will define 30 questions for each level: 1 (Easy), 2 (Medium), 3 (Hard), 4 (Konkur)
# Total = 120 questions!
# In each level: 10 from Grade 10, 10 from Grade 11, 10 from Grade 12.
# And correctIndex will cycle exactly 0, 1, 2, 3 so every level has a perfectly balanced raw bank (around 25% for each position).

print("Full bank generator ready to run")
