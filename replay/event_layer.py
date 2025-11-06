"""
CoreWars8086 Replay Event Layer

This module implements Event and EventManager classes described in the Copilot context dump.
Place this module near your replay controller and import EventManager to power HUD, memory overlays
and the transport controls.

Example usage:
    manager = EventManager()
    manager.load_jsonl('replay.jsonl')
    manager.register_callback('on_event_triggered', ui_flash_handler)
    events_at_cycle = manager.get_events_in_cycle(1234)
    next_ev = manager.get_next_event(1234)

The module is intentionally GUI-agnostic: callbacks receive event objects and the UI should decide
how to visualize them.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import Optional, Dict, List, Callable, Any, Iterable, Tuple
import json
import bisect
import logging

logger = logging.getLogger(__name__)

# Event importance categories (consistent with spec)
IMPORTANT_TYPES = {"ENSNARE", "DEATH", "OVERWRITE"}
OPTIONAL_TYPES = {"NEAR_MISS", "LOOP"}


@dataclass(order=True)
class Event:
    id: int
    round: int
    type: str
    symbol: str
    actor: str
    target: Optional[str] = None
    address: Optional[int] = None
    details: Optional[str] = ""
    score_contrib: float = 0.0

    @classmethod
    def from_dict(cls, d: Dict[str, Any]) -> "Event":
        # Accept keys flexibly and coerce types
        return cls(
            id=int(d.get("id", 0)),
            round=int(d.get("round", d.get("cycle", 0))),
            type=str(d.get("type", "UNKNOWN")),
            symbol=str(d.get("symbol", "")),
            actor=d.get("actor"),
            target=d.get("target", None),
            address=(None if d.get("address") is None else int(d.get("address"))),
            details=d.get("details", ""),
            score_contrib=float(d.get("score_contrib", 0.0)),
        )

    def is_important(self) -> bool:
        return self.type in IMPORTANT_TYPES


class EventManager:
    """Manage replay events, indexing by cycle and emitting UI callbacks.

    Callbacks supported (register with `register_callback(name, fn)`):
      - on_event_triggered(event)
      - on_event_added(event)
      - on_import_completed(count)

    The manager stores events in a flat chronological list (`self._events`) and
    an index mapping round -> list of events for quick seeking.
    """

    def __init__(self):
        self._events: List[Event] = []
        self._rounds: List[int] = []  # list of sorted rounds for binary search
        self._index_by_round: Dict[int, List[Event]] = {}
        self._callbacks: Dict[str, List[Callable[..., Any]]] = {}
        self._id_counter = 1

    # ---------- Loading and adding ----------
    def load_jsonl(self, path: str) -> int:
        """Load events from a JSONL file. Each line is a JSON dict matching Event structure.
        Returns the number of loaded events."""
        count = 0
        with open(path, "r", encoding="utf-8") as fh:
            for line in fh:
                line = line.strip()
                if not line:
                    continue
                try:
                    d = json.loads(line)
                except Exception:
                    logger.exception("Failed parsing event JSON line: %s", line)
                    continue
                ev = Event.from_dict(d)
                # assign id if missing
                if not ev.id:
                    ev.id = self._id_counter
                    self._id_counter += 1
                self.add_event(ev)
                count += 1
        self._trigger_callbacks("on_import_completed", count)
        return count

    def add_event(self, event: Event) -> None:
        """Insert event keeping chronological order and update cycle index."""
        # Insert into flat list maintaining order by round then id
        # Using bisect on rounds list is tricky because multiple events share same round.
        # We'll maintain _events sorted by (round,id) using the Event ordering.
        insert_pos = bisect.bisect_right(self._events, event)
        self._events.insert(insert_pos, event)

        # update index_by_round
        lst = self._index_by_round.setdefault(event.round, [])
        # keep per-round insertion by id order using bisect
        per_round_pos = bisect.bisect_right(lst, event)
        lst.insert(per_round_pos, event)

        # maintain sorted rounds list
        if event.round not in self._rounds:
            bisect.insort(self._rounds, event.round)

        self._trigger_callbacks("on_event_added", event)

    # ---------- Lookup helpers ----------
    def get_events_in_cycle(self, cycle: int) -> List[Event]:
        # kept for backward compatibility
        return list(self._index_by_round.get(cycle, []))
    
    def get_events_in_round(self, round_: int) -> List[Event]:
        """Return list of events that occurred in a given round."""
        return list(self._index_by_round.get(round_, []))

    def _find_event_index_by_cycle(self, cycle: int) -> int:
        """Return insertion index in the flat `_events` list for the earliest event >= cycle."""
        class _Cmp(Event):
            pass

        dummy = Event(id=0, round=cycle, type="", symbol="", actor="")
        i = bisect.bisect_left(self._events, dummy)
        return i

    def get_next_event(self, current_cycle: int) -> Optional[Event]:
        # Find the next round greater than current_cycle
        idx = bisect.bisect_right(self._rounds, current_cycle)
        if idx < len(self._rounds):
            next_round = self._rounds[idx]
            lst = self._index_by_round.get(next_round, [])
            return lst[0] if lst else None
        return None

    def get_previous_event(self, current_cycle: int) -> Optional[Event]:
        # Find the previous round strictly less than current_cycle
        idx = bisect.bisect_left(self._rounds, current_cycle) - 1
        if idx >= 0:
            prev_round = self._rounds[idx]
            lst = self._index_by_round.get(prev_round, [])
            return lst[-1] if lst else None
        return None

    def get_important_events(self) -> List[Event]:
        return [e for e in self._events if e.is_important()]

    def get_events_between(self, start_cycle: int, end_cycle: int) -> List[Event]:
        i = self._find_event_index_by_cycle(start_cycle)
        j = self._find_event_index_by_cycle(end_cycle + 1)
        return list(self._events[i:j])

    # ---------- Rendering hooks / utilities ----------
    def render_event_on_memory(self, event: Event) -> Dict[str, Any]:
        """Return a small structure that the memory view can interpret to overlay icons.

        Example return value:
            {"address": event.address, "symbol": event.symbol, "duration_ms": 800, "css_class": "ensnare"}
        """
        if event.address is None:
            address = None
        else:
            address = int(event.address)

        data = {
            "address": address,
            "symbol": event.symbol,
            "duration_ms": 800 if event.is_important() else 400,
            "type": event.type,
            "details": event.details,
            "actor": event.actor,
            "target": event.target,
        }
        return data

    # ---------- Callbacks and triggering ----------
    def register_callback(self, name: str, fn: Callable[..., Any]) -> None:
        self._callbacks.setdefault(name, []).append(fn)

    def unregister_callback(self, name: str, fn: Callable[..., Any]) -> None:
        if name in self._callbacks and fn in self._callbacks[name]:
            self._callbacks[name].remove(fn)

    def _trigger_callbacks(self, name: str, *args, **kwargs) -> None:
        for fn in list(self._callbacks.get(name, [])):
            try:
                fn(*args, **kwargs)
            except Exception:
                logger.exception("Callback %s failed", fn)

    def trigger_event_now(self, event: Event) -> None:
        """External caller can trigger the UI hooks for a single event (e.g. when seeking to a cycle).
        This will call `on_event_triggered` and supply rendering metadata."""
        overlay = self.render_event_on_memory(event)
        self._trigger_callbacks("on_event_triggered", event, overlay)
        # fire audio/subtitle hook too (UI will map event -> sfx/text)
        self._trigger_callbacks("on_play_audio", event)
        # notify log row update
        self._trigger_callbacks("on_log_update", event)

    # ---------- Filtering and utilities ----------
    def filter_events(self, types: Optional[Iterable[str]] = None, important_only: bool = False) -> List[Event]:
        evs = self._events
        if types is not None:
            typeset = set(types)
            evs = [e for e in evs if e.type in typeset]
        if important_only:
            evs = [e for e in evs if e.is_important()]
        return list(evs)


# Simple CLI/testing helper
if __name__ == "__main__":
    import sys
    from pathlib import Path

    if len(sys.argv) < 2:
        print("Usage: python event_layer.py replay.jsonl")
        raise SystemExit(2)

    path = Path(sys.argv[1])
    if not path.exists():
        print("File not found:", path)
        raise SystemExit(2)

    mgr = EventManager()
    count = mgr.load_jsonl(str(path))
    print(f"Loaded {count} events")
    print(f"Important events: {len(mgr.get_important_events())}")
