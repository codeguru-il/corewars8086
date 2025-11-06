import tempfile
import json
import os
import pytest
from replay.event_layer import EventManager, Event


def make_sample_jsonl(path):
    events = [
        {"id": 1, "cycle": 10, "type": "ENSNARE", "symbol": "🧠", "actor": "A", "target": "B", "address": 100, "details": "enslaved"},
        {"id": 2, "cycle": 15, "type": "NEAR_MISS", "symbol": "⚡", "actor": "C", "address": 140, "details": "near"},
        {"id": 3, "cycle": 20, "type": "DEATH", "symbol": "☠", "actor": "B", "address": 120, "details": "illegal opcode"},
    ]
    with open(path, "w", encoding="utf-8") as fh:
        for e in events:
            fh.write(json.dumps(e, ensure_ascii=False) + "\n")


def test_load_and_index(tmp_path):
    p = tmp_path / "sample.jsonl"
    make_sample_jsonl(p)
    mgr = EventManager()
    cnt = mgr.load_jsonl(str(p))
    assert cnt == 3
    assert len(mgr.get_events_in_cycle(10)) == 1
    assert len(mgr.get_events_in_cycle(15)) == 1
    assert len(mgr.get_events_in_cycle(20)) == 1


def test_navigation_and_filters(tmp_path):
    p = tmp_path / "sample.jsonl"
    make_sample_jsonl(p)
    mgr = EventManager()
    mgr.load_jsonl(str(p))

    ev = mgr.get_next_event(10)
    assert ev.cycle == 15
    prev = mgr.get_previous_event(15)
    assert prev.cycle == 10

    important = mgr.get_important_events()
    # ENSNARE and DEATH are important
    types = [e.type for e in important]
    assert "ENSNARE" in types and "DEATH" in types


def test_render_event_on_memory():
    e = Event(id=1, cycle=5, type="ENSNARE", symbol="🧠", actor="A", address=55, details="x")
    mgr = EventManager()
    data = mgr.render_event_on_memory(e)
    assert data["address"] == 55
    assert data["symbol"] == "🧠"
    assert data["type"] == "ENSNARE"


if __name__ == "__main__":
    pytest.main(["-q"])