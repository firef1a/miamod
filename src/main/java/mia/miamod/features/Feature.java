package mia.miamod.features;

public abstract class Feature {
    protected boolean enabled;
    protected String id, name, description;

    public Feature(String name, String id, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = true;
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean getEnabled() { return this.enabled; }

    public String getID() { return this.id; }
    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
}