# AI Research: Android On-Device Inference Landscape

> **DISCLAIMER:** This file was 100% AI-generated and is pending human review for technical accuracy.

## Table 1: Tool/Library vs. Model & Hardware Support

This table explores how different AI tools and libraries interact with mobile hardware backends.

| Tool / Library | Origin / Owner | Model Format | Preferred Backend | G2 (Pixel 7 Pro) Status | Device Reach |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **llama.cpp** | Open Source (ggerganov) | GGUF | **CPU (NEON / I8MM)** | Supported (Best on CPU) | High (Vulkan/CPU) |
| **FastText (Native)** | Meta (Facebook) | .ftz / .bin | **CPU (C++)** | **Ultra-Fast** (<5ms) | **Universal (99%+)** |
| **TFLite (LiteRT)** | Google | .tflite | **GPU / NPU / CPU** | Supported | **Very High (90%+)** |
| **ONNX Runtime** | Microsoft | .onnx | **CPU / NNAPI** | Supported | High |
| **MLC-LLM** | Open Source (TVM) | MLC Format | **Vulkan / WebGPU** | Supported (Vulkan 1.3) | High |
| **ExecuTorch** | Meta (PyTorch) | PTE | **CPU / Vulkan** | Supported | High |
| **ML Kit GenAI** | Google | Gemini Nano | **NPU (TPU)** | **Not Supported** | Low (<10%) |

---

## The "Universality" vs. "Performance" Trade-off

When selecting a technology for a project that must support multiple Android device families, consider the following hierarchy of "Reach":

### 1. **Universal Reach (Legacy & Low-End Support)**
*   **Technologies:** **FastText (Native C++)**.
*   **Why:** Requires almost zero resources. It doesn't even need a GPU. If the phone can turn on, it can run FastText.
*   **Best For:** Language ID, simple Topic Classification, Sentiment analysis.

### 2. **High Reach (Modern Android Support)**
*   **Technologies:** **TFLite**, `llama.cpp`, **ONNX Runtime**.
*   **Why:** These are standalone C++ libraries. They don't depend on the phone manufacturer's OS updates. As long as you bundle the library in your APK, it works.
*   **Best For:** MobileBERT (Topic Classification), Llama-3.2-1B (Summarization).

### 3. **Low Reach (System-Dependent)**
*   **Technologies:** **ML Kit GenAI**, **Samsung AICore**.
*   **Why:** These require specific OS versions (Android 14+) and specific hardware (Tensor G3+).
*   **Best For:** "Premium" AI features where you don't mind excluding 90% of users.

## Recommendation for the Current Project
1.  **For 20-word Topic Classification:** Use **FastText** if you have a training dataset. It will be 1,000x faster than what you have now. 
2.  **If you need LLM "Reasoning":** Stick with **`llama.cpp`** but switch to a **1B or 3B model**. This is the only "single code" way to get LLM results on your Pixel 7 Pro and a random Samsung S21 with the same performance profile.

---

## Table 2: Device Family vs. AI Hardware & Vulkan Support

This table maps specific device generations to their AI acceleration capabilities and Vulkan versions.

| Manufacturer | Device Family | Chipset (SoC) | AI Accelerator (NPU) | GPU | Vulkan Version |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Google** | Pixel 9 Series | Tensor G4 | **Rio TPU** (GenAI Opt.) | Mali-G715 | 1.3 |
| **Google** | Pixel 8 Series | Tensor G3 | **Rio TPU** (GenAI Opt.) | Mali-G715 | 1.3 |
| **Google** | **Pixel 7 Series** | **Tensor G2** | **Edge TPU** (Legacy) | **Mali-G710** | **1.3** |
| **Samsung** | S24 Ultra (US) | SD 8 Gen 3 | Qualcomm Hexagon | Adreno 750 | 1.3 |
| **Samsung** | S24 / S24+ (Intl) | Exynos 2400 | Samsung Dual NPU | Xclipse 940 | 1.3 |
| **Samsung** | S23 Series | SD 8 Gen 2 | Qualcomm Hexagon | Adreno 740 | 1.3 |
| **Samsung** | S22 Series | SD 8 Gen 1 / Exynos 2200 | Hexagon / Samsung NPU | Adreno 730 / Xclipse 920 | 1.1 / 1.2 |
| **Samsung** | S21 Series | SD 888 / Exynos 2100 | Hexagon / Samsung NPU | Adreno 660 / Mali-G78 | 1.1 |

---

## Technical Notes for Pixel 7 Pro (Tensor G2)
*   **The "TPU Gap":** While the G2 has a TPU, it is not currently accessible via the official Google AI Edge SDK or Gemini Nano (which requires the "Rio" TPU in G3/G4).
*   **llama.cpp Strategy:** For the G2, running models on the **CPU** (using ARMv8.2 dot product instructions) typically yields more stable performance than Vulkan, as the memory bandwidth overhead for the Mali-G710 GPU can bottleneck small LLM inference.
*   **Vulkan Support:** The G2 supports Vulkan 1.3, which is excellent for compatibility with modern cross-platform libraries like MLC-LLM or MediaPipe, even if raw TPU access is restricted.
