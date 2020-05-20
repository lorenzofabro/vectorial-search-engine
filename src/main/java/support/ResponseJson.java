package support;

import org.json.JSONArray;
import org.json.JSONObject;

public class ResponseJson {

    //************************* Enum para manejo de estado de respuesta
    public enum State {
        OK,
        ERROR
    }

    //************************* Atributos para manejo de objeto json
    private JSONObject responseJson = new JSONObject();
    private JSONObject dataResponseJson = new JSONObject();

    //************************* Constructor

    public ResponseJson() {
        this.responseJson.put("state", "OK");
        this.responseJson.put("message", "");
        this.responseJson.put("data", this.dataResponseJson);
    }

    //************************* Metodos

    public void setStateResponse(State state) {
        if (state == State.OK) {
            this.responseJson.put("state", "OK");
        } else if (state == State.ERROR) {
            this.responseJson.put("state", "ERROR");
        }
    }

    public void setMessageResponse (String message) {
        this.responseJson.put("message", message);
    }

    public void addDataResponse (String key, JSONObject value) {
        this.dataResponseJson.put(key, value);
    }

    public void addDataResponse (String key, JSONArray value) {
        this.dataResponseJson.put(key, value);
    }

    public String getResponseJson() {
        return this.responseJson.toString();
    }

}
