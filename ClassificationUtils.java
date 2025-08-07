package com.example.bamboospecies;

import android.graphics.Bitmap;
import java.util.HashMap;
import java.util.Map;

public class ClassificationUtils {

    public static Bitmap bitmapHolder;

    private static final Map<String, BambooMetadata> metadataMap = new HashMap<>();

    static {
        metadataMap.put("Beema", new BambooMetadata("Bambusa balcooa", "Poaceae", "Fast-growing, high biomass bamboo."));
        metadataMap.put("Bical Babi", new BambooMetadata("Bambusa vulgaris", "Poaceae", "Native to Southeast Asia, known for its thick walls."));
        metadataMap.put("Black Bamboo", new BambooMetadata("Phyllostachys nigra", "Poaceae", "Famous for its black culms, ornamental use."));
        metadataMap.put("Boos Bamboo", new BambooMetadata("Dendrocalamus giganteus", "Poaceae", "Very large, robust bamboo species."));
        metadataMap.put("Buddha Belly", new BambooMetadata("Bambusa ventricosa", "Poaceae", "Recognizable by swollen internodes."));
        metadataMap.put("Buho", new BambooMetadata("Schizostachyum lumampao", "Poaceae", "Used in traditional Filipino crafts."));
        metadataMap.put("Giant Bamboo", new BambooMetadata("Dendrocalamus asper", "Poaceae", "Large bamboo, often used in construction."));
        metadataMap.put("Giant Bolo", new BambooMetadata("Gigantochloa levis", "Poaceae", "Popular in Filipino basket weaving."));
        metadataMap.put("Hedge Bamboo", new BambooMetadata("Bambusa multiplex", "Poaceae", "Compact clumper, great for hedges."));
        metadataMap.put("Iron Bamboo", new BambooMetadata("Guadua angustifolia", "Poaceae", "Extremely strong and durable."));
        metadataMap.put("Japanese Bamboo", new BambooMetadata("Pseudosasa japonica", "Poaceae", "Low-growing, ideal for gardens."));
        metadataMap.put("Kawayan Kiling", new BambooMetadata("Bambusa blumeana", "Poaceae", "Philippine native with thorny nodes."));
        metadataMap.put("Kawayang Bayog", new BambooMetadata("Bambusa merrilliana", "Poaceae", "Smooth nodes, native to the Philippines."));
        metadataMap.put("Kawayang Tinik", new BambooMetadata("Bambusa spinosa", "Poaceae", "Spiny and widely distributed in the Philippines."));
        metadataMap.put("Kayali", new BambooMetadata("Bambusa tulda", "Poaceae", "Flexible culms, widely used in construction."));
        metadataMap.put("Long Bamboo", new BambooMetadata("Dendrocalamus longispathus", "Poaceae", "Tall, graceful, good for scaffolding."));
        metadataMap.put("Malayan Bamboo", new BambooMetadata("Gigantochloa scortechinii", "Poaceae", "Commonly found in Malaysia."));
        metadataMap.put("Malaysian Bamboo", new BambooMetadata("Schizostachyum brachycladum", "Poaceae", "Used for weaving and decoration."));
        metadataMap.put("Old ham Bamboo", new BambooMetadata("Bambusa oldhamii", "Poaceae", "Tall, timber bamboo."));
        metadataMap.put("Pole Vault Bamboo", new BambooMetadata("Arundinaria gigantea", "Poaceae", "Sometimes used in sports and crafts."));
        metadataMap.put("Random Objects", new BambooMetadata("Phyllostachys aurea", "Poaceae", "Fast-spreading bamboo species."));
        metadataMap.put("Running Bamboo", new BambooMetadata("Phyllostachys aurea", "Poaceae", "Fast-spreading bamboo species."));
        metadataMap.put("Solid Calcutta", new BambooMetadata("Dendrocalamus strictus", "Poaceae", "Solid internodes, very strong."));
        metadataMap.put("Taiwan Bamboo", new BambooMetadata("Bambusa dolichoclada", "Poaceae", "Grows well in warm climates."));
        metadataMap.put("Wamin", new BambooMetadata("Bambusa vulgaris 'Wamin'", "Poaceae", "Dwarf variety with swollen nodes."));
        metadataMap.put("Yello Bamboo", new BambooMetadata("Bambusa vulgaris 'Vittata'", "Poaceae", "Yellow striped ornamental bamboo."));
        metadataMap.put("Yellow Buho", new BambooMetadata("Schizostachyum lumampao var. flava", "Poaceae", "Rare yellow variant of buho."));
    }

    public static String getScientificName(String label) {
        if (label == null || label.equals("This is not bamboo")) return "N/A";
        BambooMetadata metadata = metadataMap.get(label);
        return (metadata != null) ? metadata.scientificName : "N/A";
    }

    public static String getFamilyName(String label) {
        if (label == null || label.equals("This is not bamboo")) return "N/A";
        BambooMetadata metadata = metadataMap.get(label);
        return (metadata != null) ? metadata.familyName : "N/A";
    }

    public static String getDescription(String label) {
        if (label == null || label.equals("This is not bamboo")) return "N/A";
        BambooMetadata metadata = metadataMap.get(label);
        return (metadata != null) ? metadata.description : "N/A";
    }

    private static class BambooMetadata {
        String scientificName;
        String familyName;
        String description;

        BambooMetadata(String scientificName, String familyName, String description) {
            this.scientificName = scientificName;
            this.familyName = familyName;
            this.description = description;
        }
    }
}
