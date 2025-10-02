package com.google.mediapipe.tasks.core;

public enum Quantization {
  FLOAT_16("fp16"),
  FLOAT_32("fp32"),
  INT_8("int8");

  private final String description;

  /**
   * Factory method to create a {@link Quantization} from the model metadata.
   *
   * @param description a string representing the quantization type.
   */
  public static Quantization fromCanonicalName(String description) {
    for (Quantization quantization : Quantization.values()) {
      if (quantization.getDescription().equals(description)) {
        return quantization;
      }
    }
    throw new IllegalArgumentException("Unsupported quantization type: " + description);
  }

  Quantization(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
