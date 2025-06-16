import numpy as np
import tensorflow as tf
from PIL import Image
import io

# Bamboo species class labels
class_labels = [
    "Beema", "Bical Babi", "Black Bamboo", "Boos Bamboo", "Buddha Belly", "Buho",
    "Giant Bamboo", "Giant Bolo", "Hedge Bamboo", "Iron Bamboo", "Japanese Bamboo",
    "Kawayan Kiling", "Kawayang Bayog", "Kawayang Tinik", "Kayali", "Long Bamboo",
    "Malayan Bamboo", "Malaysian Bamboo", "Old ham Bamboo", "Pole Vault Bamboo",
    "Running Bamboo", "Solid Calcutta", "Taiwan Bamboo", "Wamin", "Yello Bamboo", "Yellow Buho"
]

# Optional: dictionaries for metadata (can be expanded later)
scientific_names = {label: f"Scientific {label}" for label in class_labels}
family_names = {label: "Poaceae" for label in class_labels}
short_descriptions = {label: f"{label} is a type of bamboo found in the Philippines." for label in class_labels}

def classify_image(image_bytes, model_path):
    # Load model
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    # Convert image bytes to image
    image = Image.open(io.BytesIO(image_bytes)).convert("RGB").resize((250, 250))
    input_data = np.expand_dims(np.array(image, dtype=np.float32) / 255.0, axis=0)

    # Run inference
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()
    output_data = interpreter.get_tensor(output_details[0]['index'])

    prediction = np.argmax(output_data[0])
    confidence = float(np.max(output_data[0])) * 100
    label = class_labels[prediction]

    # Return result as a dictionary (as Java expects)
    return {
        "label": label,
        "confidence": f"{confidence:.2f}%",
        "scientific_name": scientific_names.get(label, "Unknown"),
        "family_name": family_names.get(label, "Unknown"),
        "short_description": short_descriptions.get(label, "No description available.")
    }
