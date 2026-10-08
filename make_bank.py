# -*- coding: utf-8 -*-
import sys
import json

questions = []

def add_q(q_id, grade, topic, diff, q_text, options, correct_idx, expl, independent_calc=None):
    assert len(options) == 4, f"Question {q_id} does not have 4 options"
    assert 0 <= correct_idx < 4, f"Question {q_id} correct_idx out of range"
    assert len(set(options)) == 4, f"Question {q_id} has duplicate options: {options}"
    if independent_calc is not None:
        calc_str = str(independent_calc).strip()
        corr_opt = options[correct_idx].strip()
        assert calc_str in corr_opt or corr_opt in calc_str, f"Mismatch in Q{q_id}: calc={calc_str}, opt={corr_opt}"
    questions.append({
        "id": q_id,
        "grade": grade,
        "topic": topic,
        "difficulty": diff,
        "question": q_text,
        "options": options,
        "correctIndex": correct_idx,
        "explanation": expl,
        "calc": independent_calc
    })

print("Helper ready")
