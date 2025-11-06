corewars8086
============

Core Wars for standard 8086 assembly.

Replay Event Layer
------------------
We added a lightweight Python `EventManager` to power replay UI features and event-driven overlays at
`replay/event_layer.py`. It loads events from a JSONL replay file and exposes callbacks for UI components
to subscribe to (e.g. `on_event_triggered`, `on_event_added`, `on_import_completed`).

Quick usage:

```powershell
python replay\event_layer.py path\to\replay.jsonl
```

Or import `EventManager` into your replay frontend to receive events and render overlays.
