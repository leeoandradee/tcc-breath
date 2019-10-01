package breath.mackenzie.com.br.tcc_breath;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
    "Atelectasis",
    "Normal",
    "Pneumonia"
})
public class RespostaDto {

    @JsonProperty("Atelectasis")
    private String Atelectasis;
    @JsonProperty("Normal")
    private String Normal;
    @JsonProperty("Pneumonia")
    private String Pneumonia;

    @JsonProperty("Atelectasis")
    public String getAtelectasis() {
        return Atelectasis;
    }

    @JsonProperty("Atelectasis")
    public void setAtelectasis(String atelectasis) {
        Atelectasis = atelectasis;
    }

    @JsonProperty("Normal")
    public String getNormal() {
        return Normal;
    }

    @JsonProperty("Normal")
    public void setNormal(String normal) {
        Normal = normal;
    }

    @JsonProperty("Pneumonia")
    public String getPneumonia() {
        return Pneumonia;
    }

    @JsonProperty("Pneumonia")
    public void setPneumonia(String pneumonia) {
        Pneumonia = pneumonia;
    }
}
