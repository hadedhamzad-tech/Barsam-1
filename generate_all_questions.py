# -*- coding: utf-8 -*-
import json
import math

persian_digits = "۰۱۲۳۴۵۶۷۸۹"
def to_latin(s):
    res = []
    for c in str(s):
        if c in persian_digits:
            res.append(str(persian_digits.index(c)))
        elif c in ['٫', '،', '/']:
            res.append('.')
        else:
            res.append(c)
    return "".join(res)

questions = []

def q(id_num, grade, topic, diff, question, options, correct_idx, expl, calc_val=None):
    assert len(options) == 4, f"Q{id_num} len options != 4"
    assert len(set(options)) == 4, f"Q{id_num} duplicate options: {options}"
    assert 0 <= correct_idx <= 3, f"Q{id_num} bad correct_idx: {correct_idx}"
    if calc_val is not None:
        calc_str = str(calc_val).strip()
        correct_text = options[correct_idx].strip()
        correct_latin = to_latin(correct_text)
        calc_latin = to_latin(calc_str)
        # Check numeric match or substring match
        matched = (calc_latin in correct_latin or correct_latin in calc_latin or calc_str in correct_text)
        try:
            val1 = float(calc_latin)
            val2 = float(correct_latin)
            if abs(val1 - val2) < 1e-4:
                matched = True
        except ValueError:
            pass
        assert matched, f"Q{id_num} verification failed: calc={calc_str} (latin={calc_latin}) not matched in correct_text={correct_text} (latin={correct_latin})"
    questions.append({
        "id": id_num,
        "grade": grade,
        "topic": topic,
        "difficulty": diff,
        "question": question,
        "options": options,
        "correctIndex": correct_idx,
        "explanation": expl,
        "verifiedCalc": str(calc_val) if calc_val is not None else None
    })

print("q func updated with robust float / Persian decimal parsing")
