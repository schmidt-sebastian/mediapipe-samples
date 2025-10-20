package com.google.mediapipe.tasks.core

enum class Quantization(description: String) {
    FLOAT16("fp16"),
    FLOAT32("fp32"),
    INT8("int8");

    val description: String?

    init {
        this.description = description
    }

    companion object {
        /**
         * Factory method to create a [Quantization] from the model metadata.
         *
         * @param description a string representing the quantization type.
         */
        fun fromCanonicalName(description: String?): Quantization {
            for (quantization in values()) {
                if (quantization.description == description) {
                    return quantization
                }
            }
            throw IllegalArgumentException("Unsupported quantization type: " + description)
        }
    }
}
