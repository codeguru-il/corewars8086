package war;

import org.json.JSONObject;

public class ReplayEvent {
    public final String type;
    public final int round;
    public final JSONObject data;

    public ReplayEvent(JSONObject json) {
        this.data = json;
        this.type = json.getJSONObject("payload").getString("type"); // Adjust based on your actual JSON structure
        this.round = json.getJSONObject("payload").optInt("round", -1); // Adjust based on your actual JSON structure
    }
}